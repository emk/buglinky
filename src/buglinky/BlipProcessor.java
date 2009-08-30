// buglinky - A robot for adding bugtracker links to a wave
// Copyright 2009 Eric Kidd
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package buglinky;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.Range;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;

/**
 * Annotates and edits a blip.
 */
abstract class BlipProcessor {
	private static final Logger LOG =
		Logger.getLogger(BlipProcessor.class.getName());
	
	/**
	 * The accumulated difference between the original length of the
	 * blip, and the length after text replacements have been performed.
	 */
	private int totalCorrection;

	/**
	 * Apply a list of blip processors to any blips which have changed.
	 * 
	 * @param processors The event processors to apply, in order.
	 * @param bundle     The event bundle indicating which blips have changed.
	 * @param myAddress  The Wave address of the current bot, used to make sure
	 *                   we don't respond to our own messages.  BE CAREFUL!
	 *                   Failure to specify the correct address will cause
	 *                   infinite loops and overwhelm the servers.
	 */
	static void applyProcessorsToChangedBlips(
			ArrayList<BlipProcessor> processors,
			RobotMessageBundle bundle, String myAddress) {
		// Find all affected blips.
		HashSet<Blip> changedBlips = new HashSet<Blip>();
		for (Event e : bundle.getEvents()) {
			if (!e.getModifiedBy().equals(myAddress)) {
				switch (e.getType()) {
				case BLIP_SUBMITTED:        // The user has clicked "Done".
				case BLIP_VERSION_CHANGED:  // The blip has been updated.
					changedBlips.add(e.getBlip());
					break;
					
				default:
					break;
				}
			}
		}
		
		// Process all affected blips.
		for (Blip blip : changedBlips) {
			for (BlipProcessor processor : processors)
				processor.processBlip(blip);
		}
	}

	/**
	 * Apply this text processor to the specified blip.  This function
	 * is not re-entrant.
	 * 
	 * @param blip The blip to process.
	 */
	public void processBlip(Blip blip) {
		LOG.fine("Processing blip " + blip.getBlipId() + " with " +
				this.getClass().getName());
		// Adapted from http://senikk.com/min-f%C3%B8rste-google-wave-robot,
		// a robot which links to @names on Twitter.
		TextView doc = blip.getDocument();
		totalCorrection = 0; // Reset.
		Matcher matcher = getCompiledPattern().matcher(doc.getText());
		while (matcher.find()) {
			LOG.fine("Found match to process: " + matcher.group());
			int start = matcher.start() + totalCorrection;
			int end   = matcher.end()   + totalCorrection;
			processMatch(doc, new Range(start, end), matcher);
		}
	}

	/**
	 * Return a regular expression matching the text we want to process.
	 */
	protected abstract String getPattern();
	
	/**
	 * Take our simple pattern, add some kludges, and compile it.
	 */
	private Pattern getCompiledPattern() {
		// KLUDGE - Try to avoid annotating text while the user's caret is
		// still inside the annotation.  For example, imagine that the user
		// types:
		//
		//   bug #12|
		//
		// ...where "|" represents the cursor.  We could immediately annotate
		// this with a link:
		//
		//   [bug #12|]
		//
		// ...but this will tend to make a mess when the user keeps typing:
		//
		//   [bug #12 is very annoying|]
		//
		// Instead, we require at least one non-newline character to appear 
		// after the match before we try to annotate it.  (Note that this hack
		// won't work anywhere but at the end of a paragraph.  Users making
		// modifications inside of paragraphs will have to live with minor
		// glitches until the Wave API improves.)
		//
		// To do this, we use a zero-width negative lookahead pattern.  But we
		// don't want to use the last character that would normally be matched
		// by getPattern as our negative lookahead, so we use a possessive
		// qualifier to avoid backtracking.
		return Pattern.compile("(?:" + getPattern() + "){1}+(?!\\r|\\n)");
	}
	
	/**
	 * Process a regular expression match.  Use the annotate and replace
	 * functions to perform the actual transformations.
	 * 
	 * @param doc   The document containing the match.
	 * @param range The range of text in the blip that was matched.
	 * @param match The regular expression match object.  Note that the
	 *              offsets provided by this object may be incorrect
	 *              because of previous text replacements.
	 * 
	 * @see BlipProcessor#annotate(TextView, Range, String, String)
	 * @see BlipProcessor#replace(TextView, Range, String)
	 */
	protected abstract void processMatch(TextView doc, Range range,
			Matcher match);

	/**
	 * Add an annotation if it isn't already present.
	 * 
	 * The Wave Robot API does not currently filter out duplicate annotation
	 * requests, which causes extra network traffic and more possibilities for
	 * nasty bot loops.  So we filter them out ourselves.
	 * 
	 * @param doc   The TextView to containing the text to annotate.
	 * @param range The range of text to apply the annotation to.
	 * @param name  The name of the annotation.
	 * @param value The value of the annotation. 
	 */
	protected void annotate(TextView doc, Range range, String name,
			String value) {
		// If this annotation is already present, give up now.  Note that
		// we allow the existing annotation to be bigger than the one we're
		// creating, because in that case, setting the new annotation won't
		// do anything useful.
		for (Annotation annotation : doc.getAnnotations(range, name)) {
			if (annotation.getValue().equals(value) &&
					annotation.getRange().getStart() <= range.getStart() &&
					range.getEnd() <= annotation.getRange().getEnd())
				return;
		}
		
		LOG.fine("Annotating with " + name + "=" + value);
		doc.setAnnotation(range, name, value);
	}

	/**
	 * Replace the specified range in the TextView with a new string, leaving
	 * existing annotations alone.
	 * 
	 * @param doc   The TextView to containing the text to annotate.
	 * @param range The range of text to replace.  This must fall entirely
	 *              within the current match.
	 * @param text  The replacement text.
	 */
	protected void replace(TextView doc, Range range, String text) {
		doc.replace(range, text);

		// Update our correction factor to account for this replacement.
		int oldLength = range.getEnd() - range.getStart();
		totalCorrection = (totalCorrection - oldLength) + text.length();
	}	
}