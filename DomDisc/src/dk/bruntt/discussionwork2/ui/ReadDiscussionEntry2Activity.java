package dk.bruntt.discussionwork2.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import dk.bruntt.discussionwork2.ApplicationLog;
import dk.bruntt.discussionwork2.R;
import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionEntry;



public class ReadDiscussionEntry2Activity extends SherlockFragmentActivity {
	
	public static final String EXTRA_URL = "unid";
	private boolean shouldCommitToLog = false;
	
	 @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    DatabaseManager.init(getApplication());
	    shouldCommitToLog = getLogALot(this);
	    ApplicationLog.d(getClass().getSimpleName() + " onCreate ", shouldCommitToLog);

	    // Need to check if Activity has been switched to landscape mode
	    // If yes, finished and go back to the start Activity
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	ApplicationLog.d(getClass().getSimpleName() + " Landscape - will exit", shouldCommitToLog);
	      finish();
	      return;
	    }
	    
	    /**
	     * Needs basic protection from errors
	     */
	    setContentView(R.layout.read_discussion_entry2);
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	      String unid = extras.getString(EXTRA_URL);
	      if (unid == null || unid == "") {
	    	  ApplicationLog.w(getClass().getSimpleName() + " NO unid received: " + unid);
	      } else {
	    	  ApplicationLog.d(getClass().getSimpleName() + " unid received: " + unid, shouldCommitToLog);
		      
		      DiscussionEntry discussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(unid); 
		      
		      ApplicationLog.d(getClass().getSimpleName() + " Document to show: " + discussionEntry.getSubject(), shouldCommitToLog);
		      
		      ReadDiscussionEntryFragment fragment = (ReadDiscussionEntryFragment) getSupportFragmentManager().findFragmentById(R.id.discussionEntryFragment);
		      fragment.setDiscussionEntry(discussionEntry);
	      }
	      
	    }
	  }

	 private static boolean getLogALot(Context ctxt) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			return prefs.getBoolean("checkbox_preference_logalot", false);
		}

}
