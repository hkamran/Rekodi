package com.hkamran.mocking.gui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;

public class AboutPage {

	protected Shell shlAbout;
	private Text txtEclipseJavaEe;

	public AboutPage(Shell shell) {
		this.shlAbout = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		;
	}

	public AboutPage() {
		this.shlAbout = new Shell();
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AboutPage window = new AboutPage();
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
		shlAbout.open();
		shlAbout.layout();
		while (!shlAbout.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlAbout.setSize(317, 140);
		shlAbout.setText("About Service Recorder");

		txtEclipseJavaEe = new Text(shlAbout, SWT.READ_ONLY | SWT.WRAP);
		txtEclipseJavaEe.setText("Record and Playback  HTTP SOAP and REST calls.\r\n\r\n\r\nhttps://github.com/hkamran");
		txtEclipseJavaEe.setBounds(10, 10, 414, 215);

	}
}
