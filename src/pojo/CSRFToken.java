package pojo;

public class CSRFToken {
	private static long lifetimeInS = 3600;
	private String token;
	private long created;
	
	public CSRFToken(String token) {
		this.token = token;
		this.created = System.currentTimeMillis() / 1000L;
	}
	
	public boolean isStale() {
		long currentUnixTime = System.currentTimeMillis() / 1000L;
		return currentUnixTime>(created+lifetimeInS);
	}
	
	public long validUntil() {
		return created+lifetimeInS-1;
	}
	
	public long getLifeTime() {
		return lifetimeInS;
	}
	
	public long getCreated() {
		return created;
	}

	public String getToken() {
		return token;
	}
}
