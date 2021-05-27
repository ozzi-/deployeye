package pojo;

import java.util.ArrayList;

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
	private String headerName;
	private String headerValue;
	
	private int timeout = 3000;
	private ArrayList<String> notificationRecipients = new ArrayList<String>();
	
	public Eye() {
		
	}
	
	public Eye(String name, String url, String kwVersion, String kwBranch, String kwHealth, String kwChangelog, Integer timeout, ArrayList<String> notificationRecipients) {
		this.setName(name);
		this.setUrl(url);
		this.setKwVersion(kwVersion);
		this.setKwBranch(kwBranch);
		this.setKwHealth(kwHealth);
		this.setKwChangelog(kwChangelog);
		if(timeout!=null) {
			this.setTimeout(timeout);
		}
		if(notificationRecipients!=null) {
			this.setNotificationRecipients(notificationRecipients);			
		}
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public ArrayList<String> getNotificationRecipients() {
		return notificationRecipients;
	}

	public void setNotificationRecipients(ArrayList<String> notificationRecipients) {
		this.notificationRecipients = notificationRecipients;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getHeaderValue() {
		return headerValue;
	}

	public void setHeaderValue(String headerValue) {
		this.headerValue = headerValue;
	}

}
