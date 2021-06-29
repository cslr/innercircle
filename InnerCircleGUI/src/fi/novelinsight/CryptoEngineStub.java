package fi.novelinsight;

import java.util.ArrayList;
import java.util.Date;

public class CryptoEngineStub implements CryptoEngineInterface {
	
	private Date encryptionEndTime = new Date(System.currentTimeMillis());
	private Date decryptionEndTime = new Date(System.currentTimeMillis());
	
	private String encryptionFilename, encryptionPassphrase;
	private String decryptionFilename, decryptionPassphrase;
	
	private ArrayList<String> messages = new ArrayList<String>();
	
	// shows verbose message
	synchronized public boolean addMessage(String message) {
		if(message == null) return false;
		if(message == "") return false;
		
		messages.add(message);
		return true;
	}

	@Override
	public boolean startFileEncryption(String filename, String passphrase) {
		if(filename == null || passphrase == null) return false;
		if(filename.equals("") || passphrase.equals("")) return false;
		
		Date now = new Date(System.currentTimeMillis());
		
		if(now.before(encryptionEndTime)) return false;
		if(now.before(decryptionEndTime)) return false;
		
		encryptionEndTime = new Date(System.currentTimeMillis() + 5000); // 5 seconds from now
		
		encryptionFilename = filename;
		encryptionPassphrase = passphrase;
		
		messages.add(new String("Encryption started: " + filename));
		
		return true;
	}

	@Override
	public boolean encryptionRunning() {
		
		Date now = new Date(System.currentTimeMillis());
		
		if(now.after(encryptionEndTime)) return false;
		else return true;
	}

	@Override
	public boolean encryptionEnded() {
		
		Date now = new Date(System.currentTimeMillis());
		
		if(now.after(encryptionEndTime)) return true;
		else return false;
	}

	@Override
	public boolean stopEncryption() {
		encryptionEndTime = new Date(System.currentTimeMillis()-1);
		
		messages.add(new String("Encryption stopped."));
		
		return true;
	}
	
	
	// returns encrypted message in Base64 ASCII format
	public String encryptMessage(String message, String passphrase)
	{
		if(message == null || passphrase == null) return null;
		if(message == "" || passphrase == "") return null;
		
		return "ENCRYPTED BASE64 ASCII ARMOUR MESSAGE";
	}
	
	
	@Override
	public boolean startFileDecryption(String filename, String passphrase) {
		if(filename == null || passphrase == null) return false;
		if(filename.equals("") || passphrase.equals("")) return false;
		
		Date now = new Date(System.currentTimeMillis());
		
		if(now.before(encryptionEndTime)) return false;
		if(now.before(decryptionEndTime)) return false;
		
		decryptionEndTime = new Date(System.currentTimeMillis() + 5000); // 5 seconds from now
		
		decryptionFilename = filename;
		decryptionPassphrase = passphrase;
		
		messages.add(new String("Decryption started: " + filename));
		
		return true;
	}

	@Override
	public boolean decryptionRunning() {
		Date now = new Date(System.currentTimeMillis());
		
		if(now.after(decryptionEndTime)) return false;
		else return true;
	}

	@Override
	public boolean decryptionEnded() {
		Date now = new Date(System.currentTimeMillis());
		
		if(now.after(decryptionEndTime)) return true;
		else return false;
	}

	@Override
	public boolean stopDecryption() {
		decryptionEndTime = new Date(System.currentTimeMillis()-1);
		
		messages.add(new String("Decryption stopped."));
		
		return true;
	}
	
	// return most recent messages from the messages buffer
	synchronized public String getMessages() {
		
		if(messages.isEmpty()) return "";
		
		String m = (String)messages.remove(0);
		
		return m;
	}
	
	// returns encrypted message in Base64 ASCII format
	public String decryptMessage(String message, String passphrase)
	{
		if(message == null || passphrase == null) return null;
		if(message == "" || passphrase == "") return null;
		
		return "DECRYPTED BASE64 ASCII ARMOUR MESSAGE";
	}

}
