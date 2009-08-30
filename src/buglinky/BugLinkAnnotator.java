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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.Range;
import com.google.wave.api.TextView;

/** Add bug links to a blip. */
class BugLinkAnnotator extends Annotator {
	/** The URL to a bug, minus the actual bug number. */
	private String bugUrl;

	/** Create a BugLinkAnnotator for the specified URL. */
	public BugLinkAnnotator(String bugUrl) {
		this.bugUrl = bugUrl;
	}

	/** Return a regular expression matching the text we want to process. */
	protected Pattern getPattern() {
		// Regex used to find bug numbers in the text. Note that we require at
		// least one non-numeric character after the bug number (and not a
		// newline).  This ensures that when the user is adding text at the
		// end of a paragraph, we won't add any links until the user is safely
		// outside the area that we need to modify. Users making modifications
		// inside of paragraphs will have to live with minor glitches.
		return Pattern.compile("(?:bug|issue) #(\\d+)(?!\\d|\\r|\\n)");
	}
	
	/** Process a regular expression match. */
	protected void processMatch(TextView doc, Range range, Matcher match) {
		maybeAnnotate(doc, range, "link/manual", bugUrl.concat(match.group(1)));
	}
}