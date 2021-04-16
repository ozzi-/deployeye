package pojo;

public class Val {
	private String value;
	private boolean isString;

	public Val(int value) {
		this.setValue(String.valueOf(value));
		this.setString(false);
	}
	
	public Val(String value) {
		this.setValue(value);
		this.setString(true);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isString() {
		return isString;
	}

	public void setString(boolean isString) {
		this.isString = isString;
	}
}