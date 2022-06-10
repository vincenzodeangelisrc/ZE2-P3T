package com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.unirc.tesi.marcoventura.contacttracing.cipher.MakeHash;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature.HealthFacility.getPublic;

/**
 * The class User represents User who wishes to get a signature from HF over his message
 * but without HF seeing the actual message
 */
public class User {
    static BigInteger r;

    static BigInteger m;


    private static byte[] getRandomForBlindSignature() {

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[768];
        random.nextBytes(bytes);
        return bytes;

    }
    
    private static String getRandomForDigest() {
    	
    	final char[] characters = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ".toCharArray();
        SecureRandom random = new SecureRandom();
        
        StringBuilder res = new StringBuilder(); 

        for (int i = 0; i < 64; i++)
            res.append(characters[random.nextInt(characters.length)]);
        
        return res.toString();
        
    }
    /**
     * Calculates and returns the mu
     * User uses HF's public key and a random value r, such that r is relatively prime to N
     * to compute the blinding factor r^e mod N. User then computes the blinded message mu = H(msg) * r^e mod N
     * It is important that r is a random number so that mu does not leak any information about the actual message
     * @return the blinded messahe mu
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static BigInteger calculateMessage(Context context) {
        try {
        	
        	r = new BigInteger(getRandomForBlindSignature());
        	
        	String random_msg = getRandomForDigest();
        	String digest_random = MakeHash.generateSHA(random_msg);
        	String final_msg = random_msg + digest_random;

            Log.d("Random",random_msg);
            Log.d("Digest", digest_random);
            Log.d("Final", final_msg);

            byte[] msg = final_msg.getBytes(StandardCharsets.UTF_8); //get the bytes of the hashed message

            m = new BigInteger(msg);  //create a BigInteger object based on the extracted bytes of the message

            BigInteger e = ((RSAPublicKey) getPublic(context)).getPublicExponent(); //get the public exponent 'e' of HF's key pair
            BigInteger N = ((RSAKey) getPublic(context)).getModulus(); // get modulus 'N' of the key pair

            return ((r.modPow(e, N)).multiply(m)).mod(N); //User computes message' = message * r^e mod N

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculate signature over mu'
     * User receives the signature over the blinded message that he sent to HF
     * and removes the blinding factor to compute the signature over his actual message
     * @param muprime
     * @return signature
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String signatureCalculation(BigInteger muprime, Context context) {
        try {

            BigInteger N = ((RSAKey) getPublic(context)).getModulus(); //get modulus of the key pair

            BigInteger s = r.modInverse(N).multiply(muprime).mod(N); //User computes sig = mu'*r^-1 mod N, inverse of r mod N multiplied with muprime mod N, to remove the blinding factor

            String signature = Base64.getEncoder().encodeToString(s.toByteArray()); //encode with Base64 encoding to be able to read all the symbols
            Log.d("Signature", signature);

            return signature;
        
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Checks if the signature received from HF, is a valid signature for the message given, this can be easily computed because(m^d)^e modN = m
     * @param signature
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean verify(String signature, Context context){
    	
        try{

            byte[] bytes = signature.getBytes(); //create a byte array extracting the bytes from the signature

            byte[] decodedBytes = Base64.getDecoder().decode(bytes); // decode the bytes with Base64 decoding (remember we encoded with base64 earlier)

            BigInteger sig = new BigInteger(decodedBytes); // create the BigInteger object based on the bytes of the signature

            BigInteger e = ((RSAPublicKey) getPublic(context)).getPublicExponent();//get the public exponent of HF's key pair
            BigInteger N = ((RSAKey) getPublic(context)).getModulus(); //get the modulus of HF's key pair

            BigInteger signedMessageBigInt = sig.modPow(e, N); //calculate sig^e modN, if we get back the initial message that means that the signature is valid, this works because (m^d)^e modN = m

            String signedMessage = new String(signedMessageBigInt.toByteArray()); //create a String based on the result of the above calculation
            String initialMessage = new String(m.toByteArray()); //create a String based on the initial message we wished to get a signature on

            //compare the two Strings, if they are equal the signature we got is a valid
            return signedMessage.equals(initialMessage);
            
        }catch (Exception e) {
            e.printStackTrace();
        }
		return false;
    }

}

