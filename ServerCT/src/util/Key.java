package util;

public class Key {
	long timeslot;
	String digest;
	public Key(long timeslot, String digest) {
		super();
		this.timeslot = timeslot;
		this.digest = digest;
	}
	public long getTimeslot() {
		return timeslot;
	}
	public void setTimeslot(long timeslot) {
		this.timeslot = timeslot;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
		result = prime * result + (int) (timeslot ^ (timeslot >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (digest == null) {
			if (other.digest != null)
				return false;
		} else if (!digest.equals(other.digest))
			return false;
		if (timeslot != other.timeslot)
			return false;
		return true;
	}
	
	
}
