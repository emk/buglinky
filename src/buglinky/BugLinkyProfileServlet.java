package buglinky;

import com.google.wave.api.ProfileServlet;

/** Provide a name and icon for our robot. */
@SuppressWarnings("serial")
public class BugLinkyProfileServlet extends ProfileServlet {

	@Override
	public String getRobotAvatarUrl() {
		return "http://emk-test.appspot.com/avatar.png";
	}

	@Override
	public String getRobotName() {
		return "buglinky";
	}
}
