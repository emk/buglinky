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

import com.google.wave.api.*;

/** Called via JSON-RPC whenever an event occurs on one of our waves. */
@SuppressWarnings("serial")
public class BugLinkyServlet extends AbstractRobotServlet {
	private static final Logger LOG =
		Logger.getLogger(BugLinkyServlet.class.getName());
	
	private static final String ME = "buglinky@appspot.com";
	private static final String LINK = "link/manual";

	private static final String INSTRUCTIONS =
		"buglinky will attempt to link \"bug #NNN\" to a bug tracker.";
	private static final String BUG_URL =
		"http://code.google.com/p/google-wave-resources/issues/detail?id=";

	/**
	 * Regex used to find bug numbers in the text. Note that we require at least
	 * one non-numeric character after the bug number (and not a newline). This
	 * ensures that when the user is adding text at the end of a paragraph, we
	 * won't add any links until the user is safely outside the area that we
	 * need to modify. Users making modifications inside of paragraphs will have
	 * to live with minor glitches.
	 */
	private static final Pattern REGEX =
		Pattern.compile("(?:bug|issue) #(\\d+)(?!\\d|\\r|\\n)");

	/** Called when we receive events from the Wave server. */
	@Override
	public void processEvents(RobotMessageBundle bundle) {
		if (bundle.wasSelfAdded())
			addInstructionsToWave(bundle);
		dispatchEvents(bundle);
	}

	/** Add an instruction blip to this wave if we were just added. */
	private void addInstructionsToWave(RobotMessageBundle bundle) {
		LOG.fine("Adding instructions to wavelet " +
				bundle.getWavelet().getWaveletId());
		Blip blip = bundle.getWavelet().appendBlip();
		TextView textView = blip.getDocument();
		textView.append(INSTRUCTIONS);
	}

	/** Dispatch events to the appropriate handler method. */
	private void dispatchEvents(RobotMessageBundle bundle) {
		for (Event e : bundle.getEvents()) {
			if (!e.getModifiedBy().equals(ME)) {
				switch (e.getType()) {
				// One or the other of these should be wired up in
				// capabilities.xml.  If we use BLIP_SUBMITTED, we'll apply
				// our links once the user clicks "Done".  If we use
				// BLIP_VERSION_CHANGED, we'll apply our links in real time.
				case BLIP_SUBMITTED:
				case BLIP_VERSION_CHANGED:
					addLinksToBlip(e.getBlip());
					break;
					
				default:
					break;
				}
			}
		}
	}

	/** Add links to the specified blip. */
	private void addLinksToBlip(Blip blip) {
		LOG.fine("Adding links to blip " + blip.getBlipId());
		// Adapted from http://senikk.com/min-f%C3%B8rste-google-wave-robot,
		// a robot which links to @names on Twitter.
		TextView doc = blip.getDocument();
		Matcher matcher = REGEX.matcher(doc.getText());
		while (matcher.find()) {
			LOG.fine("Found a link: " + matcher.group());
			Range range = new Range(matcher.start(), matcher.end());
			String url = BUG_URL.concat(matcher.group(1));
			maybeAnnotate(doc, range, LINK, url);
		}
	}

	/**
	 * Add an annotation if it isn't already present.
	 * 
	 * The Wave Robot API does not currently filter out duplicate annotation
	 * requests, which causes extra network traffic and more possibilities for
	 * nasty bot loops.  So we do this screening on our end.
	 */
	private void maybeAnnotate(TextView doc, Range range, String name,
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
		
		LOG.fine("Annotating with " + value);
		doc.setAnnotation(range, name, value);
	}
}
