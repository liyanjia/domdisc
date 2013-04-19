package dk.bruntt.discussionwork2;

import java.util.UUID;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;
import dk.bruntt.discussionwork2.model.DiscussionEntry;
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
//        deleteButton = (Button) contentView.findViewById(R.id.button_delete);

//        Button btn = (Button) contentView.findViewById(R.id.button_save);
//        setupSaveButton(btn);

        setContentView(contentView);
        
        setupDiscussionDatabase();
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
//	         NavUtils.navigateUpTo(this, new Intent(this, DiscussionEntriesViewActivity.class));
//	    	  NavUtils.navigateUpTo(this, new Intent(this, dk.bruntt.discussionwork2.ui.StartActivity.class));
	    	  NavUtils.navigateUpFromSameTask(this);
	         return true;
	      case R.id.menu_save:
	    	  
	    	  String subject = editSubject.getText().toString();
	    	  String body = editBody.getText().toString();
	    	  String categories = editCategories.getText().toString();
	    	  
	    	  createNewDiscussionEntry(subject, body, categories);
	    	  return true;
	   }
	   return super.onOptionsItemSelected(item);
	}	
	
	
	
	
	
	private void setupDiscussionDatabase() {
		Bundle bundle = getIntent().getExtras();
		if (null!=bundle && bundle.containsKey(Constants.keyDiscussionDatabaseId)) {
			int discussionDatabaseId = bundle.getInt(Constants.keyDiscussionDatabaseId);
	        discussionDatabase = DatabaseManager.getInstance().getDiscussionDatabaseWithId(discussionDatabaseId);	
		}
	}

//	private void setupDiscussionEntry() {
//		Bundle bundle = getIntent().getExtras();
//		if (null!=bundle && bundle.containsKey(Constants.keyDiscussionEntryId)) {
//			String discussionEntryId = bundle.getString(Constants.keyDiscussionEntryId);
//			discussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(discussionEntryId);
//			editSubject.setText(discussionEntry.getSubject());
//			editBody.setText(discussionEntry.getBody());
//			deleteButton.setVisibility(View.VISIBLE);
//			setupDeleteButton();
//		} else {
//			deleteButton.setVisibility(View.INVISIBLE);
//		}
//	}

//	private void setupSaveButton(Button btn) {
//		final Activity activity = this;
//		btn.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				String name = editSubject.getText().toString();
//				String description = editBody.getText().toString();
//				boolean isValid = notEmpty(name) && notEmpty(description);
//				if (isValid) {
//					if (null==discussionEntry) {
//						createNewDiscussionEntry(name,description);
//					} else {
//						updateWishItem(name,description);
//					}
//					finish();
//				} else {
//					new AlertDialog.Builder(activity)
//					.setTitle("Error")
//					.setMessage("All fields must be filled")
//					.setNegativeButton("OK", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					})
//					.show();
//				}
//			}
//		});
//	}
	
//	private void setupDeleteButton() {
//		if (null!=deleteButton) {
//			final Activity activity = this;
//			deleteButton.setOnClickListener(new OnClickListener() {
//				public void onClick(View v) {
//					new AlertDialog.Builder(activity)
//					.setTitle("Warning")
//					.setMessage("Are you sure you would like to delete wish '"+discussionEntry.getSubject()+"'?")
//					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							DatabaseManager.getInstance().deleteDiscussionEntry(discussionEntry);
//							dialog.dismiss();
//							activity.finish();
//						}
//					})
//					.setNegativeButton("No", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					})
//					.show();
//				}
//			});
//		}
//	}

//	protected void updateWishItem(String subject, String body) {
//		discussionEntry.setSubject(subject);
//		discussionEntry.setBody(body);
//		DatabaseManager.getInstance().updateDiscussionEntry(discussionEntry);
//	}

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
			UUID uuid = UUID.randomUUID();
			discussionEntry.setUnid(String.valueOf(uuid));
			ApplicationLog.d(getClass().getSimpleName() + " unid: " + String.valueOf(uuid), shouldCommitToLog);
			
//			DatabaseManager.getInstance().updateDiscussionEntry(discussionEntry);
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