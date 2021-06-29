package fi.novelinsight;

public interface CryptoEngineInterface {
	
	// shows verbose message
	public boolean addMessage(String message);
	
	public boolean startFileEncryption(String filename, String passphrase);
	
	public boolean encryptionRunning();
	
	public boolean encryptionEnded();
	
	public boolean stopEncryption();
	
	// returns encrypted message in Base64 ASCII format
	public String encryptMessage(String message, String passphrase);

	
	public boolean startFileDecryption(String filename, String passphrase);
	
	public boolean decryptionRunning();
	
	public boolean decryptionEnded();
	
	public boolean stopDecryption();
	
	// returns decrypted message from crypted Base64 ASCII format message
	public String decryptMessage(String message, String passphrase);
	
	
	// return most recent messages from the messages buffer
	public String getMessages();
}
