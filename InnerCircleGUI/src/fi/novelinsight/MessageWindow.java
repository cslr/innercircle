package fi.novelinsight;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class MessageWindow {

	protected Shell shell;
	
	private boolean buttonPressed = false;
	private String secretMessage = null;
	
	Button btnNewButton;

	/**
	 * Launch the application.
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		try {
			MessageWindow window = new MessageWindow();
			Shell parent = null;
			String title = "Encrypt message..";
			window.open(parent, title);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

	/**
	 * Open the window.
	 */
	public String open(Shell parent, String title) {
		Display display = Display.getDefault();
		createContents(parent, title);
		shell.open();
		shell.layout();
		while (!shell.isDisposed() && this.buttonPressed == false) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		if(this.buttonPressed) {
			return secretMessage;
		}
		else 
			return null; // no message to encrypt/decrypt
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents(Shell parent, String windowTitle) {
		if(parent == null)
			shell = new Shell();
		else
			shell = new Shell(parent);

		shell.setSize(650, 450);
		shell.setText(windowTitle);
		
		Image icon = new Image(Display.getDefault(), "encrypt_icon.png");
		shell.setImage(icon);
		shell.setLayout(new GridLayout(1, false));
		
		TextViewer textViewer = new TextViewer(shell, SWT.BORDER | SWT.V_SCROLL);
		StyledText styledText = textViewer.getTextWidget();
		styledText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				secretMessage = styledText.getText();
				if(secretMessage == null) return;
				if(secretMessage == "") return;
				
				btnNewButton.setEnabled(true);
			}
		});
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		styledText.setBounds(0, 0, 71, 24);
		
		btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(secretMessage == null) return;
				if(secretMessage == "") return;
				
				System.out.println("Secret message to (de)crypted: " + secretMessage);
				buttonPressed = true;
				shell.close();
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnNewButton.setBounds(0, 0, 90, 30);
		btnNewButton.setText("Encrypt/Decrypt message");
		btnNewButton.setEnabled(false);

	}
}
