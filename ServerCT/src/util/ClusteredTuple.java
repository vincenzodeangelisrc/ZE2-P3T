package util;

public class ClusteredTuple {
	
	String ephemeralId;
	String digest;

	long timeslot;
	public ClusteredTuple(String ephemeralId, String digest, long timeslot) {
		super();
		this.ephemeralId = ephemeralId;
		this.digest = digest;

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

	public long getTimeslot() {
		return timeslot;
	}
	public void setTimeslot(long timeslot) {
		this.timeslot = timeslot;
	}
	
}
