package com.hkamran.mocking.gui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.hkamran.mocking.FilterManager;

public class SettingsPage {

	protected Shell shlSettings;
	Display display;
	private Text redirectHostInput;
	private Text redirectPortInput;
	FilterManager filter;


	public SettingsPage(Shell shell, FilterManager filter) {
		this.shlSettings = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.filter = filter;
	}

	public SettingsPage() {
		this.shlSettings = new Shell();
	}
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SettingsPage window = new SettingsPage();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlSettings.open();
		shlSettings.layout();
		while (!shlSettings.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlSettings.setSize(450, 153);
		shlSettings.setText("Settings");
		shlSettings.setLayout(new FormLayout());

		CLabel lblNewLabel = new CLabel(shlSettings, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.right = new FormAttachment(0, 104);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Redirect Host:");

		CLabel lblRedirectPort = new CLabel(shlSettings, SWT.NONE);
		FormData fd_lblRedirectPort = new FormData();
		fd_lblRedirectPort.top = new FormAttachment(0, 37);
		fd_lblRedirectPort.left = new FormAttachment(0, 10);
		fd_lblRedirectPort.right = new FormAttachment(0, 104);
		lblRedirectPort.setLayoutData(fd_lblRedirectPort);
		lblRedirectPort.setText("Redirect Port:");

		redirectHostInput = new Text(shlSettings, SWT.BORDER);
		FormData fd_redirectHostInput = new FormData();
		fd_redirectHostInput.top = new FormAttachment(0, 10);
		fd_redirectHostInput.left = new FormAttachment(lblNewLabel);
		fd_redirectHostInput.bottom = new FormAttachment(lblNewLabel, 0, SWT.BOTTOM);
		fd_redirectHostInput.right = new FormAttachment(100, -10);
		redirectHostInput.setLayoutData(fd_redirectHostInput);

		redirectPortInput = new Text(shlSettings, SWT.BORDER);
		FormData fd_redirectPortInput = new FormData();
		fd_redirectPortInput.left = new FormAttachment(redirectHostInput, 0, SWT.LEFT);
		fd_redirectPortInput.top = new FormAttachment(redirectHostInput, 6);
		fd_redirectPortInput.right = new FormAttachment(100, -10);
		redirectPortInput.setLayoutData(fd_redirectPortInput);

		Button btnNewButton = new Button(shlSettings, SWT.NONE);

		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.bottom = new FormAttachment(100, -10);
		fd_btnNewButton.right = new FormAttachment(redirectHostInput, 0, SWT.RIGHT);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Save");

		redirectHostInput.setText(filter.redirectHost);
		redirectPortInput.setText(filter.redirectPort.toString());
		
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String host = redirectHostInput.getText();
					Integer port = Integer.parseInt(redirectPortInput.getText());
					filter.setRedirectInfo(host, port);
					shlSettings.dispose();
				} catch (NumberFormatException c) {
					c.printStackTrace();
				}
			}
		});

	}
}
