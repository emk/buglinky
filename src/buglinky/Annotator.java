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
 * Adds annotations to a blip.
 */
abstract class Annotator {
	private static final Logger LOG =
		Logger.getLogger(Annotator.class.getName());

	/** Return a regular expression matching the text we want to process. */
	protected abstract Pattern getPattern();
	
	/** Process a regular expression match. */
	protected abstract void processMatch(TextView doc, Range range,
			Matcher match);

	/** Add links to the specified blip. */
	public void processBlip(Blip blip) {
		LOG.fine("Annotating blip " + blip.getBlipId());
		// Adapted from http://senikk.com/min-f%C3%B8rste-google-wave-robot,
		// a robot which links to @names on Twitter.
		TextView doc = blip.getDocument();
		Matcher matcher = getPattern().matcher(doc.getText());
		while (matcher.find()) {
			LOG.fine("Found text to annotate: " + matcher.group());
			Range range = new Range(matcher.start(), matcher.end());
			processMatch(doc, range, matcher);
		}
	}

	/**
	 * Add an annotation if it isn't already present.
	 * 
	 * The Wave Robot API does not currently filter out duplicate annotation
	 * requests, which causes extra network traffic and more possibilities for
	 * nasty bot loops.  So we do this screening on our end.
	 */
	protected void maybeAnnotate(TextView doc, Range range, String name,
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