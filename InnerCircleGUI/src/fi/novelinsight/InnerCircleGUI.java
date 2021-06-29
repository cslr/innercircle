package fi.novelinsight;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.StyledText;

import java.io.File;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;


public class InnerCircleGUI {
	
	protected InnerCircleModel model = new InnerCircleModel();;
	protected CryptoEngineInterface engine = new CryptoEngineJava();

	protected Shell shlInnerCircle;
	private Text txtFile;
	private Text txtPassphrase;
	
	protected Button btnEncrypt;
	protected Button btnDecrypt;
	protected Button btnStop;
	
	protected MenuItem mntmSetPassphrase_1;
	protected MenuItem mntmSelectFile_1;
	
	protected StyledText styledText;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			InnerCircleGUI window = new InnerCircleGUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlInnerCircle.open();
		shlInnerCircle.layout();
		
		
		Thread updaterThread = new Thread(){
			public void run(){
				while(true){
					
					Display.getDefault().syncExec(new Runnable(){
						public void run(){
							if(!shlInnerCircle.isDisposed()){
								boolean busy = engine.encryptionRunning() || engine.decryptionRunning();
								
								if(busy){
									btnEncrypt.setEnabled(false);
									btnDecrypt.setEnabled(false);
									btnStop.setEnabled(true);
									
									mntmSetPassphrase_1.setEnabled(false);
									mntmSelectFile_1.setEnabled(false);
								}
								else{
									btnEncrypt.setEnabled(true);
									btnDecrypt.setEnabled(true);
									btnStop.setEnabled(false);
									
									mntmSetPassphrase_1.setEnabled(true);
									mntmSelectFile_1.setEnabled(true);
								}
								
								String engineMessage = engine.getMessages();
								
								while(engineMessage != null) {
									if(engineMessage.equals("")) break;
									
									String messages = styledText.getText();
									if(messages.equals("")) messages = engineMessage;
									else messages = messages + "\n" + engineMessage;
									
									styledText.setText(messages);
									styledText.setTopIndex(styledText.getLineCount()-1);
									
									engineMessage = engine.getMessages();
								}
							}
						}
					});
					
					try{ Thread.sleep(10); } // 10ms
					catch(InterruptedException e){ }
				}
			}
		};
		
		updaterThread.setDaemon(true);
		updaterThread.start();
		
		
		while (!shlInnerCircle.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlInnerCircle = new Shell();
		shlInnerCircle.setSize(650, 620);
		shlInnerCircle.setText("Inner Circle encryption tool");
		Display display = Display.getDefault();
		display.setAppName("Inner Circle");
		display.setAppVersion(model.versionNumber);
		
		shlInnerCircle.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		String path = System.getProperty("user.dir");
		Image icon = new Image(Display.getDefault(), path + File.separator + "encrypt_icon.png");
		shlInnerCircle.setImage(icon);
		
		Menu menu = new Menu(shlInnerCircle, SWT.BAR);
		shlInnerCircle.setMenuBar(menu);
		
		MenuItem mntmNewSubmenu_1 = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu_1.setText("File");
		
		Menu menu_1_1 = new Menu(mntmNewSubmenu_1);
		mntmNewSubmenu_1.setMenu(menu_1_1);
		
		mntmSelectFile_1 = new MenuItem(menu_1_1, SWT.NONE);
		mntmSelectFile_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Select a file to be encrypted..");
				
				FileDialog dialog = new FileDialog(shlInnerCircle, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.*" });
				// dialog.setFilterNames(new String[] { "All files" });
				
				if(dialog.open() != null){
					String filename = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
					
					System.out.println("File selected: " + filename);
					
					model.setFilename(filename);
					
					txtFile.setText(model.getFilename());
				}
			}
		});
		mntmSelectFile_1.setToolTipText("Select a file to be encypted/decrypted.");
		mntmSelectFile_1.setText("Select a file..");
		
		mntmSetPassphrase_1 = new MenuItem(menu_1_1, SWT.NONE);
		mntmSetPassphrase_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Set a passhrase for encryption.");
				
				PassphraseWindow window = new PassphraseWindow();
				String passphrase = window.open(shlInnerCircle);
				
				if(passphrase != null) {
					model.setPassphrase(passphrase);
					txtPassphrase.setText(model.getPassphrase());
				}
			}
		});
		mntmSetPassphrase_1.setToolTipText("Set a secret passphrase to encrypt/decrypt file.");
		mntmSetPassphrase_1.setText("Set a passphrase..");
		
		MenuItem menuItem = new MenuItem(menu_1_1, SWT.SEPARATOR);
		
		MenuItem mntmExit_1 = new MenuItem(menu_1_1, SWT.NONE);
		mntmExit_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		mntmExit_1.setToolTipText("Exits program.");
		mntmExit_1.setText("Exit");
		
		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("Help");
		
		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);
		
		
		MenuItem mntmSelectHelp = new MenuItem(menu_1, SWT.NONE);
		mntmSelectHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Show help..");
				String path = System.getProperty("user.dir");
				org.eclipse.swt.program.Program.launch(path + File.separator + "help" + File.separator + "index.html");
			}
		});
		mntmSelectHelp.setText("Help..");
		
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		
		MenuItem mntmAbout = new MenuItem(menu_1, SWT.NONE);
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("About dialog..");
				
				MessageBox aboutbox = new MessageBox(shlInnerCircle, SWT.ICON_INFORMATION | SWT.OK);
				aboutbox.setText("About Novel Insight Inner Circle..");
				aboutbox.setMessage(
						"Inner Circle encryptor tool (" + model.versionNumber + ") is developed by Novel Insight (www.novelinsight.fi). It is copyright Tomas Ukkonen 2021.\n\n" + 
						"The encryption is AES 256bit that is used twice to extend to non-standard AES 512bit encryption (CBC mode). " + 
						"This means MITM attack complexity is still 2^256 so the effective key size is 256bit but AES is used in a non-standard way " + 
						"forcing attacker to specifically design attack code for this cipher.\n\n" + 
						"The software is free to use but please help me (tomas.ukkonen@iki.fi) to get a job and rid of psychiatry. " + 
						"Mental health system keeps destroying me when its "
						+ "their medicines that make me crazy. Initially I had no need to psychiatry and worked as a scientist without problems but "
						+ "had stress related minor insomnia.\n");
				
				int returncode = aboutbox.open();
			}
		});
		mntmAbout.setText("About Novel Insight Inner Circle..");
		
		Composite composite = new Composite(shlInnerCircle, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(1, false);
		gl_composite_1.horizontalSpacing = 1;
		composite_1.setLayout(gl_composite_1);
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_2.setLayout(new GridLayout(2, false));
		
		Label lblFile = new Label(composite_2, SWT.NONE);
		lblFile.setText("File:");
		
		txtFile = new Text(composite_2, SWT.BORDER);
		txtFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				System.out.println("Select a file to be encrypted..");
				
				FileDialog dialog = new FileDialog(shlInnerCircle, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.*" });
				// dialog.setFilterNames(new String[] { "All files" });
				
				if(dialog.open() != null){
					String filename = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
					
					System.out.println("File selected: " + filename);
					
					model.setFilename(filename);
					
					txtFile.setText(model.getFilename());
				}
			}
		});
		
		txtFile.setEditable(false);
		txtFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if(model.getFilename() != null) {
			txtFile.setText(model.getFilename());
		}
		else {
			txtFile.setText("Select a file by clicking here or use the menu.");
		}
		
		Label lblPassphrase = new Label(composite_2, SWT.NONE);
		lblPassphrase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassphrase.setText("Passphrase:");
		
		txtPassphrase = new Text(composite_2, SWT.BORDER);
		txtPassphrase.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				System.out.println("Set passhrase for encryption.");
				
				PassphraseWindow window = new PassphraseWindow();
				String passphrase = window.open(shlInnerCircle);
				
				if(passphrase != null) {
					model.setPassphrase(passphrase);
					txtPassphrase.setText(model.getPassphrase());
				}
				
			}
		});
		
		if(model.getPassphrase() != null) {
			txtPassphrase.setText(model.getPassphrase());
		}
		else {
			txtPassphrase.setText("Set a passphrase by clicking here or use the menu.");
		}
		
		txtPassphrase.setEditable(false);
		txtPassphrase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_3 = new Composite(composite_1, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_composite_3 = new GridLayout(3, true);
		gl_composite_3.horizontalSpacing = 2;
		composite_3.setLayout(gl_composite_3);
		
		btnEncrypt = new Button(composite_3, SWT.CENTER);
		btnEncrypt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String f = model.getFilename();
				String p = model.getPassphrase();
				
				if(f == null) { engine.addMessage("Cannot encrypt: no file selected."); return; }
				if(p == null) { engine.addMessage("Cannot encrypt: no passphrase."); return; }
				if(f == "") { engine.addMessage("Cannot encrypt: no file selected."); return; }
				if(p == "") { engine.addMessage("Cannot encrypt: no passphrase."); return; }

				if(engine.startFileEncryption(f, p))
					System.out.println("Encrypt file: " + f + " using passphrase: " + p);
			}
		});
		btnEncrypt.setToolTipText("Encrypt using AES encryption.");
		btnEncrypt.setText("Encrypt file");
		
		btnDecrypt = new Button(composite_3, SWT.CENTER);
		btnDecrypt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String f = model.getFilename();
				String p = model.getPassphrase();
				
				if(f == null) { engine.addMessage("Cannot decrypt: no file selected."); return; }
				if(p == null) { engine.addMessage("Cannot decrypt: no passphrase."); return; }
				if(f == "") { engine.addMessage("Cannot decrypt: no file selected."); return; }
				if(p == "") { engine.addMessage("Cannot decrypt: no passphrase."); return; }

				if(engine.startFileDecryption(f, p))
					System.out.println("Decrypt file: " + f + " using passphrase: " + p);

			}
		});
		btnDecrypt.setToolTipText("Decrypt using AES encryption.");
		btnDecrypt.setText("Decrypt file");
		
		btnStop = new Button(composite_3, SWT.NONE);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(engine.encryptionRunning()) engine.stopEncryption();
				if(engine.decryptionRunning()) engine.stopDecryption();
			}
		});
		btnStop.setEnabled(false);
		btnStop.setText("Stop Computation");
		
		Composite composite_4 = new Composite(composite_1, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_composite_4 = new GridLayout(2, true);
		gl_composite_4.horizontalSpacing = 3;
		composite_4.setLayout(gl_composite_4);
		
		Button btnEncrypt_1 = new Button(composite_3, SWT.CENTER);
		btnEncrypt_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{	
				String p = model.getPassphrase();
				
				if(p == null) { engine.addMessage("Cannot encrypt: no passphrase."); return; }
				if(p == "") { engine.addMessage("Cannot encrypt: no passphrase."); return; }
				
				MessageWindow window = new MessageWindow();
				
				String message = window.open(shlInnerCircle, "Encrypt message");
				
				if(message == null) return;
				if(message == "") return;
				
				String encryptedMsg = engine.encryptMessage(message, model.getPassphrase());
				
				System.out.println("Encrypt message: " + encryptedMsg);
			}
		});
		btnEncrypt_1.setToolTipText("Encrypt message using AES encryption.");
		btnEncrypt_1.setText("Encrypt message..");
		
		Button btnDecrypt_1 = new Button(composite_3, SWT.CENTER);
		btnDecrypt_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String p = model.getPassphrase();
				
				if(p == null) { engine.addMessage("Cannot decrypt: no passphrase."); return; }
				if(p == "") { engine.addMessage("Cannot decrypt: no passphrase."); return; }
				
				MessageWindow window = new MessageWindow();
				
				String message = window.open(shlInnerCircle, "Decrypt message");
				
				if(message == null) return;
				if(message == "") return;
				
				String decryptedMsg = engine.decryptMessage(message, model.getPassphrase());
				
				System.out.println("Decrypt message: " + decryptedMsg);
			}
		});
		btnDecrypt_1.setToolTipText("Decrypt message using AES encryption.");
		btnDecrypt_1.setText("Decrypt message..");

		Button btnClear = new Button(composite_3, SWT.CENTER);
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				System.out.println("Clear data from the user interace");
				styledText.setText("");
				model.setPassphrase("");
				model.setFilename("");
				
				txtFile.setText("Select a file by clicking here or use the menu.");
				txtPassphrase.setText("Set a passphrase by clicking here or use the menu.");
			}
		});
		btnClear.setToolTipText("Clear data from the user inteface");
		btnClear.setText("Clear data");

		
		TextViewer textViewer = new TextViewer(composite_1, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		styledText = textViewer.getTextWidget();
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		styledText.setEditable(false);
		
	}
}
