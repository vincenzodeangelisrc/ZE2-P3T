package util;

public class RestrictedTuple {

	String ephemeralId;
	String digest;

	long timestamp;
	public RestrictedTuple(String ephemeralId, String digest, long timestamp) {
		super();
		this.ephemeralId = ephemeralId;
		this.digest = digest;

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

	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
