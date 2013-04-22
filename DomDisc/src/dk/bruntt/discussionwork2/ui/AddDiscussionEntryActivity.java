package dk.bruntt.discussionwork2.ui;

import java.util.UUID;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import dk.bruntt.discussionwork2.R.id;
import dk.bruntt.discussionwork2.R.layout;
import dk.bruntt.discussionwork2.R.menu;
import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;
import dk.bruntt.discussionwork2.model.DiscussionEntry;
import dk.bruntt.discussionwork2.ApplicationLog;
import dk.bruntt.discussionwork2.Constants;
import dk.bruntt.discussionwork2.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class AddDiscussionEntryActivity extends SherlockActivity {
	private EditText editSubject;
	private EditText editBody;
	private EditText editCategories;
	private DiscussionDatabase discussionDatabase;
	private DiscussionEntry parentDiscussionEntry;
	private boolean shouldCommitToLog = false;
//	private DiscussionEntry discussionEntry;
//	private Button deleteButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shouldCommitToLog = getLogALot(this);
		DatabaseManager.init(getApplicationContext());
        ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.add_discussion_entry, null);
        editSubject = (EditText) contentView.findViewById(R.id.edit_subject);
        editBody = (EditText) contentView.findViewById(R.id.edit_body);
        editCategories = (EditText) contentView.findViewById(R.id.edit_categories);

        setContentView(contentView);
        
        setupDiscussionDatabaseAndParent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String title = "";
        if (parentDiscussionEntry != null) {
        	title = "New response to" + parentDiscussionEntry.getSubject();
        } else {
        	title = "New discussion thread";
        }
        getSupportActionBar().setTitle(title);
//        setupDiscussionEntry();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.activity_add_discussion_entry, menu);
       return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	      case android.R.id.home:
//	    	  NavUtils.navigateUpFromSameTask(this);
	    	  finish(); // stops  this Activity
	         return true;
	      case R.id.menu_save:
	    	  
	    	  String subject = editSubject.getText().toString();
	    	  String body = editBody.getText().toString();
	    	  String categories = editCategories.getText().toString();
	    	  
	    	  createNewDiscussionEntry(subject, body, categories);
	    	  finish(); // stops  this Activity
	    	  return true;
	   }
	   return super.onOptionsItemSelected(item);
	}	
	
	
	


	private void setupDiscussionDatabaseAndParent() {
		Bundle bundle = getIntent().getExtras();
		if (null!=bundle) {
			if (bundle.containsKey(Constants.keyDiscussionDatabaseId)) {
				int discussionDatabaseId = bundle.getInt(Constants.keyDiscussionDatabaseId);
				discussionDatabase = DatabaseManager.getInstance().getDiscussionDatabaseWithId(discussionDatabaseId);
			}
			if (bundle.containsKey(Constants.keyDiscussionEntryId)) {
				String discussionEntryId = bundle.getString(Constants.keyDiscussionEntryId);
				parentDiscussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(discussionEntryId);
			}
		}
		
//		Same as above for the parentID
		
	}

	boolean notEmpty(String s) {
		return null!=s && s.length()>0;
	}

	private void createNewDiscussionEntry(String subject,String body, String categories) {
		if (null!=discussionDatabase) {
			DiscussionEntry discussionEntry = new DiscussionEntry();
			discussionEntry.setSubject(subject);
			discussionEntry.setBody(body);
			discussionEntry.setCategories(categories);
			discussionEntry.setDiscussionDatabase(discussionDatabase);
			if (parentDiscussionEntry != null) {
				discussionEntry.setParentid(parentDiscussionEntry.getParentid());
			}
			UUID uuid = UUID.randomUUID();
			discussionEntry.setUnid(String.valueOf(uuid));
			ApplicationLog.d(getClass().getSimpleName() + " unid: " + String.valueOf(uuid), shouldCommitToLog);
			
			DatabaseManager.getInstance().createDiscussionEntry(discussionEntry);
			
		} else {
			ApplicationLog.w(getClass().getSimpleName() + " No discussionDatabase available for saving the entry");
		}
	}
	
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
}