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

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Range;
import com.google.wave.api.TextView;

/**
 * Annotates and edits a blip.
 */
abstract class BlipProcessor {
	private static final Logger LOG =
		Logger.getLogger(BlipProcessor.class.getName());

	/**
	 * Annotate the specified blip.
	 * 
	 * @param blip The blip to process.
	 */
	public void processBlip(Blip blip) {
		LOG.fine("Processing blip " + blip.getBlipId() + " with " +
				this.getClass().getName());
		// Adapted from http://senikk.com/min-f%C3%B8rste-google-wave-robot,
		// a robot which links to @names on Twitter.
		TextView doc = blip.getDocument();
		Matcher matcher = getCompiledPattern().matcher(doc.getText());
		while (matcher.find()) {
			LOG.fine("Found match to process: " + matcher.group());
			Range range = new Range(matcher.start(), matcher.end());
			processMatch(doc, range, matcher);
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
}