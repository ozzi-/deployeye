package pojo;

public class CSRFCheckResult {

	private boolean userExists;
	private boolean tokenMatches;
	private boolean tokenStale;

	public CSRFCheckResult(boolean userExists, boolean tokenMatches, boolean tokenStale) {
		this.userExists = userExists;
		this.tokenMatches = tokenMatches;
		this.tokenStale = tokenStale;
	}

	public boolean userExists() {
		return userExists;
	}
	
	public boolean tokenMatches() {
		return tokenMatches;
	}
	
	public boolean tokenStale() {
		return tokenStale;
	}
	
	public String asJSON() {
		return "{ \"tokenMatches\": "+Boolean.toString(tokenMatches)+", \"tokenStale\": "+Boolean.toString(tokenStale)+"}";
	}
}
