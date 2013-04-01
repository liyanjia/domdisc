package dk.bruntt.dominodiscussionwork2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import dk.brunt.discussionwork2.db.DatabaseManager;
import dk.bruntt.discussionwork2.comms.UserSessionTools;
import dk.bruntt.discussionwork2.model.DiscussionDatabase;
import dk.bruntt.discussionwork2.model.DiscussionEntry;

/**
 * Classe for replicating Domino Discussion databases
 */
public class DiscussionReplicator {

	Context context;
	private boolean shouldLogALot = false;
	private static final String loginPath = "/names.nsf?Login";
	private String authenticationCookie = "";

	public DiscussionReplicator(Context context) {
		super();
		this.context = context;
		shouldLogALot = getLogALot(context);
	}

	/**
	 * Activate to replicate all known Discussion Databases
	 */
	public void replicateDiscussionDatabases() {

	}

	/**
	 * Activate to replicate one Discussion database
	 */
	public void replicateDiscussionDatabase(
			DiscussionDatabase discussionDatabase) {
		DatabaseManager.init(context);
		ApplicationLog.i("Replicate " + discussionDatabase.getName());

		if (UserSessionTools.haveInternet(context) == false) {
			Log.i(getClass().getSimpleName(),
					"Internet connection not available - Replication not possible");
			ApplicationLog
			.i("Internet connection not available - Replication not possible");			
		} else {
			Log.d(getClass().getSimpleName(),
					"Internet connection is available");
			Log.d(getClass().getSimpleName(), "Will replicate");

			ApplicationLog.d(
					"Internet connection is available- Will replicate",
					shouldLogALot);

			String hostName = discussionDatabase.getHostName();
			String dbPath = discussionDatabase.getDbPath();
			String httpPort = discussionDatabase.getHttpPort();
			String password = discussionDatabase.getPassword();
			String userName = discussionDatabase.getUserName();
			String httpType = "";
			if (discussionDatabase.isUseSSL()) {
				httpType = "https";
			} else {
				httpType = "http";
			}
			String urlForDocuments = httpType + "://" + hostName;

			// Flere portnumre her
			if (httpPort.contentEquals("80") || httpPort.contentEquals("")) {
				urlForDocuments = urlForDocuments + dbPath
						+ "/api/data/documents/";
			} else {
				urlForDocuments = urlForDocuments + ":" + httpPort + dbPath
						+ "/api/data/documents/";
			}

			Log.d(getClass().getSimpleName(), "Starting");

			ApplicationLog.d("Starting", shouldLogALot);

			// getAuthenticationToken
			ApplicationLog.d("Activating getAuthenticationToken now",
					shouldLogALot);
			authenticationCookie = getAuthenticationToken(hostName, httpPort,
					userName, password, discussionDatabase.isUseSSL(),
					urlForDocuments);

			if (authenticationCookie.equals("")){
				ApplicationLog.w("Unable to start replication as Authentication with the server was not established. Stopping.");
			} else {
				// String url =
				// "http://www.jens.bruntt.dk/androiddev/discussi.nsf/api/data/documents/";
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(
						new StringHttpMessageConverter());

				// Add the gzip Accept-Encoding header
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);

				ApplicationLog.d("Setting LtpaToken in request header",	shouldLogALot);
				requestHeaders.add("Cookie", authenticationCookie);

				requestHeaders.add("Referer", urlForDocuments);
				//			requestHeaders
				//			.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
				requestHeaders.setCacheControl("max-age=0");

				HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

				Log.d(getClass().getSimpleName(), "HTTP connection now");
				ApplicationLog.d("HTTP connection now", shouldLogALot);

				String jsonString;
				try {
					Log.i(getClass().getSimpleName(), "Accesing " + urlForDocuments);
					ApplicationLog.i("Accesing " + urlForDocuments);

					// Make the HTTP GET request, marshaling the response to a
					// String
					ResponseEntity<String> response = restTemplate.exchange(
							urlForDocuments, HttpMethod.GET, requestEntity,
							String.class);
					jsonString = response.getBody();

					Log.d(getClass().getSimpleName(), "String received length: "
							+ jsonString.length());

					ApplicationLog.d(
							"String received length: " + jsonString.length(),
							shouldLogALot);

					// Log.d(getClass().getSimpleName(), jsonString);

					if (!isThisALoginForm(jsonString)) {
						try {
							JSONArray jsonArray = new JSONArray(jsonString);

							Log.d(getClass().getSimpleName(), "Number of entries "
									+ jsonArray.length());

							ApplicationLog.d(
									"Number of entries " + jsonArray.length(),
									shouldLogALot);

							if (jsonArray.length() > 0) {
								handleJsonDiscussionEntries(jsonArray,
										discussionDatabase);

							} else {
								Log.i(getClass().getSimpleName(),
										"No entries retrieved. Nothing to do");
								ApplicationLog
								.i("No entries retrieved. Nothing to do");
							}

						} catch (Exception e) {
							// e.printStackTrace();
							Log.e(getClass().getSimpleName(),
									"getmessage: " + e.getMessage());
							ApplicationLog.e("Exception" + e.getMessage());
						}

					} else {
						Log.e(getClass().getSimpleName(),
								"There is a login issue when accessing "
										+ urlForDocuments);
						Log.e(getClass().getSimpleName(), "The server at "
								+ hostName + " prompts for login");
						ApplicationLog.e("There is a login issue when accessing "
								+ urlForDocuments);
						ApplicationLog.e("The server at " + hostName
								+ " prompts for login");

					}

				} catch (RestClientException e1) {
					// e1.printStackTrace();
					String errorMessage = e1.getMessage();
					if (errorMessage == null) {
						errorMessage = "Error message not available";
					}
					Log.e(getClass().getSimpleName(), "getmessage: " + errorMessage);
					ApplicationLog.e("Exception: " + errorMessage);

					if (errorMessage.contains("403")) {
						Log.i(getClass().getSimpleName(),
								"403 - Looks like the Domino Data Service is not enabled for the database "
										+ discussionDatabase.getDbPath());
						ApplicationLog
						.i("403 - Looks like the Domino Data Service is not enabled for the database "
								+ discussionDatabase.getDbPath());
					}

					else if (errorMessage.contains("404")) {
						String myErrorMessage = "404 - Looks like the Domino Database-path is wrong - typing error in the Configuration? ";
						Log.i(getClass().getSimpleName(), myErrorMessage
								+ discussionDatabase.getDbPath());
						ApplicationLog.i(myErrorMessage + " "
								+ discussionDatabase.getDbPath());
					}

					else {
						String localizedErrorMessage = e1.getLocalizedMessage();
						if (localizedErrorMessage != null) {
							Log.e(getClass().getSimpleName(),
									"localizedErrorMessage: "
											+ localizedErrorMessage);
							ApplicationLog.e("localizedErrorMessage: "
									+ localizedErrorMessage);
						} else {
							Log.e(getClass().getSimpleName(),
									"Unable to get data from the database "
											+ discussionDatabase.getDbPath());
							ApplicationLog
							.e("Unable to get data from the database "
									+ discussionDatabase.getDbPath());
						}
					}
					e1.printStackTrace();

				} catch (Exception e2) {
					String message = e2.getMessage();
					if (message == null) {
						message = "Exception with no message";
					}
					Log.e(getClass().getSimpleName(), message);
					e2.printStackTrace();
				}
			}
		}
	}


	/**
	 * If String contains FORM tag and HTML tag - this is a Domino login form
	 * 
	 * @param checkString
	 * @return true if this is a login form
	 */
	private boolean isThisALoginForm(String checkString) {

		boolean returnValue = false;
		if (checkString.contains("<form") || checkString.contains("<FORM")) {
			if (checkString.contains("html") || checkString.contains("HTML")) {
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Feed in a jsonArray from the /api/data/documents/ output and a
	 * DiscussionDatabase this method will make sure that the database is
	 * updated
	 * 
	 * @param discussionArray
	 * @param discussionDatabase
	 */
	private void handleJsonDiscussionEntries(JSONArray discussionArray,
			DiscussionDatabase discussionDatabase) {

		ArrayList<DiscussionEntry> serverDiscussionEntryList = new ArrayList<DiscussionEntry>();

		for (int i = 0; i < discussionArray.length(); i++) {
			JSONObject jsonObject;
			try {
				jsonObject = discussionArray.getJSONObject(i);
				try {

					String modified = jsonObject.getString("@modified");
					String unid = jsonObject.getString("@unid");
					String href = jsonObject.getString("@href");

					// Log.d(getClass().getSimpleName(), "- ENTRY -");
					// Log.d(getClass().getSimpleName(), "modified: " +
					// modified);
					Log.d(getClass().getSimpleName(), "entry with unid: "
							+ unid);

					ApplicationLog.d("entry with unid: " + unid, shouldLogALot);

					// Log.d(getClass().getSimpleName(), "href: " + href);

					DiscussionEntry discussionEntry = new DiscussionEntry();
					// Log.d(getClass().getSimpleName(), "setHrefhref");
					discussionEntry.setHref(href);
					// Log.d(getClass().getSimpleName(), "setUnid");
					discussionEntry.setUnid(unid);
					// Log.d(getClass().getSimpleName(), "setModified");
					discussionEntry.setModified(modified);
					// Log.d(getClass().getSimpleName(), "add to list");
					serverDiscussionEntryList.add(discussionEntry);
					// Log.d(getClass().getSimpleName(), "add to list done");
				} catch (JSONException e) {
					Log.e(getClass().getSimpleName(),
							"Error while accessing JSON object values");
					ApplicationLog
					.e("Error while accessing JSON object values");
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} // indsat slut her

		/*
		 * Check all retrieved DiscussionDatabaseEntries - do they exist in
		 * database if no: retrieve content and add if yes: check if modified is
		 * changed if no: next if yes: retrieve newer content and update
		 */
		if ((serverDiscussionEntryList == null)
				|| (serverDiscussionEntryList.isEmpty())) {
			Log.i(getClass().getSimpleName(),
					"The JSON retrievede did not contain any documents");
			ApplicationLog
			.i("The JSON retrievede did not contain any documents");
		} else {
			Log.i(getClass().getSimpleName(),
					"Checking all downloaded entries: are they already stored locally?");

			ApplicationLog
			.d("Checking all downloaded entries: are they already stored locally?",
					shouldLogALot);

			Iterator<DiscussionEntry> serverDiscussionEntryListIterator = serverDiscussionEntryList
					.iterator();
			while (serverDiscussionEntryListIterator.hasNext()) {
				DiscussionEntry currentEntry = serverDiscussionEntryListIterator
						.next();
				String unid = currentEntry.getUnid();
				// Check if the entry is already in the database
				// Her er jeg
				Log.d(getClass().getSimpleName(), "Lookup for unid " + unid);

				ApplicationLog.d("Lookup for unid: " + unid, shouldLogALot);

				DiscussionEntry dbEntry = DatabaseManager.getInstance()
						.getDiscussionEntryWithId(unid);

				if (dbEntry == null) {
					Log.d(getClass().getSimpleName(),
							"This entry has not been stored before - creating newDiscussionEntry");

					ApplicationLog
					.d("This entry has not been stored before - creating newDiscussionEntry",
							shouldLogALot);

					currentEntry.setDiscussionDatabase(discussionDatabase);
					DiscussionEntry fullDiscussionEntry = getFullEntryFromServer(currentEntry);
					DatabaseManager.getInstance().createDiscussionEntry(
							fullDiscussionEntry);
					Log.d(getClass().getSimpleName(),
							"This entry has been stored with values: "
									+ fullDiscussionEntry.getSubject());

					ApplicationLog.d("This entry has been stored with values: "
							+ fullDiscussionEntry.getSubject(), shouldLogALot);

				} else {
					Log.d(getClass().getSimpleName(),
							"This entry is already in the database: "
									+ dbEntry.getSubject());

					ApplicationLog.d("This entry is already in the database: "
							+ dbEntry.getSubject(), shouldLogALot);
					ApplicationLog.d("Checking if modified dates are the same", shouldLogALot);
					
					String currentEntryModified = currentEntry.getModified();
					String dbEntryModified = dbEntry.getModified();
					if (currentEntryModified.contentEquals(dbEntryModified)) {
						ApplicationLog.d("Modified date is unchanged", shouldLogALot);
					} else {
						ApplicationLog.d("Modified date is changed. Updating dbEntry", shouldLogALot);
						DiscussionEntry fullDiscussionEntry = getFullEntryFromServer(currentEntry);
						fullDiscussionEntry.setDiscussionDatabase(discussionDatabase);
						dbEntry = fullDiscussionEntry;
						DatabaseManager.getInstance().updateDiscussionEntry(dbEntry);
					}
				}

			}

		}

	}

	// Fjernet slut her

	/**
	 * Retrieve a full entry from the server
	 * 
	 * @param discussionEntry
	 * @param discussionDatabase
	 * @return discussionDatabase
	 */
	private DiscussionEntry getFullEntryFromServer(
			DiscussionEntry discussionEntry) {

		if (UserSessionTools.haveInternet(context)) {

			Log.i(getClass().getSimpleName(),
					"Internet connection is available");
			ApplicationLog
			.i("Internet connection is available - will replicate");
			Log.i(getClass().getSimpleName(), "Will replicate");

			DiscussionDatabase discussionDatabase = discussionEntry
					.getDiscussionDatabase();

			// String hostName = discussionDatabase.getHostName();
			// String dbPath = discussionDatabase.getDbPath();
			// String httpPort = discussionDatabase.getHttpPort();
			// String password = discussionDatabase.getPassword();
			// String userName = discussionDatabase.getUserName();
			String urlForDocuments = discussionEntry.getHref();
			// String httpType = "";
			// if (discussionDatabase.isUseSSL()) {
			// httpType = "https";
			// } else {
			// httpType = "http";
			// }
			// String urlForDocuments = httpType + "://" + hostName + ":"
			// + httpPort + dbPath + "/api/data/documents/";
			Log.i(getClass().getSimpleName(), "Accesing " + urlForDocuments);
			ApplicationLog.i("Accesing " + urlForDocuments);

			Log.d(getClass().getSimpleName(), "Starting");

			ApplicationLog.d("Starting", shouldLogALot);

			// Add the gzip Accept-Encoding header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);

			if (authenticationCookie != "") {

				ApplicationLog.d("Setting LtpaToken in request header",
						shouldLogALot);

				requestHeaders.add("Cookie", authenticationCookie);
			}

			// if (shouldLogALot) {
			// ApplicationLog.d("Setting LtpaToken");
			// }
			// requestHeaders
			// .set("LtpaToken",
			// "AAECAzUxNEEwNkFFNTE0QTIyQ0VDTj1KZW5zIEJydW50dC9PPWJydW50dDpULkwtLx2bdYtJ+Z8Vv1qS9dQL");
			// requestHeaders.set("SessionID",
			// "C0386B96E48178B83A7E778E710289F19773F76D");
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new StringHttpMessageConverter());

			String jsonString;
			try {
				// jsonString = restTemplate.getForObject(urlForDocuments,
				// String.class);
				// Make the HTTP GET request, marshaling the response to a
				// String
				ResponseEntity<String> response = restTemplate.exchange(
						urlForDocuments, HttpMethod.GET, requestEntity,
						String.class);
				jsonString = response.getBody();

				Log.d(getClass().getSimpleName(), "String received length: "
						+ jsonString.length());

				ApplicationLog.d(
						"String received length: " + jsonString.length(),
						shouldLogALot);

				// Log.d(getClass().getSimpleName(), jsonString);

				if (!isThisALoginForm(jsonString)) {
					try {
						JSONObject jsonDocument = new JSONObject(jsonString);
						if (jsonDocument.length() > 0) {
							// xx
							discussionEntry = enrichDiscussionEntryFromJson(
									discussionEntry, jsonDocument);
						} else {
							Log.i(getClass().getSimpleName(),
									"No Document retrieved. Nothing to do");
							ApplicationLog
							.i("No Document retrieved. Nothing to do");
						}
					} catch (Exception e) {
						// e.printStackTrace();
						Log.e(getClass().getSimpleName(),
								"getmessage: " + e.getMessage());
						ApplicationLog.e("Exception: " + e.getMessage());
					}

				} else {
					Log.e(getClass().getSimpleName(),
							"There is a login issue when accessing "
									+ urlForDocuments);
					Log.e(getClass().getSimpleName(),
							"The server prompts for login");

					ApplicationLog.e("There is a login issue when accessing "
							+ urlForDocuments);
					ApplicationLog.e("The server prompts for login");
				}

			} catch (RestClientException e1) {
				// e1.printStackTrace();
				String errorMessage = e1.getMessage();
				Log.e(getClass().getSimpleName(), "getmessage: " + errorMessage);
				ApplicationLog.e("Exception: " + errorMessage);
				if (errorMessage.contains("403")) {
					Log.i(getClass().getSimpleName(),
							"403 - Looks like the Domino Data Service is not enabled for the database "
									+ discussionDatabase.getDbPath());
					ApplicationLog
					.i("403 - Looks like the Domino Data Service is not enabled for the database "
							+ discussionDatabase.getDbPath());
				} else {
					String localizedErrorMessage = e1.getLocalizedMessage();
					if (localizedErrorMessage != null) {
						Log.e(getClass().getSimpleName(),
								"localizedErrorMessage: "
										+ localizedErrorMessage);
						ApplicationLog.e("localizedErrorMessage: "
								+ localizedErrorMessage);
					} else {
						Log.e(getClass().getSimpleName(),
								"Unable to get data from the database "
										+ discussionDatabase.getDbPath());
						ApplicationLog
						.e("Unable to get data from the database "
								+ discussionDatabase.getDbPath());
					}
				}
				e1.printStackTrace();

			}

		} else {
			Log.i(getClass().getSimpleName(),
					"Internet connection not available - Replication not possible");
			ApplicationLog
			.i("Internet connection not available - Replication not possible");
		}

		return discussionEntry;

	}

	private DiscussionEntry enrichDiscussionEntryFromJson(
			DiscussionEntry discussionEntry, JSONObject jsonDocument) {

		discussionEntry.setAbbreviateFrom(getDominoValueFromJson(jsonDocument,
				"AbbreviateFrom"));
		discussionEntry.setAbrFrom(getDominoValueFromJson(jsonDocument,
				"AbrFrom"));
		discussionEntry.setAbstractDoc(getDominoValueFromJson(jsonDocument,
				"AbstractDoc"));
		discussionEntry.setAltFrom(getDominoValueFromJson(jsonDocument,
				"AltFrom"));
		discussionEntry.setBody(getDominoValueFromJson(jsonDocument, "Body"));
		discussionEntry.setCategories(getDominoValueFromJson(jsonDocument,
				"Categories"));
		discussionEntry.setFrom(getDominoValueFromJson(jsonDocument, "From"));
		discussionEntry
		.setMainID(getDominoValueFromJson(jsonDocument, "MainID"));
		discussionEntry.setMimeVersion(getDominoValueFromJson(jsonDocument,
				"MimeVersion"));
		discussionEntry.setNewsLetterSubject(getDominoValueFromJson(
				jsonDocument, "NewsLetterSubject"));
		discussionEntry.setPath_Info(getDominoValueFromJson(jsonDocument,
				"Path_Info"));
		discussionEntry.setRemote_User(getDominoValueFromJson(jsonDocument,
				"Remote_User"));
		discussionEntry.setSubject(getDominoValueFromJson(jsonDocument,
				"Subject"));
		discussionEntry.setThreadId(getDominoValueFromJson(jsonDocument,
				"ThreadId"));
		discussionEntry.setWebCategories(getDominoValueFromJson(jsonDocument,
				"WebCategories"));
		// System fields below
		discussionEntry.setModified(getDominoValueFromJson(jsonDocument,
				"@modified"));
		discussionEntry.setHref(getDominoValueFromJson(jsonDocument, "@href"));
		discussionEntry.setUnid(getDominoValueFromJson(jsonDocument, "@unid"));
		discussionEntry.setCreated(getDominoValueFromJson(jsonDocument,
				"@created"));
		discussionEntry.setForm(getDominoValueFromJson(jsonDocument, "@form"));
		discussionEntry.setNoteid(getDominoValueFromJson(jsonDocument,
				"@noteid"));
		discussionEntry.setAuthors(getDominoValueFromJson(jsonDocument,
				"@authors"));

		return discussionEntry;
	}

	private String getDominoValueFromJson(JSONObject jsonDocument,
			String fieldName) {
		String returnValue = "";

		if (fieldName.contains("Body")) {
			try {
				JSONObject bodyObjectArray = jsonDocument
						.getJSONObject(fieldName);
				JSONArray bodyArray = bodyObjectArray.getJSONArray("content");

				for (int i = 0; i < bodyArray.length(); i++) {
					JSONObject bodyItem = bodyArray.getJSONObject(i);
					if (bodyItem.has("contentType")) {
						String thisContentType = bodyItem
								.getString("contentType");
						if (thisContentType.contains("text/html")) {
							Log.d(getClass().getSimpleName(),
									"Found body item with html - adding");

							ApplicationLog.d(
									"Found body item with html - adding",
									shouldLogALot);

							returnValue = bodyItem.getString("data");
						}

					}

				}
			}

			catch (JSONException e) {
				Log.d(getClass().getSimpleName(), "Unable to find field "
						+ fieldName);

				ApplicationLog.d("Unable to find field " + fieldName,
						shouldLogALot);

				returnValue = "";
			}

		} else { // All other fields than Body
			try {
				returnValue = jsonDocument.getString(fieldName);
			} catch (JSONException e) {
				// e.printStackTrace();
				Log.w(getClass().getSimpleName(), "Unable to find field "
						+ fieldName);

				ApplicationLog.d("Unable to find field " + fieldName,
						shouldLogALot);

				returnValue = "";
			}

		}

		return returnValue;
	}

	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}

	private String getAuthenticationToken(String hostName, String httpPort,
			String userName, String password, boolean useSSL,
			String redirectToUrl) {

		String authenticationCookie = "";

		String httpType = "";
		if (useSSL) {
			httpType = "https";
		} else {
			httpType = "http";
		}
		String urlForLogin = httpType + "://" + hostName + ":" + httpPort
				+ loginPath;

		ApplicationLog.d("URL for login: " + urlForLogin, shouldLogALot);

		RestTemplate template = new RestTemplate();
		template.getMessageConverters().add(new FormHttpMessageConverter());
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);
		ApplicationLog.d("Getting LtpaToken and SessionID", shouldLogALot);

		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("username", userName);
		requestBody.add("password", password);
		requestBody.add("redirectto", redirectToUrl);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(requestBody, requestHeaders);
		ApplicationLog.d( "HTTP connection now", shouldLogALot);
		ResponseEntity<?> httpResponse;
		try {
			httpResponse = template.exchange(urlForLogin,
					HttpMethod.POST, request, null);
			HttpHeaders responseHeaders = httpResponse.getHeaders();

			if (responseHeaders.isEmpty()) {
				ApplicationLog.d("No response headers", shouldLogALot);
			} else {
				List<String> val = responseHeaders.get("Set-Cookie");
				if (null != val) {
					String cookie = val.get(0);
					ApplicationLog.d("Set-Cookie: " + cookie, shouldLogALot);
					if (cookie.startsWith("LtpaToken=")) {
						ApplicationLog.d("Cookie is an LtpaToken", shouldLogALot);
						String actualToken = (String) cookie.subSequence(0,
								cookie.indexOf(";"));
						if (actualToken != null) {
							ApplicationLog.d("Token value= " + actualToken,
									shouldLogALot);
							authenticationCookie = actualToken;
						} else {
							ApplicationLog.d("Did not get the LtpaToken value",
									shouldLogALot);
						}
					}
				} else {
					ApplicationLog.d("No Set-Cookie", shouldLogALot);
				}
			} 

		} catch (RestClientException e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Error message not available";
			}
			Log.e(getClass().getSimpleName(), "getmessage: " + errorMessage);
			ApplicationLog.e("RestClientException: " + errorMessage);
			e.printStackTrace();
		} catch (Exception e){
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Error message not available";
			}
			Log.e(getClass().getSimpleName(), "getmessage: " + errorMessage);
			ApplicationLog.e("Exception: " + errorMessage);
			e.printStackTrace();
		}
		
		if (authenticationCookie.contentEquals("")) {
			ApplicationLog.i("Did not get an authentication cookie");
		}

		return authenticationCookie;
	}
}
