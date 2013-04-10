package dk.bruntt.discussionwork2.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;

import dk.bruntt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;
import dk.bruntt.discussionwork2.model.DiscussionEntry;
import dk.bruntt.discussionwork2.R;

public class DiscussionMainEntriesViewFragment extends SherlockFragment {


	private OnItemSelectedListener listener;
	ListView listView;
	private DiscussionDatabase currentDiscussionDatabase = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DatabaseManager.init(getActivity());
		View view = inflater.inflate(R.layout.discussion_entry_list, container, false);
		listView = (ListView) view.findViewById(R.id.list_view);
		return view;
	}
	
	/**
	 * Hooks the listener to the enclosing Activity
	 */	
	 @Override
	    public void onAttach(Activity activity) {
	      super.onAttach(activity);
	      if (activity instanceof OnItemSelectedListener) {
	        listener = (OnItemSelectedListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implement DiscussionMainEntriesViewFragment.OnItemSelectedListener");
	      }
	    }
	
	
	public void setDiscussionDatabase(DiscussionDatabase discussionDatabase) {
		currentDiscussionDatabase = discussionDatabase;
		populateListView();
		
	}

	private void populateListView() {
		if (null != currentDiscussionDatabase) {
			final List<DiscussionEntry> discussionEntries = currentDiscussionDatabase.getDiscussionEntries();
			List<String> titles = new ArrayList<String>();
			for (DiscussionEntry discussionEntry : discussionEntries) {
				titles.add(discussionEntry.getSubject());
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, titles);
			listView.setAdapter(adapter);
			
			/**
			 * When clicked activate the OnViewItemSelected method with the unid. The enclosing Activity will have to handle
			 * what happens next
			 */
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					DiscussionEntry item = discussionEntries.get(position);
					listener.onViewItemSelected(item.getUnid());
				}
			});
		}
	}

	public interface OnItemSelectedListener {
		public void onViewItemSelected(String unid);
	}


}
