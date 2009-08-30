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
import java.util.logging.Logger;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;

/** Called via JSON-RPC whenever an event occurs on one of our waves. */
@SuppressWarnings("serial")
public class BugLinkyServlet extends AbstractRobotServlet {
	private static final Logger LOG =
		Logger.getLogger(BugLinkyServlet.class.getName());
	
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

	/** The instructions to display when we join a wave. */
	private static final String INSTRUCTIONS =
		"buglinky will attempt to link \"bug #NNN\" to a bug tracker.";

	/** The URL to a specific bug in our bug tracker, minus the number. */
	private static final String BUG_URL =
		"http://code.google.com/p/google-wave-resources/issues/detail?id=";
	
	@Override
	public void processEvents(RobotMessageBundle bundle) {
		if (bundle.wasSelfAdded())
			addInstructionsToWave(bundle);
		processBlips(bundle);
	}

	/** Add an instruction blip to this wave if we were just added. */
	private void addInstructionsToWave(RobotMessageBundle bundle) {
		LOG.fine("Adding instructions to wavelet " +
				bundle.getWavelet().getWaveletId());
		Blip blip = bundle.getWavelet().appendBlip();
		TextView textView = blip.getDocument();
		textView.append(INSTRUCTIONS);
	}

	/** Process any blips which have changed. */
	private void processBlips(RobotMessageBundle bundle) {
		// We clean up URLs first, so that we can annotate the newly-created
		// text in the second pass.
		ArrayList<BlipProcessor> processors = new ArrayList<BlipProcessor>();
		processors.add(new BugUrlReplacer(BUG_URL)); 
		processors.add(new BugNumberLinker(BUG_URL)); 
		BlipProcessor.applyProcessorsToChangedBlips(processors, bundle,
				BOT_ADDRESS);		
	}
}
