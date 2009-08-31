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
		return BugLinkyServlet.APP_NAME;
	}

	@Override
	public String getRobotProfilePageUrl() {
		return "http://github.com/emk/buglinky";
	}
}
