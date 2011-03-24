package hsma.ss2011.vsy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GameManagement {
	private URL baseURL;
	private String nick;
	private HttpClient httpclient;
	
	public GameManagement(String server, int port, String nick) throws MalformedURLException {
		this.baseURL = new URL("http", server, port, "/");
		this.nick = nick;
		this.httpclient = new DefaultHttpClient();
	}
	
	/**
	 * Request the currently running game sessions from the server.
	 * @return The current game sessions or null if no exist.
	 */
	public GameSession[] getCurrentGames() {
		GameSession[] gameSessions = null;
		HttpGet request = new HttpGet(baseURL.toString() + "CurrentGames");
		HttpResponse responseFromServer = null;
		HttpEntity entity = null;
		BufferedReader reader = null;
		JSONArray responseAsJSON = null;
		
		/* Request for the raw list of currently running sessions
		 */
		try {
			responseFromServer = httpclient.execute(request);
			entity = responseFromServer.getEntity();
			if (entity != null) {
				InputStream stream = entity.getContent();
				reader = new BufferedReader(new InputStreamReader(stream), 8192);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Now convert the raw response into a JSONArray
		 * and extract the information from that.
		 */
		try {
			responseAsJSON = new JSONArray(new JSONTokener(reader));
			gameSessions = this.extractGameSessions(responseAsJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return gameSessions;
	}
	
	/**
	 * Extract the information about the running sessions from the JSONArray
	 * @param rawData the JSONArray containing the raw data.
	 * @return The currently running sessions or null if no session exists.
	 * @throws JSONException 
	 */
	private GameSession[] extractGameSessions(JSONArray rawData) throws JSONException {
		GameSession[] sessions = (rawData.length()>0) ? new GameSession[rawData.length()] : null;
		
		// Convert the JSONArray to a GameSession Array
		for (int i=0; i < sessions.length; i++) {
			JSONObject item = rawData.getJSONObject(i);
			JSONArray participants = item.getJSONArray("participants");
			
			GameSession entry = new GameSession();
			entry.setCreated(item.getInt("created"));
			entry.setId(item.getString("id"));
			entry.setName("name");
			
			// extract the playernames from the participants JSONArray
			String[] players = new String[participants.length()];
			for (int j=0; j< players.length; j++)
				players[j] = participants.getString(j);
			entry.setParticipants(players);
			
			sessions[i] = entry;
			item = null; // just to make sure, that nothing happens by accident
		}
		
		return sessions;
	}
	
}