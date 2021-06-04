package pojo;

public class Version {
	private int id;
	private int eye_idfk;
	private String date;
	private String string;
	
	public Version() {
	}
	
	public Version(int id, int eye_idfk, String date, String string) {
		this.setId(id);
		this.setEye_idfk(eye_idfk);
		this.setDate(date);
		this.setVersionString(string);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEye_idfk() {
		return eye_idfk;
	}

	public void setEye_idfk(int eye_idfk) {
		this.eye_idfk = eye_idfk;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getVersionString() {
		return string;
	}

	public void setVersionString(String string) {
		this.string = string;
	}
}
