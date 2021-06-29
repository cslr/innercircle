package fi.novelinsight;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PassphraseWindow {

	protected Shell shell;
	private Text textPassphrase1;
	private Text textPassphrase2;
	
	private Button btnOk;
	private boolean buttonPressed = false;
	private String passphrase = null;

	/**
	 * Launch the application.
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		try {
			PassphraseWindow window = new PassphraseWindow();
			Shell parent = null;
			window.open(parent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

	/**
	 * Open the window.
	 */
	public String open(Shell parent) {
		Display display = Display.getDefault();
		createContents(parent);
		shell.open();
		shell.layout();
		while (!shell.isDisposed() && this.buttonPressed == false) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		if(this.buttonPressed) {
			return passphrase;
		}
		else return null;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents(Shell parent) {
		shell = new Shell(parent);
		shell.setSize(650, 300);
		shell.setText("Type a passphrase..");
		
		Image icon = new Image(Display.getDefault(), "encrypt_icon.png");
		shell.setImage(icon);
		
		shell.setToolTipText("Preferrably use more than 5 words in your passphrase to increase randomness.");
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.horizontalSpacing = 2;
		shell.setLayout(gl_shell);
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_1.setBounds(0, 0, 64, 64);
		GridLayout gl_composite_1 = new GridLayout(1, false);
		gl_composite_1.verticalSpacing = 1;
		gl_composite_1.horizontalSpacing = 1;
		composite_1.setLayout(gl_composite_1);
		
		Label lblPassphrase = new Label(composite_1, SWT.NONE);
		lblPassphrase.setText("Type a passphrase:");
		
		textPassphrase1 = new Text(composite_1, SWT.BORDER);
		textPassphrase1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {

				if(textPassphrase1 != null && textPassphrase2 != null) {
					String p1 = textPassphrase1.getText();
					String p2 = textPassphrase2.getText();
					
					if(p1.equals(p2) && p1.length() != 0) {
						if(btnOk != null) btnOk.setEnabled(true);
					}
					else {
						if(btnOk != null) btnOk.setEnabled(false);
					}
				}	
			}
		});
		textPassphrase1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		textPassphrase1.setToolTipText("Preferrably use more than 5 words in your passphrase to increase randomness.");
		
		Label lblPassphraseagain = new Label(composite_1, SWT.NONE);
		lblPassphraseagain.setText("Type a passphrase (again):");
		
		textPassphrase2 = new Text(composite_1, SWT.BORDER);
		textPassphrase2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {

				if(textPassphrase1 != null && textPassphrase2 != null) {
					String p1 = textPassphrase1.getText();
					String p2 = textPassphrase2.getText();
					
					if(p1.equals(p2) && p1.length() != 0) {
						if(btnOk != null) btnOk.setEnabled(true);
					}
					else {
						if(btnOk != null) btnOk.setEnabled(false);
					}
				}
				
			}
		});
		textPassphrase2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		textPassphrase2.setToolTipText("Preferrably use more than 5 words in your passphrase to increase randomness.");
		
		Composite composite = new Composite(composite_1, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1));
		
		btnOk = new Button(composite, SWT.CENTER);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if(textPassphrase1 != null) {
					buttonPressed = true;
					passphrase = textPassphrase1.getText();
				}
				
				shell.close();
			}
		});
		btnOk.setBounds(0, 0, 90, 30);
		btnOk.setText("OK");
		btnOk.setEnabled(false);

	}
}
