package ar.com.callcenter.model;

public class Call {
	
	private boolean attendedOk = false;
	private String id;
	private int duration;
	
	public Call(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isAttendedOk() {
		return attendedOk;
	}

	public void setAttendedOk(boolean attendedOk) {
		this.attendedOk = attendedOk;
	}
	
}
