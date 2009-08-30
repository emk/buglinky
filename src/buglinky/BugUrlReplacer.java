package buglinky;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.Range;
import com.google.wave.api.TextView;

/**
 * Convert raw bug tracker URLs into text of the form "issue #NNN".
 */
class BugUrlReplacer extends BlipProcessor {
	/** The URL to a bug, minus the actual bug number. */
	private String bugUrl;

	/** Create a BugUrlReplacer for the specified URL. */
	public BugUrlReplacer(String bugUrl) {
		super();
		this.bugUrl = bugUrl;
	}

	@Override
	protected String getPattern() {
		return Pattern.quote(bugUrl) + "(\\d+)";
	}

	@Override
	protected void processMatch(TextView doc, Range range, Matcher match) {
		replace(doc, range, "issue #" + match.group(1));
	}

}
