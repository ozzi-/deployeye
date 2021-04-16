package pojo;

public class Availability {
	private int id;
	private int eye_idfk;
	private String down_date;
	private String recover_date;
	
	public Availability() {
	}
	
	public Availability(int id, int eye_idfk, String down_date, String recover_date) {
		this.setId(id);
		this.setEye_idfk(eye_idfk);
		this.setDown_date(down_date);
		this.setRecover_date(recover_date);
	}
	
	public Availability(int eye_idfk, String down_date, String recover_date) {
		this.setEye_idfk(eye_idfk);
		this.setDown_date(down_date);
		this.setRecover_date(recover_date);
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

	public String getDown_date() {
		return down_date;
	}

	public void setDown_date(String down_date) {
		this.down_date = down_date;
	}

	public String getRecover_date() {
		return recover_date;
	}

	public void setRecover_date(String recover_date) {
		this.recover_date = recover_date;
	}

}
