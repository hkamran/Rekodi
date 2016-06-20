package com.hkamran.mocking.gui;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.http.entity.ContentType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.hkamran.mocking.Response;

public class ResponsePage {

	protected Shell shlEditResponse;
	private Text txtContent;
	Response response;

	public ResponsePage(Shell shell, Response response) {
		this.shlEditResponse = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.response = response;
	}

	public ResponsePage() {
		this.shlEditResponse = new Shell();
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ResponsePage window = new ResponsePage();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void open(Shell shell, Response response) {
		ResponsePage page = new ResponsePage(shell, response);
		page.open();
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlEditResponse.open();
		shlEditResponse.layout();
		while (!shlEditResponse.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlEditResponse = new Shell();
		shlEditResponse.setSize(410, 520);
		shlEditResponse.setText("Edit Response");
		shlEditResponse.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(shlEditResponse, SWT.BORDER);
		composite.setLayout(new FormLayout());

		Label lblStatus = new Label(composite, SWT.NONE);
		FormData fd_lblStatus = new FormData();
		fd_lblStatus.top = new FormAttachment(0, 15);
		lblStatus.setLayoutData(fd_lblStatus);
		lblStatus.setText("Status:");

		Label lblProtocol = new Label(composite, SWT.NONE);
		lblProtocol.setText("Protocol: ");
		FormData fd_lblProtocol = new FormData();
		fd_lblProtocol.top = new FormAttachment(lblStatus, 18);
		fd_lblProtocol.left = new FormAttachment(lblStatus, 0, SWT.LEFT);
		lblProtocol.setLayoutData(fd_lblProtocol);

		Label lblContenttype = new Label(composite, SWT.NONE);
		fd_lblStatus.left = new FormAttachment(lblContenttype, 0, SWT.LEFT);
		lblContenttype.setText("Content-Type: ");
		FormData fd_lblContenttype = new FormData();
		fd_lblContenttype.top = new FormAttachment(lblProtocol, 20);
		fd_lblContenttype.left = new FormAttachment(0, 10);
		lblContenttype.setLayoutData(fd_lblContenttype);

		Label lblContent = new Label(composite, SWT.NONE);
		lblContent.setText("Content: ");
		FormData fd_lblContent = new FormData();
		fd_lblContent.top = new FormAttachment(lblContenttype, 22);
		fd_lblContent.left = new FormAttachment(lblStatus, 0, SWT.LEFT);
		lblContent.setLayoutData(fd_lblContent);

		final Combo protocolCombo = new Combo(composite, SWT.READ_ONLY);
		protocolCombo.setItems(new String[] { "HTTP/1.1", "HTTP/1.0" });
		FormData fd_protocolCombo = new FormData();
		fd_protocolCombo.left = new FormAttachment(0, 108);
		fd_protocolCombo.right = new FormAttachment(100, -10);
		protocolCombo.setLayoutData(fd_protocolCombo);
		protocolCombo.select(0);

		final Combo statusCodeCombo = new Combo(composite, SWT.READ_ONLY);
		fd_protocolCombo.top = new FormAttachment(statusCodeCombo, 10);
		statusCodeCombo.setItems(new String[] { "100", "101", "102", "200", "201", "202", "203", "204", "205", "206", "207", "300", "301", "302",
				"303", "304", "305", "307", "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414",
				"415", "416", "417", "422", "423", "424", "425", "426", "428", "429", "431", "500", "501", "502", "503", "504", "505", "506", "507",
				"510", "511" });
		FormData fd_statusCodeCombo = new FormData();
		fd_statusCodeCombo.left = new FormAttachment(lblStatus, 63);
		fd_statusCodeCombo.right = new FormAttachment(100, -10);
		fd_statusCodeCombo.bottom = new FormAttachment(0, 35);
		fd_statusCodeCombo.top = new FormAttachment(0, 12);
		statusCodeCombo.setLayoutData(fd_statusCodeCombo);
		statusCodeCombo.select(3);

		final Combo contentTypeCombo = new Combo(composite, SWT.READ_ONLY);
		fd_protocolCombo.bottom = new FormAttachment(contentTypeCombo, -12);
		contentTypeCombo.setItems(new String[] { "NONE", "application/atom+xml", "application/x-www-form-urlencoded", "application/json",
				"application/octet-stream", "application/svg+xml", "application/xhtml+xml", "application/xml", "multipart/form-data", "text/html",
				"text/plain", "text/xml", "*/*" });
		FormData fd_contentTypeCombo = new FormData();
		fd_contentTypeCombo.bottom = new FormAttachment(100, -403);
		fd_contentTypeCombo.top = new FormAttachment(0, 80);
		fd_contentTypeCombo.left = new FormAttachment(lblContenttype, 18);
		fd_contentTypeCombo.right = new FormAttachment(100, -10);
		contentTypeCombo.setLayoutData(fd_contentTypeCombo);
		contentTypeCombo.select(0);

		txtContent = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		FormData fd_txtContent = new FormData();
		fd_txtContent.top = new FormAttachment(lblContent, 6);
		fd_txtContent.left = new FormAttachment(lblStatus, 0, SWT.LEFT);
		fd_txtContent.right = new FormAttachment(protocolCombo, 0, SWT.RIGHT);
		txtContent.setLayoutData(fd_txtContent);

		Button btnSave = new Button(composite, SWT.NONE);
		fd_txtContent.bottom = new FormAttachment(btnSave, -6);

		btnSave.setText("Save");
		FormData fd_btnSave = new FormData();
		fd_btnSave.top = new FormAttachment(contentTypeCombo, 359);
		fd_btnSave.bottom = new FormAttachment(contentTypeCombo, 393, SWT.BOTTOM);
		fd_btnSave.left = new FormAttachment(protocolCombo, -57);
		fd_btnSave.right = new FormAttachment(100, -10);
		btnSave.setLayoutData(fd_btnSave);

		statusCodeCombo.select(statusCodeCombo.indexOf(response.getStatus().toString()));
		protocolCombo.select(protocolCombo.indexOf(response.getProtocol().toString()));

		txtContent.setText(response.getContent());

		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String version = protocolCombo.getItem(protocolCombo.getSelectionIndex());
				Integer status = Integer.parseInt(statusCodeCombo.getItem(statusCodeCombo.getSelectionIndex()));

				HttpVersion httpVersion = HttpVersion.valueOf(version);
				HttpResponseStatus statusCode = HttpResponseStatus.valueOf(status);
				ContentType contentType = ContentType.parse(contentTypeCombo.getItem(contentTypeCombo.getSelectionIndex()));
				String content = txtContent.getText();

				System.out.println(response);
				
				shlEditResponse.dispose();
			}
		});

	}
}
