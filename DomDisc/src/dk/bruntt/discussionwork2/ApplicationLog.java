package dk.bruntt.discussionwork2;

import java.util.Date;

import android.util.Log;
import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.AppLog;

public class ApplicationLog {
	
	private static String TAG = "DomDisc";
	
	/**
	 * Log Error
	 * @param logText
	 */
	public final static void e(String logText) {
		String level = "e";
		add(logText, level);
		Log.e(TAG, logText);
	}

	/**
	 * Log informational
	 * @param logText
	 */
	public final static void i(String logText) {
		String level = "i";
		add(logText, level);
		Log.i(TAG, logText);
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
	 * @param shouldCommitToLog
	 */
	public final static void d(String logText, boolean shouldCommitToLog) {
		String level = "d";
		if (shouldCommitToLog) {
			add(logText, level);	
		}
		Log.d(TAG, logText);
	}

	/**
	 * Log Warning
	 * @param logText
	 */
	public final static void w(String logText) {
		String level = "w";
		add(logText, level);
		Log.w(TAG, logText);
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
