package dk.bruntt.discussionwork2.comms;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UserSessionTools {
	
	public static boolean haveInternet(Context ctx) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return true;
		}
		return true;
	}

}
