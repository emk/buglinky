package buglinky;

import java.util.logging.Logger;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.Event;
import com.google.wave.api.FormElement;
import com.google.wave.api.FormView;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

/** Display our configuration options and instructions for using buglinky. */
public class AdminView {
	private static final Logger LOG =
		Logger.getLogger(AdminView.class.getName());

	/** The URL to a specific bug in our bug tracker, minus the number. */
	private static final String BUG_URL =
		"http://code.google.com/p/google-wave-resources/issues/detail?id=";

	/** The instructions to display when we join a wave. */
	private static final String INSTRUCTIONS =
		"buglinky will attempt to link \"issue #NNN\" to the Wave issue " +
		"tracker.\n\n" +
		"Note that the issue number must not be at the very end of " +
		"a paragraph. This is temporary kludge to discourage buglinky from " +
		"annotating your insertion point as you type.\n\n" +
		"Once you've set your preferences, you can delete this blip.";

	/** Get the URL prefix we'll use to link to bugs. */
	static String getBugUrl(Wavelet wavelet) {
		String bugUrl = wavelet.getDataDocument("buglinky-url");
		if (bugUrl == null)
			bugUrl = BUG_URL;
		LOG.fine("Using issue URL " + bugUrl);
		return bugUrl;
	}
	
	/** Add an admin view to the specified wavelet. */
	static void createFor(Wavelet wavelet) {
		LOG.fine("Adding instructions to wavelet " + wavelet.getWaveletId());
		Blip blip = wavelet.appendBlip();
		TextView textView = blip.getDocument();
		textView.append(INSTRUCTIONS);
		
		// Our form-handling code is heavily inspired by the original
		// "Polly the Pollster" bot.
		textView.append("\n\n");
		textView.appendElement(new FormElement(ElementType.LABEL,
				"bugUrl", "Enter your issue URL, minus the issue number:"));
		textView.appendElement(new FormElement(ElementType.INPUT,
				"bugUrl", getBugUrl(wavelet)));
		textView.append("\n");
		textView.appendElement(new FormElement(ElementType.BUTTON,
				"saveButton", "Save Preferences"));
		textView.setAnnotation("buglinky-admin", "");		
	}

	/** Does the specified blip contain our AdminView? */
	public static boolean isAdminBlip(Blip blip) {
		return blip.getDocument().hasAnnotation("buglinky-admin");
	}

	/** The blip containing our admin view. */
	private Blip blip;
	
	/** Create an object which provides easy access to our AdminView. */
	AdminView(Blip blip) {
		this.blip = blip;
	}

	/** Called when a button is pressed in our AdminView. */
	public void onFormButtonClicked(Event e) {
		if (e.getButtonName().equals("saveButton")) {
			LOG.fine("Buglinky save button clicked");
			FormView form = blip.getDocument().getFormView();
			String newUrl = form.getFormElement("bugUrl").getValue();
			if (!newUrl.matches("^ *$")) {
				LOG.fine("Setting issue URL to " + newUrl);
				e.getWavelet().setDataDocument("buglinky-url", newUrl);
			}
		}		
	}
}
