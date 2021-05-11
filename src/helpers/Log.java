package helpers;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	public static <T> void logInfo(String msg, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.INFO, "deployeye - "+msg);		
	}
	
	public static void logWarning(String msg, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.WARNING, "deployeye - "+msg);		
	}
	

	public static void logException(Exception e, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.WARNING, "deployeye - "+e.getClass().getName()+": "+e.getMessage(),e);	
	}
}
