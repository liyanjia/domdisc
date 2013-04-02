package dk.bruntt.discussionwork2.db;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;

import dk.bruntt.discussionwork2.model.AppLog;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;
import dk.bruntt.discussionwork2.model.DiscussionEntry;

import android.content.Context;
import android.text.GetChars;
import android.util.Log;


public class DatabaseManager {

	static private DatabaseManager instance;

	static public void init(Context ctx) {
		if (null==instance) {
			instance = new DatabaseManager(ctx);
		}
	}

	static public DatabaseManager getInstance() {
		if (null == instance) {
			Log.d("DatabaseManager", "instance not initialized");
		}
		return instance;
	}

	private DatabaseHelper helper;
	private DatabaseManager(Context ctx) {
		helper = new DatabaseHelper(ctx);
	}

	private DatabaseHelper getHelper() {
		return helper;
	}

	public List<DiscussionDatabase> getAllDiscussionDatabases() {
//		Log.d("debug", "getAllDiscussionDatabases 1");
		List<DiscussionDatabase> DiscussionDatabases = null;
		try {
//			Log.d("debug", "getAllDiscussionDatabases 2");
			DiscussionDatabases = getHelper().getDiscussionDatabaseDao().queryForAll();
//			Log.d("debug", "getAllDiscussionDatabases 3");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return DiscussionDatabases;
	}

	public void addDiscussionDatabase(DiscussionDatabase l) {
		try {
			getHelper().getDiscussionDatabaseDao().create(l);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public DiscussionDatabase getDiscussionDatabaseWithId(int DiscussionDatabaseId) {
		DiscussionDatabase DiscussionDatabase = null;
		try {
			DiscussionDatabase = getHelper().getDiscussionDatabaseDao().queryForId(DiscussionDatabaseId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return DiscussionDatabase;
	}
	

//	public DiscussionEntry getDiscussionEntryWithId(int discussionEntryId) {
//		DiscussionEntry discussionEntry = null;
//		try {
//			discussionEntry = getHelper().getDiscussionEntryDao().queryForId(discussionEntryId);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return discussionEntry;
//	}
	
	public DiscussionEntry getDiscussionEntryWithId(String unid) {
		DiscussionEntry discussionEntry = null;
		try {
			discussionEntry = getHelper().getDiscussionEntryDao().queryForId(unid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return discussionEntry;
	}
	
	public DiscussionEntry getDiscussionEntryWithUnid(String unid) {
		//skal laves færdig
		DiscussionEntry discussionEntry = null;
//		try {
////			discussionEntry = getHelper().getDiscussionEntryDao().queryForFieldValues(arg0);
//			getHelper().getDiscussionEntryDao().queryfor
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		return discussionEntry;
	}

	public DiscussionEntry newDiscussionEntry() {
		DiscussionEntry discussionEntry = new DiscussionEntry();
		try {
			getHelper().getDiscussionEntryDao().create(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return discussionEntry;
	}
	
	public void createDiscussionEntry(DiscussionEntry discussionEntry) {
//		DiscussionEntry discussionEntry = new DiscussionEntry();
		try {
			getHelper().getDiscussionEntryDao().create(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}
//		return discussionEntry;
	}

	public void deleteDiscussionDatabase(DiscussionDatabase DiscussionDatabase) {
		try {
			getHelper().getDiscussionDatabaseDao().delete(DiscussionDatabase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteDiscussionEntry(DiscussionEntry discussionEntry) {
		try {
			getHelper().getDiscussionEntryDao().delete(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	public void updateDiscussionEntry(DiscussionEntry discussionEntry) {
		try {
			getHelper().getDiscussionEntryDao().update(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void refreshDiscussionDatabase(DiscussionDatabase DiscussionDatabase) {
		try {
			getHelper().getDiscussionDatabaseDao().refresh(DiscussionDatabase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateDiscussionDatabase(DiscussionDatabase DiscussionDatabase) {
		try {
			getHelper().getDiscussionDatabaseDao().update(DiscussionDatabase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/*
	 * ApplicationLog
	 * 
	 */


	public List<AppLog> getAllAppLogs() {
		List<AppLog> appLogs = null;
		try {
			appLogs = getHelper().getAppLogDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return appLogs;
	}

	public void addAppLog(AppLog l) {
		try {
			getHelper().getAppLogDao().create(l);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AppLog getAppLogWithId(int appLogId) {
		AppLog appLog = null;
		try {
			appLog = getHelper().getAppLogDao().queryForId(appLogId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return appLog;
	}
	
	public void deleteAppLog(AppLog appLog) {
		try {
			getHelper().getAppLogDao().delete(appLog);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void emptyAppLogOld() {
		try {
			Dao appLogDao =  getHelper().getAppLogDao();
			List<AppLog> allAppLogs = appLogDao.queryForAll();
			appLogDao.delete(allAppLogs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void emptyAppLog() {
		
			Dao appLogDao =  getHelper().getAppLogDao();
//			UpdateBuilder<AppLog, String> updateBuilder = appLogDao.updateBuilder();
			DeleteBuilder<AppLog, String> deleteBuilder = appLogDao.deleteBuilder();
			
			
			
			try {
				deleteBuilder.where().isNotNull(AppLog.ID_FIELD_NAME);
				deleteBuilder.delete();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}
	

}