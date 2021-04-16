package pojo;

public class Mail {
	private String from;
	private String to;
	private String subject;
	private String content;
	
	public Mail(String from, String to, String subject, String content) {
		this.setFrom(from);
		this.setTo(to);
		this.setSubject(subject);
		this.setContent(content);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
