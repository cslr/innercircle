package fi.novelinsight;

public class InnerCircleModel {
	
	public String versionNumber = "v0.901 beta";
	
	protected String filename = null;
	protected String passphrase = null;
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String file) {
		filename = file;
	}
	
	public String getPassphrase() {
		return passphrase;
	}
	
	public void setPassphrase(String pass) {
		passphrase = pass;
	}

}
