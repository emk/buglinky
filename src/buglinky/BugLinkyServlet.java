package buglinky;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.*;

@SuppressWarnings("serial")
public class BugLinkyServlet extends AbstractRobotServlet {
	private static final Logger log = Logger.getLogger(BugLinkyServlet.class.getName());
	private static final Pattern REGEX = Pattern.compile("bug #(\\d+)");
	private static final String BUG_URL =
		"http://code.google.com/p/google-wave-resources/issues/detail?id=";

	/** Called when we receive events from the Wave server. */
	@Override
	public void processEvents(RobotMessageBundle bundle) {
		if (bundle.wasSelfAdded())
			addInstructionsToWave(bundle);
		dispatchEvents(bundle);
	}

	/** Add an instruction blip to this wave if we were just added. */
	private void addInstructionsToWave(RobotMessageBundle bundle) {
		log.info("Adding instructions to wavelet " + bundle.getWavelet().getWaveletId());
		Blip blip = bundle.getWavelet().appendBlip();
		TextView textView = blip.getDocument();
		textView.append("buglinky will attempt to link \"bug #NNN\" to a bug tracker.");
	}

	/** Dispatch events to the appropriate handler method. */
	private void dispatchEvents(RobotMessageBundle bundle) {
		for (Event e: bundle.getEvents()) {
			if (e.getType() == EventType.BLIP_SUBMITTED)
				addLinksToBlip(e.getBlip());
		}
	}

	/** Add links to the specified blip. */
	private void addLinksToBlip(Blip blip) {
		log.info("Adding links to blip " + blip.getBlipId());
		// Adapted from http://senikk.com/min-f%C3%B8rste-google-wave-robot,
		// a robot which links to @names on Twitter.
		TextView doc = blip.getDocument();
		Matcher matcher = REGEX.matcher(doc.getText());
		while (matcher.find()) {
			log.info("Found a link: " + matcher.group());
			Range range = new Range(matcher.start(), matcher.end());
			String bugNumber = matcher.group(1);
			doc.setAnnotation(range, "link/manual", BUG_URL.concat(bugNumber));
		}
	}
}
