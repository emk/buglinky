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

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;

/** Called via JSON-RPC whenever an event occurs on one of our waves. */
@SuppressWarnings("serial")
public class BugLinkyServlet extends AbstractRobotServlet {
	
	/**
	 * The name of this application on Google App Engine.  This is used
	 * to ignore our own edits, and as our default profile name.
	 * 
	 * BE SURE TO UPDATE THIS FOR YOUR BOT! If you don't update this
	 * correctly, your bot will go into an infinite loop and generate
	 * dozens of useless messages per second.
	 */
	static final String APP_NAME = "buglinky";
	
	/** The wave address for this bot.  Used to ignore our own edits. */
	private static final String BOT_ADDRESS = APP_NAME + "@appspot.com";
	
	@Override
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
		if (bundle.wasSelfAdded())
			AdminView.createFor(wavelet);
		dispatchUiEvents(bundle);
		processBlips(bundle, AdminView.getBugUrl(wavelet));
	}

	/** Dispatch UI events to the appropriate view. */
	private void dispatchUiEvents(RobotMessageBundle bundle) {
		for (Event e : bundle.getEvents()) {
			if (e.getType() == EventType.FORM_BUTTON_CLICKED) {
				if (AdminView.isAdminBlip(e.getBlip())) {
					AdminView adminView = new AdminView(e.getBlip());
					adminView.onFormButtonClicked(e);
				}
			}
		}
	}

	/** Process any blips which have changed. */
	private void processBlips(RobotMessageBundle bundle, String bugUrl) {
		// We clean up URLs first, so that we can annotate the newly-created
		// text in the second pass.
		ArrayList<BlipProcessor> processors = new ArrayList<BlipProcessor>();
		processors.add(new BugUrlReplacer(bugUrl)); 
		processors.add(new BugNumberLinker(bugUrl)); 
		BlipProcessor.applyProcessorsToChangedBlips(processors, bundle,
				BOT_ADDRESS);		
	}
}
