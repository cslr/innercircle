package fi.novelinsight;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.ArrayList;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;


public class CryptoEngineJava implements CryptoEngineInterface {
	
	// unhandled messages
	private ArrayList<String> messages = new ArrayList<String>();
	
	public synchronized boolean addMessage(String m) {
		messages.add(m);
		return true;
	}
	
	
	protected byte[] encryptBytes(byte[] data, String passphrase)
	{
		// 0. calculate SHA-512 from passphrase and combine it later with SALT1 and SALT2 to get 512 bit key
		byte[] messageDigest;
		
		try {
			// getInstance() method is called with algorithm SHA-512
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			
			// digest() method is called
			// to calculate message digest of the input string
			// returned as array of byte
			messageDigest = md.digest(passphrase.getBytes("UTF-8"));
		}
		catch (Exception e) {
			return null;
		}

		
		// 1. encrypt data using AES-256 twice
		try {
			// 128 bit IV for 128 bit blocks
			byte[] iv1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			byte[] iv2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			
			// 256 bit SALT for 256 bit keys
			byte[] salt1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			byte[] salt2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			
			SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
			rng.nextBytes(iv1);
			rng.nextBytes(iv2);
			rng.nextBytes(salt1);
			rng.nextBytes(salt2);
			
			byte[] crypted_salt1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			byte[] crypted_salt2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			
			for(int i=0;i<salt1.length;i++) {
				crypted_salt1[i] = (byte)(messageDigest[i] ^ salt1[i]);
				crypted_salt2[i] = (byte)(messageDigest[i+32] ^ salt2[i]);
			}

			
			IvParameterSpec ivspec1 = new IvParameterSpec(iv1);
			IvParameterSpec ivspec2 = new IvParameterSpec(iv2);
			
			SecretKeyFactory factory1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec1 = new PBEKeySpec(passphrase.toCharArray(), crypted_salt1, 65536, 256);
			SecretKey tmp1 = factory1.generateSecret(spec1);
			SecretKeySpec secretKey1 = new SecretKeySpec(tmp1.getEncoded(), "AES");
		 
			Cipher cipher1 = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher1.init(Cipher.ENCRYPT_MODE, secretKey1, ivspec1);
			
			SecretKeyFactory factory2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec2 = new PBEKeySpec(passphrase.toCharArray(), crypted_salt2, 65536, 256);
			SecretKey tmp2 = factory2.generateSecret(spec2);
			SecretKeySpec secretKey2 = new SecretKeySpec(tmp2.getEncoded(), "AES");
		 
			Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher2.init(Cipher.ENCRYPT_MODE, secretKey2, ivspec2);
			
			// compresses message to increase input entropy
			byte[] messageBytes = data; // filename.getBytes(StandardCharsets.UTF_8);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DeflaterOutputStream delf = new DeflaterOutputStream(out);
			delf.write(messageBytes);
			delf.flush();
			delf.close();
			byte[] compressedBytes = out.toByteArray();
			
			byte[] cryptedBytes = cipher2.doFinal(cipher1.doFinal(compressedBytes));
			
			// add IVs and SALTs to initial part of datastream
			byte[] allByteArray = new byte[iv1.length + salt1.length + iv2.length + salt2.length + cryptedBytes.length];
			ByteBuffer buff = ByteBuffer.wrap(allByteArray);
			buff.put(iv1);
			buff.put(salt1);
			buff.put(iv2);
			buff.put(salt2);
			buff.put(cryptedBytes);
			
			byte[] combined = buff.array();
			
			// return combined;

			String cryptedMessage = Base64.getEncoder().encodeToString(combined);
			
			// split Base64 encoded string to 64 char lines separated by "\n" newline
			{
				int size = 64;
				
				String cutString = "";
				
				for (int start = 0; start < cryptedMessage.length(); start += size) {
			        cutString = cutString + cryptedMessage.substring(start, Math.min(cryptedMessage.length(), start + size)) + "\n";
			    }
				
				cryptedMessage = cutString;
			}

			System.out.println( cryptedMessage );
			
			return cryptedMessage.getBytes("UTF-8");
/*			
			this.startFileDecryption(cryptedMessage, passphrase);
			
			return true;
*/
		}
		catch(Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}
	
	
	protected byte[] decryptBytes(byte[] data, String passphrase)
	{
		// 0. calculate SHA-512 from passphrase and combine it later with SALT1 and SALT2 to get 512 bit key
		byte[] messageDigest;
		
		try {
			// getInstance() method is called with algorithm SHA-512
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			
			// digest() method is called
			// to calculate message digest of the input string
			// returned as array of byte
			messageDigest = md.digest(passphrase.getBytes("UTF-8"));
		}
		catch (Exception e) {
			return null;
		}
		
		
		try {
			// 128 bit IV for 128 bit blocks
			byte[] iv1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			byte[] iv2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			
			// 256 bit SALT for 256 bit keys
			byte[] salt1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			byte[] salt2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			
			String strdata = new String(data, "UTF-8");
			strdata = strdata.replaceAll("\n", "");
			strdata = strdata.replaceAll("\r", "");
			
			System.out.println("Stringified data: '" + strdata + "'");

			// read IVs and SALT and encrypted message from the encrypted message
			byte[] allByteArray;
			try {
				allByteArray = Base64.getDecoder().decode(strdata);
			}
			catch(IllegalArgumentException e) {
				// addMessage("Bad encrypted data cannot decode input.");
				return null;
			}
			
			if(allByteArray.length <= (iv1.length+salt1.length+iv2.length+salt2.length)){
				return null;
			}
			
			byte[] cryptedBytes = new byte[allByteArray.length - iv1.length - iv2.length - salt1.length - salt2.length];
			
			ByteBuffer buff = ByteBuffer.wrap(allByteArray);
			buff.get(iv1);
			buff.get(salt1);
			buff.get(iv2);
			buff.get(salt2);
			buff.get(cryptedBytes);
			
			IvParameterSpec ivspec1 = new IvParameterSpec(iv1);
			IvParameterSpec ivspec2 = new IvParameterSpec(iv2);
			
			byte[] crypted_salt1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			byte[] crypted_salt2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			
			for(int i=0;i<salt1.length;i++) {
				crypted_salt1[i] = (byte)(messageDigest[i] ^ salt1[i]);
				crypted_salt2[i] = (byte)(messageDigest[i+32] ^ salt2[i]);
			}
			

			SecretKeyFactory factory1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec1 = new PBEKeySpec(passphrase.toCharArray(), crypted_salt1, 65536, 256);
			SecretKey tmp1 = factory1.generateSecret(spec1);
			SecretKeySpec secretKey1 = new SecretKeySpec(tmp1.getEncoded(), "AES");
			
			Cipher cipher1 = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher1.init(Cipher.DECRYPT_MODE, secretKey1, ivspec1);
			
			SecretKeyFactory factory2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec2 = new PBEKeySpec(passphrase.toCharArray(), crypted_salt2, 65536, 256);
			SecretKey tmp2 = factory2.generateSecret(spec2);
			SecretKeySpec secretKey2 = new SecretKeySpec(tmp2.getEncoded(), "AES");
			
			Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher2.init(Cipher.DECRYPT_MODE, secretKey2, ivspec2);

			byte[] compressedBytes = cipher1.doFinal(cipher2.doFinal(cryptedBytes));
			
			// decompresses message to increase input entropy
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InflaterOutputStream infl = new InflaterOutputStream(out);
			infl.write(compressedBytes);
			infl.flush();
			infl.close();
			byte[] messageBytes = out.toByteArray();

			
			String originalMessage = new String(messageBytes, "UTF-8");
			
			System.out.println("Decrypted message: " + originalMessage);
				
			return messageBytes;
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
			return null;
		}
				
	}
	
	
	// returns encrypted message in Base64 ASCII format
	public String encryptMessage(String message, String passphrase)
	{
		try {
			if(message == null || passphrase == null) return null;
			if(message == "" || passphrase == "") return null;
			
			byte[] tmp = encryptBytes(message.getBytes("UTF-8"), passphrase);
			
			if(tmp == null) {
				addMessage("Bad passphrase or data. Cannot encrypt message.");
				return null;
			}
			
			String encrypted = new String(tmp, "UTF-8");
			
			addMessage("Encrypted message:\n" + encrypted);
			
			return encrypted;
		}
		catch(UnsupportedEncodingException e) {
			System.out.println("Exception: " + e.toString());
			return null; 
		}
	}
	

	// returns encrypted message in Base64 ASCII format
	public String decryptMessage(String message, String passphrase)
	{
		try {
			if(message == null || passphrase == null) return null;
			if(message == "" || passphrase == "") return null;
			
			byte[] tmp = decryptBytes(message.getBytes("UTF-8"), passphrase);
			
			if(tmp == null) {
				addMessage("Bad passphrase or data. Cannot decrypt message.");
				return null;
			}
			
			String decrypted = new String(tmp, "UTF-8");
			
			addMessage("Decrypted message:\n" + decrypted + "\n");

			return decrypted;
		}
		catch(UnsupportedEncodingException e) {
			System.out.println("Exception: " + e.toString());
			return null; 
		}		
	}

	
	
	@Override
	public boolean startFileEncryption(String filename, String passphrase) 
	{
		try {
			if(filename == null || passphrase == null) return false;
			if(filename == "" || passphrase == "") return false;
			
			File file;
			byte[] fileContent;
			
			try {
				file = new File(filename);
				fileContent = Files.readAllBytes(file.toPath());
			}
			catch(Exception e) {
				addMessage("File encryption failed. Cannot open file: " + filename + ".");
				System.out.println("Exception: " + e.toString());
				return false;				 
			}
			
			byte[] encryptedBytes = this.encryptBytes(fileContent, passphrase);
			
			String encryptedFilename = filename + ".encrypted";
			
			File encryptedFile;
			
			try {
				encryptedFile = new File(encryptedFilename);
			
				Files.write(encryptedFile.toPath(), encryptedBytes);
			}
			catch(Exception e) {
				addMessage("File encryption failed. Cannot write file: " + encryptedFilename + ".");
				System.out.println("Exception: " + e.toString());
				return false;				 				
			}
			
			addMessage("File " + filename + " encrypted to " + encryptedFilename + ".\n");
			
			return true;
		}
		catch(Exception e) {
			addMessage("File encryption failed.");
			System.out.println("Exception: " + e.toString());
			return false;
		}
		
	}

	@Override
	public boolean encryptionRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean encryptionEnded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopEncryption() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startFileDecryption(String filename, String passphrase) 
	{
		try {
			if(filename == null || passphrase == null) return false;
			if(filename == "" || passphrase == "") return false;
			
			File file;
			byte[] fileContent;
			
			try {
				file = new File(filename);
				fileContent = Files.readAllBytes(file.toPath());
			}
			catch(Exception e) {
				addMessage("File decryption failed. Cannot read file: " + filename + ".");
				System.out.println("Exception: " + e.toString());
				return false;
			}
			
			byte[] decryptedBytes = this.decryptBytes(fileContent, passphrase);
			
			String decryptedFilename = "";
			
			if(filename.endsWith(".encrypted")) {
				decryptedFilename = filename.substring(0, filename.length() - 10);
			}
			else {
				decryptedFilename = filename + ".decrypted";
			}
			
			// filename + ".encrypted";
			File decryptedFile;
			
			try {
				decryptedFile = new File(decryptedFilename);
			
				Files.write(decryptedFile.toPath(), decryptedBytes);
			}
			catch(Exception e) {
				addMessage("File decryption failed. Cannot write file: " + decryptedFilename + ".");
				System.out.println("Exception: " + e.toString());
				return false;
			}
			
			addMessage("File " + filename + " decrypted to " + decryptedFilename + ".\n");
			
			return true;
		}
		catch(Exception e) {
			addMessage("File decryption failed.");
			System.out.println("Exception: " + e.toString());
			return false;
		}
	}

	@Override
	public boolean decryptionRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean decryptionEnded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopDecryption() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public synchronized String getMessages() {
		if(messages.isEmpty()) return "";
		
		String m = (String)messages.remove(0);
		
		return m;
	}

}
