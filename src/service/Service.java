
package service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import com.google.gson.JsonObject;

import errorhandling.GlobalExceptionHandler;
import helpers.Config;
import helpers.JSONRef;
import helpers.Log;
import persistence.DB;
import pojo.Eye;
import pojo.RS;
import pojo.Val;

@Path("/")
@Singleton
public class Service extends ResourceConfig implements ContainerLifecycleListener {

	
	// TODO reload cfg call
	
	private static List<Eye> eyes = Collections.synchronizedList(new ArrayList<Eye>());
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
		final String version = "1.3";
		Log.logInfo("Starting deployeye - " + version, this);

		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
		Config.loadMailConfig();
		Config.loadAppConfig();
		Config.loadEyesFromConfig();
		persistLoadedEyes();

		try {
			DB.testDB();
			Scheduler.schedule();
		} catch (Exception e) {
			Log.logException(e, Service.class);
			System.exit(1);
		}
	}

	private void persistLoadedEyes() {
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
		if(eye == null) {
			rs.close();
			JsonObject errJo = new JsonObject();
			errJo.addProperty("error", "Cannot find eye with ID '"+id+"'");
			return Response.status(400).entity(errJo.toString()).type("application/json").build();
			//throw new NullPointerException("Could not find Eye with the ID '"+id+"'");
		}
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
	
	public static List<Eye> getEyes() {
		return eyes;
	}

}