package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import helpers.Helpers;
import helpers.JSONRef;
import helpers.Log;
import helpers.Mailer;
import persistence.DB;
import pojo.Availability;
import pojo.Eye;
import pojo.RS;
import pojo.Reason;
import pojo.Val;
import pojo.Version;

public class Logic {
	static void doCheckLogic() {
		for (Eye eye : Service.getEyes()) {
			Log.logInfo("Starting Check Logic for "+eye.getName(), Service.class);
			if(eye.didLastCheckSucceed()) {
				Log.logInfo("\\_ Response: "+eye.getLastResponse(), Service.class);
				JSONObject eyesJSON;
				boolean validResponse=true;
				try {
					eyesJSON= new JSONObject(eye.getLastResponse());
				}catch (Exception e) {
					validResponse=false;
					handleAvailDown(eye,Reason.HEALTH_NOK,"Response does not seem to be valid JSON");
				}
				if(validResponse) {
					eyesJSON = new JSONObject(eye.getLastResponse());
					String currentHealth="";
					Object healthObj = eyesJSON.get(eye.getKwHealth());
					if(healthObj instanceof JSONArray) {
						JSONArray healthEvents = eyesJSON.getJSONArray(eye.getKwHealth());
						if(healthEvents.length()>0) { 
							// i.E: "health":[[1619624880705,"some error"],[1619625601244,"another error!"]]
							currentHealth = healthEvents.toString(); 						
						}else {
							currentHealth="ok";
						}
					}else {
						currentHealth = eyesJSON.getString(eye.getKwHealth()).toLowerCase();
					}
					String currentVersion = eyesJSON.getString(eye.getKwVersion());
					String currentChangelog = eyesJSON.getString(eye.getKwChangelog());
					eye.setCurrentChangelog(currentChangelog);
					
					if(currentHealth.equals("ok")) {
						handleOK(eye, currentVersion);
					}else {
						handleAvailDown(eye,Reason.HEALTH_NOK,currentHealth.equals("nok")?"Generic Error":currentHealth);
					}
				}
			}else {
				handleAvailDown(eye,Reason.CONNECTION_FAILED,eye.getLastCheckFailReason());
			}
		}
	}
	
	private static void handleAvailDown(Eye eye, int reasonCode, String reasonDescription) {
		Log.logInfo("\\_ Handling DOWN ("+reasonCode+") with reason '"+reasonDescription+"'", Service.class);

		ArrayList<Val> vals = new ArrayList<Val>();
		vals.add(new Val(eye.getId()));
		String event = eye.getName()+" went down";
		String eventLong= eye.getName()+" went down on "+Helpers.getCurrentDateTime()+" - "+reasonDescription;
		try {
			RS rs = DB.doSelect("SELECT * FROM availability WHERE availability_eye_idfk = ? ORDER BY availability_down_date DESC LIMIT 1;", vals);
			ArrayList<Availability> avails = JSONRef.createFromRS(rs.getRs(), Availability.class);
			rs.close();
			if(avails.size()==0) {
				insertNewAvail(eye.getId(),reasonCode, reasonDescription);
				Mailer.sendMailToRecipients(eye, event, eventLong);
				Log.logInfo("\\_ Peristing first availability issue for "+eye.getName(), Service.class);

			}else {
				Availability latest = avails.get(0);
				if(latest.getRecover_date()==null) {
					Log.logInfo("\\_ Availability check: Still down", Service.class);
				}else {
					insertNewAvail(eye.getId(), reasonCode, reasonDescription);
					Mailer.sendMailToRecipients(eye, event, eventLong);
					Log.logInfo("\\_ Availability check: Just went down", Service.class);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.logException(e, Service.class);
		}
	}
	
	private static void handleOK(Eye eye, String currentVersion) {
		Log.logInfo("\\_ Handling OK", Service.class);
		try {
			ArrayList<Val> vals = new ArrayList<Val>();
			vals.add(new Val(eye.getId()));
			
			RS rs2 = DB.doSelect("SELECT * FROM availability WHERE availability_eye_idfk = ? ORDER BY availability_down_date DESC LIMIT 1;", vals);
			ArrayList<Availability> avails = JSONRef.createFromRS(rs2.getRs(), Availability.class);
			rs2.close();
			if(avails.size()!=0) {
				Availability latest = avails.get(0);
				if(latest.getRecover_date()==null) {
					String event = eye.getName()+" recovered";
					String eventLong = eye.getName()+" ("+currentVersion+")"+" has recovered on "+Helpers.getCurrentDateTime();
					Log.logInfo(eventLong,Service.class);
					Mailer.sendMailToRecipients(eye, event, eventLong);
					ArrayList<Val> vals2 = new ArrayList<Val>();
					vals2.add(new Val(latest.getId()));
					DB.doUpdate("UPDATE availability SET availability_recover_date= now() WHERE availability_id = ?;", vals2);
				}
			}
			
			RS rs = DB.doSelect("SELECT * FROM version WHERE version_eye_idfk= ? ORDER BY version_date DESC LIMIT 1;", vals);
			ArrayList<Version> versions = JSONRef.createFromRS(rs.getRs(), Version.class);
			rs.close();
			if(versions.size()==0) {
				insertNewVersion(eye.getId(),currentVersion);
				Log.logInfo("\\_ Peristing first with version '"+currentVersion+"'", Service.class);
			}else{
				Version latest = versions.get(0);
				Log.logInfo("\\_ Lastest version '"+latest.getVersionString()+"' - current version '"+currentVersion+"'", Service.class);
				if(!latest.getVersionString().equals(currentVersion)) {
					String event = eye.getName()+" changed its version to "+currentVersion;
					String eventLong = eye.getName()+" changed its version from "+latest.getVersionString()+" to "+currentVersion+" on "+Helpers.getCurrentDateTime();
					Mailer.sendMailToRecipients(eye, event, eventLong);
					Log.logInfo("\\_ "+eventLong, Service.class);
					insertNewVersion(eye.getId(),currentVersion);							
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.logException(e, Service.class);
		}
	}
	

	
	private static void insertNewAvail(int id, int reason, String reasonDescription) throws SQLException {
		ArrayList<Val> val = new ArrayList<Val>();
		val.add(new Val(id));
		val.add(new Val(reason));
		val.add(new Val(reasonDescription));
		DB.doInsert("INSERT INTO availability (availability_eye_idfk,availability_down_date,availability_reason_code, availability_reason_description) VALUES (?,NOW(),?,?);", val);
	}
	
	

	private static int insertNewVersion(int id, String version) throws SQLException {
		ArrayList<Val> val = new ArrayList<Val>();
		val.add(new Val(id));
		val.add(new Val(version));
		return DB.doInsert("INSERT INTO version (version_eye_idfk,version_date,version_string) VALUES (?,NOW(),?);", val);
	}

	static void doHTTPGetCheck() {
		for (Eye eye : Service.getEyes()) {
			Log.logInfo("Getting health for "+eye.getName()+" - HTTP Get '"+eye.getUrl()+"' with a timeout of '"+eye.getTimeout()+"'", Service.class);

			StringBuilder result = new StringBuilder();
			URL url;
			try {
				url = new URL(eye.getUrl());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				if(eye.getHeaderName()!=null && eye.getHeaderValue()!=null) {
					conn.setRequestProperty(eye.getHeaderName(), eye.getHeaderValue());
				}
				conn.setConnectTimeout(eye.getTimeout());
				if(eye.getCookieName()!=null && eye.getCookieValue()!=null) {
					String cookieValue=eye.getCookieValue();
					if(eye.getCookieValue().startsWith("@")) {
						Log.logInfo("\\_ Loading cookie value from file '"+eye.getCookieValue()+"' as starting with @", Service.class);
						cookieValue = Helpers.readFileToStringWithoutNewlines(eye.getCookieValue().substring(1));
					}
					String cookieString = eye.getCookieName()+"="+cookieValue;
					String cookieStringDesc = eye.getCookieName()+"="+Helpers.censorValue(cookieValue,4);
					Log.logInfo("\\_ Adding cookie '"+cookieStringDesc+"'", Service.class);
					conn.setRequestProperty("Cookie",cookieString);
				}
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				rd.close();
				eye.setLastCheckSucceeded(true);
				eye.setLastResponse(result.toString());
			} catch (Exception e) {
				eye.setLastCheckSucceeded(false);
				eye.setLastCheckFailReason(e.getClass().getName()+": "+e.getMessage());
				Log.logInfo("\\_ Exception when doing HTTP GET on '"+eye.getUrl()+"' - "+e.getClass().getName()+": "+e.getMessage(), Service.class);
			}
		}
	}

}
