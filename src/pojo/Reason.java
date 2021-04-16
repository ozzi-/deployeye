package pojo;

public class Reason {
	public static final int CONNECTION_FAILED = 1;
	public static final int HEALTH_NOK = 2;
	
	public static int getByName(String name) {
		if(name.equals("CONNECTION_FAILED")) {
			return 1;
		}
		if(name.equals("HEALTH_NOK")) {
			return 2;
		}
		return 0;
	}
}
