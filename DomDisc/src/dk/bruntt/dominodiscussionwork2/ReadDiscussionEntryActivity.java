package dk.bruntt.dominodiscussionwork2;

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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;
import dk.bruntt.discussionwork2.model.DiscussionEntry;


public class ReadDiscussionEntryActivity extends SherlockActivity {
	private EditText editName;
	private EditText editDescription;
	private DiscussionDatabase discussionDatabase;
	private DiscussionEntry discussionEntry;
//	private Button deleteButton;
	private TextView bodyView;
	private WebView webView;
	private TextView subjectView;
	private boolean shouldLogALot = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shouldLogALot = getLogALot(this);
        ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.read_discussion_entry, null);
//        editName = (EditText) contentView.findViewById(R.id.edit_name);
//        editDescription = (EditText) contentView.findViewById(R.id.edit_description);
        
        bodyView = (EditText) contentView.findViewById(R.id.bodytext);
        subjectView = (TextView) contentView.findViewById(R.id.name);
        webView = (WebView) contentView.findViewById(R.id.bodyhtml);
//        deleteButton = (Button) contentView.findViewById(R.id.button_delete);
//        Button btn = (Button) contentView.findViewById(R.id.button_save);
//        setupSaveButton(btn);

        setContentView(contentView);
        
        setupDiscussionDatabase();
        setupDiscussionEntry();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, DiscussionEntriesViewActivity.class));
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

	private void setupDiscussionEntry() {
		Bundle bundle = getIntent().getExtras();
		if (null!=bundle && bundle.containsKey(Constants.keyDiscussionEntryId)) {
			String discussionEntryId = bundle.getString(Constants.keyDiscussionEntryId);
			discussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(discussionEntryId);
			subjectView.setText(discussionEntry.getSubject());

			String bodyHtml = discussionEntry.getBody() ;
//			Log.d(getClass().getSimpleName(), "bodyHtml: " + bodyHtml);
//			ApplicationLog.d("bodyHtml: " + bodyHtml, shouldLogALot);
			
			// UTF-8 to make national characters look correct
			webView.loadDataWithBaseURL(null, bodyHtml, "text/html", "UTF-8", null);

			//			webView.loadData(bodyHtml, "text/html", "ISO-8859-1");
//			bodyView.setText(discussionEntry.getBody());
//			deleteButton.setVisibility(View.VISIBLE);
//			setupDeleteButton();
		} else {
//			deleteButton.setVisibility(View.INVISIBLE);
		}
	}

	private void setupSaveButton(Button btn) {
		final Activity activity = this;
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String name = editName.getText().toString();
				String description = editDescription.getText().toString();
				boolean isValid = notEmpty(name) && notEmpty(description);
				if (isValid) {
					if (null==discussionEntry) {
						createNewDiscussionEntry(name,description);
					} else {
						updateWishItem(name,description);
					}
					finish();
				} else {
					new AlertDialog.Builder(activity)
					.setTitle("Error")
					.setMessage("All fields must be filled")
					.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.show();
				}
			}
		});
	}
	
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

	protected void updateWishItem(String subject, String body) {
		discussionEntry.setSubject(subject);
		discussionEntry.setBody(body);
		DatabaseManager.getInstance().updateDiscussionEntry(discussionEntry);
	}

	boolean notEmpty(String s) {
		return null!=s && s.length()>0;
	}

	private void createNewDiscussionEntry(String subject,String body) {
		if (null!=discussionDatabase) {
			DiscussionEntry discussionEntry = DatabaseManager.getInstance().newDiscussionEntry();
			discussionEntry.setSubject(subject);
			discussionEntry.setBody(body);
			discussionEntry.setDiscussionDatabase(discussionDatabase);
			DatabaseManager.getInstance().updateDiscussionEntry(discussionEntry);
		}
	}
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
}