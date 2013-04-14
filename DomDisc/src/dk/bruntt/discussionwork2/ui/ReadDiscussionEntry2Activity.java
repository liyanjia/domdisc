package dk.bruntt.discussionwork2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import dk.bruntt.discussionwork2.ApplicationLog;
import dk.bruntt.discussionwork2.R;
import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionEntry;



public class ReadDiscussionEntry2Activity extends SherlockFragmentActivity implements ReadDiscussionEntryFragment.OnResponseItemSelectedListener {
	
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
//	    	  ApplicationLog.d(getClass().getSimpleName() + " unid received: " + unid, shouldCommitToLog);
//		      
//		      DiscussionEntry discussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(unid); 
//		      
//		      ApplicationLog.d(getClass().getSimpleName() + " Document to show: " + discussionEntry.getSubject(), shouldCommitToLog);
//		      
//		      ReadDiscussionEntryFragment fragment = (ReadDiscussionEntryFragment) getSupportFragmentManager().findFragmentById(R.id.discussionEntryFragment);
//		      fragment.setDiscussionEntry(discussionEntry);
	    	  
	    	  FrameLayout containerForReadDiscussionEntryFragment = (FrameLayout) findViewById(R.id.discussionEntryFragment);
	    	  
	    	  if (containerForReadDiscussionEntryFragment != null) {
			    	ApplicationLog.d(getClass().getSimpleName() + " fragment is in layout", shouldCommitToLog);
			      
			        // Instantiate a new fragment.
			    	ReadDiscussionEntryFragment newFragment = ReadDiscussionEntryFragment.newInstance(unid);
			        // Add the fragment to the activity, pushing this transaction
			        // on to the back stack.
			        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			        ft.replace(R.id.discussionEntryFragment, newFragment);
			        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//			        ft.addToBackStack(null);
			        ft.commit();
			        ApplicationLog.d(getClass().getSimpleName() + "FragmentTransaction committed", shouldCommitToLog);
			    } else {
			    	ApplicationLog.w(getClass().getSimpleName() + " Did not find the fragment container");
			    }
	    	  
	      }
	      
	    }
	  }

	 private static boolean getLogALot(Context ctxt) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			return prefs.getBoolean("checkbox_preference_logalot", false);
		}
	 

		/**
		 * When an entry in the Response Entries View (Fragment) has been selected this method gets called with the unid of the selected item
		 */
		@Override
		public void onResponseViewItemSelected(String unid) {
			
			ApplicationLog.d(getClass().getSimpleName() + " got a unid: " + unid, shouldCommitToLog);
			
			DiscussionEntry selectedEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(unid);
			
			if (selectedEntry == null) {
				ApplicationLog.w(getClass().getSimpleName() + " Unable to find the selected Discussion Entry - not showing anything new");
			} else {
				FrameLayout containerForReadDiscussionEntryFragment = (FrameLayout) findViewById(R.id.discussionEntryFragment);
				
				// If the fragment is visible - feed it the DiscussionEntry. If not - launch a new Activity with the unid of the DiscussionEntry
			    if (containerForReadDiscussionEntryFragment != null) {
			    	ApplicationLog.d(getClass().getSimpleName() + " fragment is in layout", shouldCommitToLog);

			    	// Instantiate a new fragment.
			    	ReadDiscussionEntryFragment newFragment = ReadDiscussionEntryFragment.newInstance(unid);
			        // Add the fragment to the activity, pushing this transaction
			        // on to the back stack.
			        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			        ft.replace(R.id.discussionEntryFragment, newFragment);
			        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			        ft.addToBackStack(null);
			        ft.commit();
			        ApplicationLog.d(getClass().getSimpleName() + "FragmentTransaction committed", shouldCommitToLog);
//			        newFragment.setDiscussionEntry(selectedEntry);
			    } else {
			    	ApplicationLog.d(getClass().getSimpleName() + " fragment is not layout. Launching new ReadDiscussionEntry2Activity", shouldCommitToLog);
			      Intent intent = new Intent(getApplicationContext(),
			          ReadDiscussionEntry2Activity.class);
			      intent.putExtra(ReadDiscussionEntry2Activity.EXTRA_URL, unid);
			      startActivity(intent);
			    }	
			}
			
		}


}
