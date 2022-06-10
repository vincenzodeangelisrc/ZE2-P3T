import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * The class Health Facility represents Health Facility who can create an RSA keypair and can issue digital signatures
 */
public class HealthFacility {
	
	public static PrivateKey getPrivate()
			  throws Exception {

				String filename = "private_key.der";
				byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

			    PKCS8EncodedKeySpec spec =
			      new PKCS8EncodedKeySpec(keyBytes);
			    KeyFactory kf = KeyFactory.getInstance("RSA");
			    return kf.generatePrivate(spec);
	}

	
	
    /**
     * Calculate mu' using the Chinese Remainder Theorem for optimization
     * Thanks to the isomorphism property f(x+y)=f(x)+f(y) we can split the mu^d modN in two:
     * one mode p , one mode q, and then we can combine the results to calculate muprime
     * @param mu
     * @return mu'
     */
    public static BigInteger calculateMuPrimeWithChineseRemainderTheorem(BigInteger mu) {
        try {

        	BigInteger N = ((RSAKey) getPrivate()).getModulus();
            BigInteger P = ((RSAPrivateCrtKey) getPrivate()).getPrimeP(); //get the prime number p used to produce the key pair

            BigInteger Q = ((RSAPrivateCrtKey) getPrivate()).getPrimeQ(); //get the prime number q used to produce the key pair

            //We split the mu^d modN in two , one mode p , one mode q

            BigInteger PinverseModQ = P.modInverse(Q); //calculate p inverse modulo q

            BigInteger QinverseModP = Q.modInverse(P); //calculate q inverse modulo p

            BigInteger d = ((RSAPrivateKey) getPrivate()).getPrivateExponent(); //get private exponent d

            //We split the message mu in to messages m1, m2 one mod p, one mod q

            BigInteger m1 = mu.modPow(d, N).mod(P); //calculate m1=(mu^d modN)modP

            BigInteger m2 = mu.modPow(d, N).mod(Q); //calculate m2=(mu^d modN)modQ

            //We combine the calculated m1 and m2 in order to calculate muprime
            //We calculate muprime: (m1*Q*QinverseModP + m2*P*PinverseModQ) mod N where N =P*Q

            return ((m1.multiply(Q).multiply(QinverseModP)).add(m2.multiply(P).multiply(PinverseModQ))).mod(N);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    /**
     * Calculate mu' using the classic private Exponent -> muprime=(mu^d modN)

     * @param mu
     * @return mu'
     */
    public static BigInteger calculateSignatureOfMessage(BigInteger mu) {
        try {
        	BigInteger N = ((RSAKey) getPrivate()).getModulus();

            BigInteger d = ((RSAPrivateKey) getPrivate()).getPrivateExponent(); //get private exponent d

            return mu.modPow(d, N);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


