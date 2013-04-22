package dk.bruntt.discussionwork2.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import dk.bruntt.discussionwork2.ApplicationLog;
import dk.bruntt.discussionwork2.R;
import dk.bruntt.discussionwork2.db.DatabaseManager;

public class InitialRightPaneFragment extends SherlockFragment  {

	private boolean shouldCommitToLog = false;
	Activity myActivity = null;


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = getActivity();
		shouldCommitToLog = getLogALot(myActivity);
        DatabaseManager.init(myActivity);

    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		ApplicationLog.d(getClass().getSimpleName() +  " onCreateView", shouldCommitToLog);
		DatabaseManager.init(myActivity);
		View view = inflater.inflate(R.layout.initial_right_pane_fragment, container, false);
		return view;
	}
	

	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
	
	

}
