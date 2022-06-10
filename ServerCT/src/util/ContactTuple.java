package util;

public class ContactTuple {
	String ephemeralId;
	String digest;
	double pho;
	double theta;
	long timeslot;
	public ContactTuple(String ephemeralId, String digest, double pho, double theta, long timeslot) {
		super();
		this.ephemeralId = ephemeralId;
		this.digest = digest;
		this.pho = pho;
		this.theta = theta;
		this.timeslot = timeslot;
	}
	public String getEphemeralId() {
		return ephemeralId;
	}
	public void setEphemeralId(String ephemeralId) {
		this.ephemeralId = ephemeralId;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public double getPho() {
		return pho;
	}
	public void setPho(double pho) {
		this.pho = pho;
	}
	public double getTheta() {
		return theta;
	}
	public void setTheta(double theta) {
		this.theta = theta;
	}
	public long getTimeslot() {
		return timeslot;
	}
	public void setTimestamp(long timeslot) {
		this.timeslot = timeslot;
	}
}
