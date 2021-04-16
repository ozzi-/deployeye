package helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Helpers {

	private static final ArrayList<String> allowedChars = new ArrayList<String>();

	public static String getCurrentDate() {
		Date now = new Date(); 
		return new SimpleDateFormat("yyyy-MM-dd").format(now);
	}
	
	public static String getCurrentDateTime() {
		Date now = new Date(); 
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
	}
	
	public static JsonObject getJsonObjOfBody(String body) {
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(body);
		JsonObject jsonObj = jsonTree.getAsJsonObject();
		return jsonObj;
	}
	
	public static ArrayList<String> getAllowedSessionChars() {
		if (allowedChars.size() == 0) {
			allowedChars.addAll(getASCIICharacters(48, 57)); // 0-9
			allowedChars.addAll(getASCIICharacters(65, 90)); // A-Z
			allowedChars.addAll(getASCIICharacters(97, 122)); // a-z
		}
		return allowedChars;
	}

	private static ArrayList<String> getASCIICharacters(int from, int to) {
		ArrayList<String> chars = new ArrayList<String>();
		for (int i = from; i <= to; i++) {
			chars.add(String.valueOf((char) i));
		}
		return chars;
	}

	public static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String sanitizeFilename(String inputName) {
		return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
	}

	public static File[] getListOfFiles(String path) {
		File folder = new File(path);
		return folder.listFiles();
	}

	public static ArrayList<String> getListOfFiles(String path, String endsWith) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		ArrayList<String> listOfFilesFiltered = new ArrayList<String>();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(endsWith)) {
					listOfFilesFiltered.add(listOfFiles[i].getName());
				}
			}
		}
		return listOfFilesFiltered;
	}

	public static String readFile(String path) throws Exception {
		Log.logInfo( "Reading file " + path, Helpers.class);
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (Exception e) {
			throw new Exception("Could not read file: " + path);
		}
		return new String(encoded, "UTF-8");
	}

	public static JSONObject loadConfig(String path) throws Exception {
		String json = "";
		try {
			json = readFile(path);
		} catch (IOException e) {
			throw new Exception("Cannot load test json file in loadConfig!");
		}
		final JSONObject obj;
		try {
			obj = new JSONObject(json);
			return obj;
		} catch (Exception e) {
			throw new Exception("Error parsing test json file (" + path + ")   \"" + e.getMessage() + "\" in loadConfig");
		}
	}

	public static String getDateFromUnixTimestamp(long timestamp) {
		Date date = new Date(timestamp);
		return dtf.format(date);
	}

	public static String getDateTime() {
		return dtf.format(System.currentTimeMillis());
	}
	
	public static String[] getStringArray(ArrayList<String> arr) {
		String str[] = new String[arr.size()];
		for (int j = 0; j < arr.size(); j++) {
			str[j] = arr.get(j);
		}
		return str;
	}
}
