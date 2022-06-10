package util;

public class Tuple {

	String ephemeralId;
	String digest;
	double pho;
	double theta;
	long timestamp;
	public Tuple(String ephemeralId, String digest, double pho, double theta, long timestamp) {
		super();
		this.ephemeralId = ephemeralId;
		this.digest = digest;
		this.pho = pho;
		this.theta = theta;
		this.timestamp = timestamp;
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
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
