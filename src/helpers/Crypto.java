package helpers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class Crypto {
	
	public static String saltHashString(String salt, String password) {
		return sha512(salt+password);
	}
	
	public static String sha512(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String createSalt(int length) {
		String session = "";
		try {
			SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
			int[] ints = secureRandomGenerator.ints(length, 0, Helpers.getAllowedSessionChars().size()).toArray();
			for (int i = 0; i < ints.length; i++) {		
				session += Helpers.getAllowedSessionChars().get(ints[i]); 
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return session;
	}
}
