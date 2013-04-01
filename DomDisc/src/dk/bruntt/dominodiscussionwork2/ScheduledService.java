/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package dk.bruntt.dominodiscussionwork2;

import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import dk.brunt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;

public class ScheduledService extends WakefulIntentService {
	public ScheduledService() {
		super("ScheduledService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		Log.d(getClass().getSimpleName(), "I ran from DomDisc service");
		DatabaseManager.init(this);
		Context applicationContext = getApplicationContext();
		ApplicationLog.i("Background replication service starting");
		long timePassedSinceLastRpelicationMinutes = minutesSinceLastReplication(applicationContext); 
		ApplicationLog.i(" Minutes since last replication: " + timePassedSinceLastRpelicationMinutes);

		GregorianCalendar now = new GregorianCalendar();
		setLastReplication(now, applicationContext);
		
		boolean shouldLogALot = getLogALot(applicationContext);

		try {
			double batteryLevel = -1;

			batteryLevel = getBatteryLevel(batteryLevel);
			ApplicationLog.d("Current battery level: " + batteryLevel, shouldLogALot);
			if (batteryLevel < 0.2) {
				Log.i(getClass().getSimpleName(), "replication is disabled because of low battery - " + batteryLevel);
				ApplicationLog.i("background replication is disabled because of low battery - " + batteryLevel + " (below 20%)");
			} else {
				DiscussionReplicator replicator = new DiscussionReplicator(applicationContext);

				List<DiscussionDatabase> discussionDatabases = DatabaseManager.getInstance().getAllDiscussionDatabases();

				if ((null != discussionDatabases) && (discussionDatabases.size() > 0)) {


					for (int i = 0, size = discussionDatabases.size(); i < size; i++)  
					{  
						DiscussionDatabase discussionDatabase = discussionDatabases.get(i);
						Log.i(getClass().getSimpleName(), "Replicating " + discussionDatabase.getName());
						Log.i(getClass().getSimpleName(), " path: " + discussionDatabase.getDbPath());
						ApplicationLog.i("background Replicating " + discussionDatabase.getName());
						ApplicationLog.i(" path: " + discussionDatabase.getDbPath());
						replicator.replicateDiscussionDatabase(discussionDatabase);
						ApplicationLog.i("== == == == ==");
					}


					//					DiscussionDatabase discussionDatabase = discussionDatabases.get(0);



				} else {
					Log.i(getClass().getSimpleName(), "Background replication stops - no databases configured");
					ApplicationLog.i("Background replication stops - no databases configured");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();

			Log.e(getClass().getSimpleName(), "Background replication stops due to Exception");
			ApplicationLog.e("Background replication stops due to Exception");
			ApplicationLog.e(e.getMessage());
		}


	}


	private double getBatteryLevel(double batteryLevel) {
		//Batteri: http://stackoverflow.com/questions/3661464/get-battery-level-before-broadcast-receiver-responds-for-intent-action-battery-c
		Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		if (rawlevel >= 0 && scale > 0) {
			batteryLevel = rawlevel / scale;
		}
		//		Log.d(getClass().getSimpleName(), " Battery level: "+ batteryLevel);
		return batteryLevel;
	}

	private void setLastReplication(GregorianCalendar replicationTime, Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		SharedPreferences.Editor editor = prefs.edit();
		long millis = replicationTime.getTimeInMillis();
		String toSave = Long.toString(millis);
		editor.putString("lastReplicationTime", toSave);
		editor.commit();
	}

	/**
	 * 
	 * @param ctxt
	 * @return GregorianCalendar for when replication happened last. 1970 if this is the first time.
	 */
	private GregorianCalendar getLastreplication(Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);

		String lastReplication = prefs.getString("lastReplicationTime", "0");

		long milliseconds = Long.parseLong(lastReplication);

		GregorianCalendar lastReplicationTime = new GregorianCalendar();
		lastReplicationTime.setTimeInMillis(milliseconds);
		return lastReplicationTime;
	}


	/**
	 * 
	 * @param applicationContext
	 * @return Long - minutes since background replication task ran last
	 */
	private int minutesSinceLastReplication(Context applicationContext) {
		GregorianCalendar lastReplication = getLastreplication(applicationContext);
		//		ApplicationLog.i("Backgrund service was last run at " + lastReplication.toString());
		GregorianCalendar now = new GregorianCalendar();

		long timePassedSinceLastReplication = now.getTimeInMillis() - lastReplication.getTimeInMillis();
		//		ApplicationLog.i(" Time passed since last replication: " + timePassedSinceLastReplication.toString());
		long timePassedSinceLastRpelicationMinutes = (timePassedSinceLastReplication/1000)/60;
		return (int)timePassedSinceLastRpelicationMinutes;
	}
	/**
	 * 
	 * @param ctxt
	 * @return boolean true if we should log all debuglevels to the
	 *         ApplicationLog
	 */
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}

}
