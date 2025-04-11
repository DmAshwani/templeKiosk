package in.dataman.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;


@Component
public class MD5Util {
    

	 public String encodeToHex(String username, String password) {
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            String input = username.toLowerCase() + password; 
	            byte[] messageDigest = md.digest(input.getBytes());
	            
	            StringBuilder hexString = new StringBuilder();
	            for (byte b : messageDigest) {
	                String hex = Integer.toHexString(0xff & b);
	                if (hex.length() == 1) hexString.append('0');
	                hexString.append(hex);
	            }
	            return hexString.toString().toUpperCase(); 
	        } catch (NoSuchAlgorithmException e) {
	            throw new RuntimeException(e);
	        }
	    }
}
