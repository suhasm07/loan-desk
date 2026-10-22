package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes passwords with PBKDF2 (HMAC-SHA256) from the Java standard library.
 * Every account gets its own random salt, so two people with the same password
 * end up with different hashes.
 */
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH_BYTES = 16;
    public static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH_BITS = 256;

    // Makes a new random salt and returns it as text
    public static String makeSalt() {
        byte[] saltBytes = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    // Hashes a password with the normal settings (used for signup and password changes)
    public static String hashPassword(String password, String saltText) {
        return hashPassword(password, saltText, ITERATIONS, KEY_LENGTH_BITS);
    }

    private static String hashPassword(String password, String saltText, int iterations, int keyLengthBits) {
        byte[] saltBytes = Base64.getDecoder().decode(saltText);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, keyLengthBits);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hashBytes = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Could not hash the password: " + e.getMessage());
        } finally {
            // Wipe the copy of the password held by the key spec
            spec.clearPassword();
        }
    }

    // Checks a typed password against the saved hash, using the saved salt and iteration count
    public static boolean passwordMatches(String password, String saltText, int iterations, String savedHash) {
        byte[] savedHashBytes = Base64.getDecoder().decode(savedHash);
        int keyLengthBits = savedHashBytes.length * 8;

        String newHash = hashPassword(password, saltText, iterations, keyLengthBits);
        byte[] newHashBytes = Base64.getDecoder().decode(newHash);

        // isEqual takes the same time whether or not the bytes match
        return MessageDigest.isEqual(savedHashBytes, newHashBytes);
    }
}