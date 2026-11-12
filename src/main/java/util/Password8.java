package util;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Password8 {
  private static byte[] derive(String password, byte[] salt, int iterations) throws Exception {
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        .generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, 256))
        .getEncoded();
  }

  public static String hash(String password) {
    try {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      return "pbkdf2$210000$"
          + Base64.getEncoder().encodeToString(salt)
          + "$"
          + Base64.getEncoder().encodeToString(derive(password, salt, 210000));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static boolean verify(String password, String encoded) {
    try {
      String[] p = encoded.split(java.util.regex.Pattern.quote("$"));
      return p.length == 4
          && MessageDigest.isEqual(
              derive(password, Base64.getDecoder().decode(p[2]), Integer.parseInt(p[1])),
              Base64.getDecoder().decode(p[3]));
    } catch (Exception e) {
      return false;
    }
  }
}
