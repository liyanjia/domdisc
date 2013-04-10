package dk.bruntt.discussionwork2.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;


public class ReadDiscussionEntry2Activity extends SherlockActivity {
	
	public static final String EXTRA_URL = "unid";
	
	 @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Need to check if Activity has been switched to landscape mode
	    // If yes, finished and go back to the start Activity
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	      finish();
	      return;
	    }
	    
	    /**
	     * Used if used in a context without the discussion main view
	     * Most of the code in ReadDiscussionEntryFragment should be used here too
	     */
//	    setContentView(R.layout.activity_detail);
//	    Bundle extras = getIntent().getExtras();
//	    if (extras != null) {
//	      String s = extras.getString(EXTRA_URL);
//	      TextView view = (TextView) findViewById(R.id.detailsText);
//	      view.setText(s);
//	    }
	  }

}
