package helpers;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONRef {
	
	public static Object getAsObject(String json, Class<?> objClass) throws Exception {
		JsonParser jParser = new JsonParser();
		JsonObject jObj = (JsonObject) jParser.parse(json);
		Object obj = objClass.getDeclaredConstructor().newInstance();
		Field[] fields = objClass.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i].getName();
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Class<?> fieldType = fields[i].getType();

			String fieldTypeName = fieldType.getName();
			if(fieldTypeName.equals("java.lang.String")){
				try {
					String value = jObj.get(fieldName).getAsString();	
					field.set(obj, value);
				}catch (Exception e) {}
			}else if(fieldTypeName.equals("java.lang.Integer")||fieldTypeName.equals("int")){
				try {
					int value = jObj.get(fieldName).getAsInt();
					field.set(obj, value);					
				}catch (Exception e) {}
			}else if(fieldTypeName.equals("long")){
				try {
					long value = jObj.get(fieldName).getAsLong();
					field.set(obj, value);					
				}catch (Exception e) {}
			}else {
				throw new Exception("Field Value is not string, int or long but "+fieldTypeName);
			}
		}
		return obj;
	}

	public static String getAsJSON(ArrayList<?> list) throws IllegalArgumentException, IllegalAccessException {
		String json = "[";
		for (Object object : list) {
			json += JSONRef.getAsJSON(object,false) + ",";
		}
		json = json.length()>1?json.substring(0, json.length() - 1) : json;
		return json + "]";
	}
	
	/** 
	 * if result set is only one row, json array won't be returned, only json obj
	 */
	public static String getAsJSONFromRS_Single(ResultSet rs) throws SQLException {
		String json = "";
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		int rowCount=0;
		while (rs.next()) {
			rowCount++;
			json+="{";
			for (int i = 1; i <= columnCount; i++ ) {
			  String name = rsmd.getColumnLabel(i);
			  String value = rs.getString(name);
			  value = value==null?"null":value;
			  value = value.replaceAll("\r", "").replaceAll("\n", "\\\\n");
			  if(requiresQuotes(rsmd.getColumnType(i))) {
				  json+="\""+name+"\":\""+value+"\",";
			  }else {
				  json+="\""+name+"\":"+value+",";				  
			  }
			}
			json = json.substring(0, json.length() - 1);
			json+="},";
		}
		// cut off surplus last ','
		json = json.length()>1?json.substring(0, json.length() - 1) : json;
		// make it an array if result set had more than one row
		if(rowCount>1) {
			json = "["+json+"]";
		}
		if(json=="") {
			json="{}";
		}
		return json;
	}
	
	public static String getAsJSONFromRS(ResultSet rs) throws SQLException {
		String json = "[";
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		
		while (rs.next()) {
			json+="{";
			for (int i = 1; i <= columnCount; i++ ) {
			  String name = rsmd.getColumnLabel(i);
			  String value = rs.getString(name);
			  value = value==null?"null":value;
			  value = value.replaceAll("\r", "").replaceAll("\n", "\\\\n");
			  if(requiresQuotes(rsmd.getColumnType(i))) {
				  json+="\""+name+"\":\""+value+"\",";
			  }else {
				  json+="\""+name+"\":"+value+",";				  
			  }
			}
			json = json.substring(0, json.length() - 1);
			json+="},";
		}
		json = json.length()>1?json.substring(0, json.length() - 1) : json;
		return json + "]";
	}

	private static boolean requiresQuotes(int i) throws SQLException {
		boolean requires = true;
		if(i == java.sql.Types.INTEGER) {
			requires=false;
		}
		if(i == java.sql.Types.BOOLEAN) {
			requires=false;
		}
		if(i == java.sql.Types.BIGINT) {
			requires=false;
		}
		if(i == java.sql.Types.BIT) {
			requires=false;
		}
		if(i == java.sql.Types.DECIMAL) {
			requires=false;
		}
		if(i == java.sql.Types.DOUBLE) {
			requires=false;
		}
		if(i == java.sql.Types.FLOAT) {
			requires=false;
		}
		if(i == java.sql.Types.DOUBLE) {
			requires=false;
		}
		if(i == java.sql.Types.TINYINT) {
			requires=false;
		}
		return requires;
	}
	
	public static <T> ArrayList<T> createFromRS(ResultSet s, Class<T> objClass) throws Exception {
		T obj = objClass.getDeclaredConstructor().newInstance();
		ArrayList<T> objs = new ArrayList<T>();
		Field[] fields = objClass.getDeclaredFields();

		String name = obj.getClass().getName();
		
		String prefix = name.substring(name.lastIndexOf('.') + 1).trim().toLowerCase()+"_";
		while (s.next()) {

        	obj = objClass.getDeclaredConstructor().newInstance();
			for (int i = 0; i < fields.length; i++) {
				String fieldName = fields[i].getName();
				Field field = obj.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				Class<?> fieldType = fields[i].getType();
				String fieldTypeName = fieldType.getName();
				if(fieldTypeName.equals("java.lang.String")){
					try {
						String value = s.getString(prefix+fieldName);
						field.set(obj, value);
					}catch (Exception e) { throw new Exception(e); }
				}else if(fieldTypeName.equals("java.lang.Integer")||fieldTypeName.equals("int")){
					try {
						int value = s.getInt(prefix+fieldName);
						field.set(obj, value);					
					}catch (Exception e) { throw new Exception(e); }
				}else if(fieldTypeName.equals("long")){
					try { 
						long value = s.getLong(prefix+fieldName);
						field.set(obj, value);					
					}catch (Exception e) { throw new Exception(e); }
				}else if(fieldTypeName.equals("boolean")){
					try { 
						boolean value = s.getBoolean(prefix+fieldName);
						field.set(obj, value);					
					}catch (Exception e) { throw new Exception(e); }
				}else {
					throw new Exception("Field Value is not string, int or long but "+fieldTypeName);
				}
			}
			objs.add((T) obj);
        }
        return objs;
	}
	
	public static String getAsJSON(Object obj, boolean surpressObjCurlies) throws IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> objClass = obj.getClass();
		Field[] fields = objClass.getDeclaredFields();
		String json = surpressObjCurlies?"":"{";
		for (int i = 0; i < fields.length; i++) {
			String name = fields[i].getName();
			fields[i].setAccessible(true);
			Object value = fields[i].get(obj);
			if (value instanceof String) {
				json += "\"" + name + "\":\"" + value + "\",";
			} else {
				json += "\"" + name + "\":" + value + ",";
			}
		}
		json = json.length()>1?json.substring(0, json.length() - 1) : json;
		return json + (surpressObjCurlies?"":"}");
	}
	
	

}
