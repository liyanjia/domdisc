package dk.bruntt.dominodiscussionwork2;

import java.util.Date;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dk.brunt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.AppLog;

public class ApplicationLog {
	
	/**
	 * Log Error
	 * @param logText
	 */
	public final static void e(String logText) {
		String level = "e";
		add(logText, level);
	}

	/**
	 * Log informational
	 * @param logText
	 */
	public final static void i(String logText) {
		String level = "i";
		add(logText, level);
	}

//	/**
//	 * Log debug
//	 * @param logText
//	 */
//	public final static void d(String logText) {
//		String level = "d";
//		add(logText, level);
//	}
	
	/**
	 * Log debug. Only saves to Log database if shouldCommit is true
	 * @param logText
	 * @param shouldCommit
	 */
	public final static void d(String logText, boolean shouldCommit) {
		String level = "d";
		if (shouldCommit) {
			add(logText, level);	
		}
	}

	/**
	 * Log Warning
	 * @param logText
	 */
	public final static void w(String logText) {
		String level = "w";
		add(logText, level);
	}
	
	private final static void add(String logText, String level) {
		if (logText == null) {
			logText = "No logtext";
		}
		
		try {
			AppLog l = new AppLog();
			l.setMessage(logText);
			l.setLevel(level);
			String currentDateTimeString = java.text.DateFormat.getTimeInstance()
					.format(new Date());
			l.setLogTime(currentDateTimeString);
			
			DatabaseManager.getInstance().addAppLog(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
}
