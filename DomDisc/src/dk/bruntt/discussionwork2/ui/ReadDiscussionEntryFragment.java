package dk.bruntt.discussionwork2.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import dk.bruntt.discussionwork2.ApplicationLog;
import dk.bruntt.discussionwork2.Constants;
import dk.bruntt.discussionwork2.ReadDiscussionEntryActivity;
import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionEntry;
import dk.bruntt.discussionwork2.ui.DiscussionMainEntriesViewFragment.OnItemSelectedListener;
import dk.bruntt.discussionwork2.R;

public class ReadDiscussionEntryFragment extends SherlockFragment implements OnClickListener {

	private DiscussionEntry currentDiscussionEntry = null;
	private String currentUnid = "";
	private boolean shouldCommitToLog = false;
	Activity myActivity = null;


	private TextView subjectView;
	private TextView authorView;
	private WebView webView;
	private ListView responseView;
	private Button toggleBodyResponsesVisible;
	
	ArrayAdapter<String> adapter = null;
	
	//Default is to display the body and not the responses
	private boolean showBody = true;
	
	private OnResponseItemSelectedListener listener;


    /**
     * Create a new instance of ReadDiscussionEntryFragment that will be initialized
     * with the given argument that points at a document to display.
     */
    static ReadDiscussionEntryFragment newInstance(CharSequence unid) {
    	ReadDiscussionEntryFragment f = new ReadDiscussionEntryFragment();
        Bundle b = new Bundle();
        b.putCharSequence("unid", unid);
        f.setArguments(b);
        return f;
    }
    
    /**
     * During creation, if arguments have been supplied to the fragment
     * then parse those out.
     */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = getActivity();
		shouldCommitToLog = getLogALot(myActivity);
        DatabaseManager.init(myActivity);

        Bundle args = getArguments();
        if (args != null) {
            CharSequence unid = args.getCharSequence("unid");
            if (unid != null) {
            	ApplicationLog.d(getClass().getSimpleName() + " got a unid: " + unid, shouldCommitToLog);
                currentUnid = unid.toString();
            }
        }
    }

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ApplicationLog.d(getClass().getSimpleName() +  " onCreateView", shouldCommitToLog);
		DatabaseManager.init(myActivity);
		View view = inflater.inflate(R.layout.read_discussion_entry_with_children, container, false);
		
		subjectView = (TextView) view.findViewById(R.id.subject);
		authorView = (TextView) view.findViewById(R.id.author);
		toggleBodyResponsesVisible = (Button) view.findViewById(R.id.toggle_body_responses);
		toggleBodyResponsesVisible.setOnClickListener(this);
		
		webView = (WebView) view.findViewById(R.id.bodyhtml);
//		webView.setVisibility(View.GONE);
		responseView = (ListView) view.findViewById(R.id.responsesview);
		
		//If we were fed a unid from a Bundle, we will proceed and load the Document and show it
		if (currentUnid != null) {
			DatabaseManager.init(myActivity);
			currentDiscussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(currentUnid);
			if (currentDiscussionEntry != null) {
				setDiscussionEntry(currentDiscussionEntry);
			}
		}
		
		return view;
	}
	
	@Override
	public void onClick(View v){
		
		//If this is the toggle button we will go on and toggle - checking because we might add other clickables
		if (v.getId() == toggleBodyResponsesVisible.getId()) {
			toggleShowBodyResponses();			
		}
		

	}

	private void toggleShowBodyResponses() {
		if (showBody == true) {
			showBody = false;
		} else {
			showBody = true;
		}
		enforceBodyResponsesVisibility();
	}

	private void enforceBodyResponsesVisibility() {
		if (showBody == true) {
			webView.setVisibility(View.VISIBLE);
			responseView.setVisibility(View.GONE);
			toggleBodyResponsesVisible.setText(R.string.toggle_body_responses_button_body_visible);
		} else {
			webView.setVisibility(View.GONE);
			responseView.setVisibility(View.VISIBLE);
			toggleBodyResponsesVisible.setText(R.string.toggle_body_responses_button_responses_visible); 
		}
	}

	/**
	 * Will display the discussionEntry and its children
	 * @param discussionEntry
	 */
	private void setDiscussionEntry(DiscussionEntry discussionEntry) {
		
		if (discussionEntry == null)  {
			ApplicationLog.w(getClass().getSimpleName() +  " setDiscussionEntry: No discussionEntry to show");
		} else {
			ApplicationLog.d(getClass().getSimpleName() +  " Showing dicsussionentry " + discussionEntry.getSubject(), shouldCommitToLog);
			currentDiscussionEntry = discussionEntry;
			populateHeader();
			populateBody();
			populateFooter();	
			showBody = true; //Default is to show the body text
			enforceBodyResponsesVisibility();
		}
		
		// Show UP ?
		//SHow Action buttons?
	}

	private void populateFooter() {
		// TODO Auto-generated method stub

//		Populate Response View
		ApplicationLog.d(getClass().getSimpleName() +  " building footer ", shouldCommitToLog);
		
		final List<DiscussionEntry> responseEntries = DatabaseManager.getInstance().getResponseDicussionEntries(currentDiscussionEntry);
		
		if (responseEntries == null || responseEntries.size() == 0	) {
			ApplicationLog.d(getClass().getSimpleName() + " No responses. Will not display any", shouldCommitToLog);
			if (adapter != null) {
				adapter.clear();
				adapter.notifyDataSetInvalidated();	
			}
		} else {
			ApplicationLog.d(getClass().getSimpleName() + " number of responses: " + responseEntries.size(), shouldCommitToLog);
			
			if (responseEntries.size() > 0) {
				List<String> titles = new ArrayList<String>();
				for (DiscussionEntry responseEntry : responseEntries) {
					titles.add(responseEntry.getSubject());
				}
				adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, titles);
				responseView.setAdapter(adapter);
				
				/**
				 * When clicked activate the OnViewItemSelected method with the unid. The enclosing Activity will have to handle
				 * what happens next
				 */
				responseView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
						DiscussionEntry item = responseEntries.get(position);
						listener.onResponseViewItemSelected(item.getUnid());
					}
				});
				
				
//				final Activity activity = getActivity();
//				responseView.setOnItemClickListener(new OnItemClickListener() {
//					public void onItemClick(AdapterView<?> parent, View view,
//							int position, long id) {
//						DiscussionEntry item = responseEntries.get(position);
//						setDiscussionEntry(item);
////						Intent intent = new Intent(activity,
////								ReadDiscussionEntryActivity.class);
////						intent.putExtra(Constants.keyDiscussionEntryId,
////								item.getUnid());
////						startActivity(intent);
//						
////						ReadDiscussionEntryFragment newFragment = new ReadDiscussionEntryFragment(); 
////						FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
////				        ft.replace(R.id.responsesview, newFragment);
////				        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
////				        ft.addToBackStack(null);
////				        ft.commit();
//						
//						
//					}
//				});
				
			}
		}
		
		
		
		
		
		
		
	}
	
	

	private void populateBody() {
		ApplicationLog.d(getClass().getSimpleName() +  " building body", shouldCommitToLog);
		String bodyHtml = currentDiscussionEntry.getBody() ;
		webView.loadDataWithBaseURL(null, bodyHtml, "text/html", "UTF-8", null);
	}

	private void populateHeader() {
		ApplicationLog.d(getClass().getSimpleName() +  " building header", shouldCommitToLog);
		String subject = currentDiscussionEntry.getSubject();
		String author = currentDiscussionEntry.getAuthors();
		subjectView.setText(subject);
		authorView.setText(author);
	}


	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
	
	
	/**
	 * Forcing Activities that use this Class to implement this interface
	 * @author Jens
	 *
	 */
	public interface OnResponseItemSelectedListener {
		public void onResponseViewItemSelected(String unid);
	}
	
	/**
	 * Hooks the listener to the enclosing Activity
	 */	
	 @Override
	    public void onAttach(Activity activity) {
	      super.onAttach(activity);
	      if (activity instanceof OnResponseItemSelectedListener) {
	        listener = (OnResponseItemSelectedListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implement ReadDiscussionEntryFragment.OnResponseItemSelectedListener");
	      }
	    }

}
