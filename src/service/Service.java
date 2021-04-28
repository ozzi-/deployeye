
package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.json.JSONArray;
import org.json.JSONObject;

import errorhandling.GlobalExceptionHandler;
import helpers.Config;
import helpers.Helpers;
import helpers.JSONRef;
import helpers.Log;
import persistence.DB;
import pojo.Availability;
import pojo.Eye;
import pojo.RS;
import pojo.Reason;
import pojo.Val;
import pojo.Version;

@Path("/")
@Singleton
public class Service extends ResourceConfig implements ContainerLifecycleListener {

	public static ArrayList<Eye> eyes = new ArrayList<Eye>();
	private static final int pageSize = 10;
	
	@Override
	public void onReload(Container container) {
		Log.logInfo("Reloading", this);
		Scheduler.exit();
		DB.exit();
	}

	@Override
	public void onShutdown(Container container) {
		Log.logInfo("Shutting down", this);
		Scheduler.exit();
		DB.exit();
	}

	@Override
	public void onStartup(Container container) {
		final String version = "1.1";
		Log.logInfo("Starting deployeye - " + version, this);

		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
		Config.loadMailConfig();
		Config.loadAppConfig();
		Config.loadEyes();
		for (Eye eye : eyes) {
			ArrayList<Val> vals = new ArrayList<Val>();
			vals.add(new Val(eye.getName()));
			try {
				RS rs = DB.doSelect("SELECT *,COUNT(*) AS rowcount FROM eye WHERE eye_name LIKE ?;", vals);
				if (rs.getRs().next()) {
					int a = rs.getRs().getInt("rowcount");
					if (a == 0) {
						int id = DB.doInsert("INSERT INTO eye (eye_name) VALUES (?);", vals);
						eye.setId(id);
						Log.logInfo("Inserting eye '" + eye.getName() + "'", Service.class);
					}else {
						eye.setId(rs.getRs().getInt("eye_id"));
						Log.logInfo("Loaded eye '" + eye.getName() + "' with ID "+eye.getId(), Service.class);
					}
				}
				rs.close();
			} catch (SQLException e) {
				Log.logException(e, Service.class);
				e.printStackTrace();
			}
		}

		try {
			DB.testDB();
			Scheduler.schedule();
		} catch (Exception e) {
			Log.logException(e, Service.class);
			System.exit(1);
		}
	}
	
	private static Eye getEyeByID(int id) {
		for (Eye eye : eyes) {
			if(eye.getId()==id) {
				return eye;
			}
		}
		return null;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/eye")
	public Response getEyes(@Context HttpHeaders headers) throws Exception {
		Log.logInfo("called /eye", this);
		JSONArray res = new JSONArray();
		for (Eye eye : eyes) {
			ArrayList<Val> vals = new ArrayList<Val>();
			vals.add(new Val(eye.getId()));
			RS rs = DB.doSelect("SELECT * FROM eye LEFT JOIN version ON version.version_eye_idfk = eye.eye_id LEFT JOIN availability ON availability.availability_eye_idfk = eye.eye_id WHERE eye_id = ? ORDER BY version_date DESC, availability_down_date DESC LIMIT 1;", vals);
			String json = JSONRef.getAsJSONFromRS_Single(rs.getRs());
			JSONObject jsonO = new JSONObject(json);
			res.put(jsonO);
			rs.close();
		}
		return Response.status(200).entity(res.toString()).type("application/json").build();
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/eye/{id}")
	public Response getEye(@Context HttpHeaders headers, @PathParam("id") int id) throws Exception {
		Log.logInfo("called /eye/"+id, this);
		JSONObject outer = new JSONObject();
		ArrayList<Val> vals = new ArrayList<Val>();
		vals.add(new Val(id));
		RS rs = DB.doSelect("SELECT * FROM eye WHERE eye_id = ?;", vals);
		String json = JSONRef.getAsJSONFromRS_Single(rs.getRs());
		JSONObject jo = new JSONObject(json);
		Eye eye = getEyeByID(id);
		jo.put("eye_url", eye.getUrl());
		jo.put("eye_changelog", eye.getCurrentChangelog());
		outer.put("eye",jo);
		rs.close();
		
		rs = DB.doSelect("SELECT * FROM version WHERE version_eye_idfk = ? ORDER BY version_date DESC;", vals);
		json = JSONRef.getAsJSONFromRS(rs.getRs());
		outer.put("version",new JSONArray(json));
		rs.close();
		
		rs = DB.doSelect("SELECT * FROM availability WHERE availability_eye_idfk = ? ORDER BY availability_down_date DESC LIMIT "+pageSize+";", vals);
		json = JSONRef.getAsJSONFromRS(rs.getRs());
		outer.put("availability",new JSONArray(json));
		rs.close();
		
		return Response.status(200).entity(outer.toString()).type("application/json").build();
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/eye/{id}/availability/{page}")
	public Response getAvailPage(@Context HttpHeaders headers, @PathParam("id") int id,  @PathParam("page") int page) throws Exception {
		Log.logInfo("called /eye/"+id+"/availability/"+page, this);
		page = page<1 ? 1 : page;
		
		int offset = (page-1)*pageSize;
		JSONObject outer = new JSONObject();
		ArrayList<Val> vals = new ArrayList<Val>();
		vals.add(new Val(id));
		
		RS rs = DB.doSelect("SELECT * FROM availability WHERE availability_eye_idfk = ? ORDER BY availability_down_date DESC LIMIT "+pageSize+" OFFSET "+offset+";", vals);
		String json = JSONRef.getAsJSONFromRS(rs.getRs());
		outer.put("availability",new JSONArray(json));
		rs.close();
		
		return Response.status(200).entity(outer.toString()).type("application/json").build();
	}


	public static void doChecks() {
		doHTTPGetCheck();
		doCheckLogic();
	}

	private static void doCheckLogic() {
		for (Eye eye : eyes) {
			Log.logInfo("Starting Check Logic for "+eye.getName(), Service.class);
			if(eye.didLastCheckSucceed()) {
				Log.logInfo("Response: "+eye.getLastResponse(), Service.class);
				boolean validResponse=true;
				try {
					new JSONObject(eye.getLastResponse());
				}catch (Exception e) {
					validResponse=false;
					handleAvailDown(eye,Reason.HEALTH_NOK,"Response does not seem to be valid JSON");
				}
				if(validResponse) {
					JSONObject eyesJSON = new JSONObject(eye.getLastResponse());
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

	private static void handleOK(Eye eye, String currentVersion) {
		try {
			ArrayList<Val> vals = new ArrayList<Val>();
			vals.add(new Val(eye.getId()));
			
			RS rs2 = DB.doSelect("SELECT * FROM availability WHERE availability_eye_idfk = ? ORDER BY availability_down_date DESC LIMIT 1;", vals);
			ArrayList<Availability> avails = JSONRef.createFromRS(rs2.getRs(), Availability.class);
			rs2.close();
			if(avails.size()!=0) {
				Availability latest = avails.get(0);
				if(latest.getRecover_date()==null) {
					Log.logInfo(eye.getName()+" went up",Service.class);
					ArrayList<Val> vals2 = new ArrayList<Val>();
					vals2.add(new Val(latest.getId()));
					DB.doUpdate("UPDATE availability SET availability_recover_date= now() WHERE availability_id = ?;", vals2);
				}
			}
			
			RS rs = DB.doSelect("SELECT * FROM version WHERE version_eye_idfk=? ORDER BY version_date DESC LIMIT 1;", vals);
			ArrayList<Version> versions = JSONRef.createFromRS(rs.getRs(), Version.class);
			rs.close();
			if(versions.size()==0) {
				insertNewVersion(eye.getId(),currentVersion);
				Log.logInfo("Peristing first version for "+eye.getName()+" with version string "+currentVersion, Service.class);
			}else{
				Version latest = versions.get(0);
				if(!latest.getString().equals(currentVersion)) {
					Log.logInfo(eye.getName()+" ("+eye.getId()+") changed its version from "+latest.getString()+" to "+currentVersion, Service.class);
					insertNewVersion(eye.getId(),currentVersion);							
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.logException(e, Service.class);
		}
	}
	

	private static void handleAvailDown(Eye eye, int reasonCode, String reasonDescription) {
		ArrayList<Val> vals = new ArrayList<Val>();
		vals.add(new Val(eye.getId()));
		try {
			RS rs = DB.doSelect("SELECT * FROM availability WHERE availability_eye_idfk = ? ORDER BY availability_down_date DESC LIMIT 1;", vals);
			ArrayList<Availability> avails = JSONRef.createFromRS(rs.getRs(), Availability.class);
			rs.close();
			if(avails.size()==0) {
				insertNewAvail(eye.getId(),reasonCode, reasonDescription);
				Log.logInfo("Peristing first availability issue for "+eye.getName(), Service.class);

			}else {
				Availability latest = avails.get(0);
				if(latest.getRecover_date()==null) {
					Log.logInfo(eye.getName()+" is still down", Service.class);
				}else {
					insertNewAvail(eye.getId(), reasonCode, reasonDescription);
					Log.logInfo(eye.getName()+" went down", Service.class);
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

	private static void doHTTPGetCheck() {
		for (Eye eye : eyes) {
			Log.logInfo("HTTP Get "+eye.getUrl(), Service.class);

			StringBuilder result = new StringBuilder();
			URL url;
			try {
				url = new URL(eye.getUrl());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				// TODO make this configurable
				conn.setConnectTimeout(3500);
				if(eye.getCookieName()!=null && eye.getCookieValue()!=null) {
					String cookieValue=eye.getCookieValue();
					if(eye.getCookieValue().startsWith("@")) {
						Log.logInfo("Loading cookie value from file '"+eye.getCookieValue()+"' as starting with @", Service.class);
						cookieValue = Helpers.readFile(eye.getCookieValue().substring(1));
					}
					Log.logInfo("Adding cookie "+eye.getCookieName()+"="+cookieValue, Service.class);
					conn.setRequestProperty("Cookie",eye.getCookieName()+"="+cookieValue+";");
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
				Log.logException(e, Service.class);
				e.printStackTrace();
			}
		}
	}
}