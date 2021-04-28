package pojo;

public class Eye {

	private int id;
	private String name;
	private String url;
	private String kwVersion;
	private String kwBranch;
	private String kwHealth;
	private String kwChangelog;
	private String lastResponse;
	private boolean lastCheckSucceeded;
	private String currentChangelog;
	private String lastCheckFailReason="";
	private String cookieName;
	private String cookieValue;
	
	public Eye() {
		
	}
	
	public Eye(int id, String name, String url, String kwVersion, String kwBranch, String kwHealth, String kwChangelog) {
		this.setId(id);
		this.setName(name);
		this.setUrl(url);
		this.setKwVersion(kwVersion);
		this.setKwBranch(kwBranch);
		this.setKwHealth(kwHealth);
		this.setKwChangelog(kwChangelog);
	}

	public Eye(String name, String url, String kwVersion, String kwBranch, String kwHealth, String kwChangelog) {
		this.setName(name);
		this.setUrl(url);
		this.setKwVersion(kwVersion);
		this.setKwBranch(kwBranch);
		this.setKwHealth(kwHealth);
		this.setKwChangelog(kwChangelog);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getKwVersion() {
		return kwVersion;
	}

	public void setKwVersion(String kwVersion) {
		this.kwVersion = kwVersion;
	}

	public String getKwBranch() {
		return kwBranch;
	}

	public void setKwBranch(String kwBranch) {
		this.kwBranch = kwBranch;
	}

	public String getKwHealth() {
		return kwHealth;
	}

	public void setKwHealth(String kwHealth) {
		this.kwHealth = kwHealth;
	}

	public String getKwChangelog() {
		return kwChangelog;
	}

	public void setKwChangelog(String kwChangelog) {
		this.kwChangelog = kwChangelog;
	}

	public String getLastResponse() {
		return lastResponse;
	}

	public void setLastResponse(String lastResponse) {
		this.lastResponse = lastResponse;
	}

	public boolean didLastCheckSucceed() {
		return lastCheckSucceeded;
	}

	public void setLastCheckSucceeded(boolean lastCheckSucceeded) {
		this.lastCheckSucceeded = lastCheckSucceeded;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setCurrentChangelog(String currentChangelog) {
		this.currentChangelog = currentChangelog;
	}
	
	public String getCurrentChangelog() {
		return this.currentChangelog;
	}

	public void setLastCheckFailReason(String reason) {
		this.lastCheckFailReason=reason;
	}
	
	public String getLastCheckFailReason() {
		return lastCheckFailReason;
	}

	public String getCookieName() {
		return cookieName;
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public String getCookieValue() {
		return cookieValue;
	}

	public void setCookieValue(String cookieValue) {
		this.cookieValue = cookieValue;
	}

}
