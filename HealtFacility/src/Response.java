import java.math.BigInteger;

public class Response {
	
	private BigInteger signature;
	
	public Response() {
		
	}

	public Response(BigInteger signature) {
		super();
		this.signature = signature;
	}

	public BigInteger getSignature() {
		return signature;
	}

	public void setSignature(BigInteger signature) {
		this.signature = signature;
	}
		

}
