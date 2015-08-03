package com.hkamran.mocking.gui;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.http.entity.ContentType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
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
import org.eclipse.wb.swt.SWTResourceManager;

import com.hkamran.mocking.Request;
import com.hkamran.mocking.Request.MATCHTYPE;

public class RequestPage {

	protected Shell shlEditRequest;
	private Text contentText;
	private Text matchedString;
	private Text uriText;
	public Request request;
	
	public RequestPage(Shell shell, Request request) {
		this.shlEditRequest = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.request = request;
	}

	public RequestPage() {
		this.shlEditRequest = new Shell();
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RequestPage window = new RequestPage();
			
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void open(Shell shell, Request request) {
		RequestPage page = new RequestPage(shell, request);
		page.open();
	}
 
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlEditRequest.open();
		shlEditRequest.layout();
		while (!shlEditRequest.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlEditRequest.setText("Edit Request");
		shlEditRequest.setSize(410, 520);
		shlEditRequest.setLayout(new FillLayout(SWT.HORIZONTAL));
		shlEditRequest.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		Composite composite = new Composite(shlEditRequest, SWT.BORDER);
		composite.setLayout(new FormLayout());

		final Combo contentTypeCombo = new Combo(composite, SWT.READ_ONLY);
		contentTypeCombo.setItems(new String[] { "NONE", "application/atom+xml", "application/x-www-form-urlencoded", "application/json",
				"application/octet-stream", "application/svg+xml", "application/xhtml+xml", "application/xml", "multipart/form-data", "text/html",
				"text/plain", "text/xml", "*/*" });
		FormData fd_contentTypeCombo = new FormData();
		fd_contentTypeCombo.right = new FormAttachment(100, -10);
		contentTypeCombo.setLayoutData(fd_contentTypeCombo);
		contentTypeCombo.select(0);

		contentText = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		FormData fd_contentText = new FormData();
		fd_contentText.right = new FormAttachment(100, -10);
		contentText.setLayoutData(fd_contentText);

		Label label = new Label(composite, SWT.NONE);
		fd_contentText.top = new FormAttachment(label, 6);
		fd_contentText.left = new FormAttachment(label, 0, SWT.LEFT);
		label.setText("Content:");
		FormData fd_label = new FormData();
		fd_label.left = new FormAttachment(0, 10);
		label.setLayoutData(fd_label);

		Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("Protocol:");
		FormData fd_label_1 = new FormData();
		fd_label_1.left = new FormAttachment(0, 10);
		label_1.setLayoutData(fd_label_1);

		Button btnSave = new Button(composite, SWT.NONE);
		fd_contentText.bottom = new FormAttachment(100, -50);

		FormData fd_btnSave = new FormData();
		fd_btnSave.top = new FormAttachment(contentText, 7);
		fd_btnSave.right = new FormAttachment(100, -10);
		fd_btnSave.bottom = new FormAttachment(100, -10);
		fd_btnSave.left = new FormAttachment(100, -71);
		btnSave.setLayoutData(fd_btnSave);
		btnSave.setText("Save");
		contentTypeCombo.select(contentTypeCombo.indexOf(request.getContentType().toString()));
		contentText.setText(request.getContent());

		matchedString = new Text(composite, SWT.BORDER);
		FormData fd_matchedString = new FormData();
		fd_matchedString.right = new FormAttachment(100, -10);
		matchedString.setLayoutData(fd_matchedString);
		matchedString.setText(request.matchedString);

		final Combo matchTypeCombo = new Combo(composite, SWT.READ_ONLY);
		FormData fd_matchTypeCombo = new FormData();
		fd_matchTypeCombo.right = new FormAttachment(100, -10);
		matchTypeCombo.setLayoutData(fd_matchTypeCombo);
		matchTypeCombo.setItems(new String[] { "request", "content" });
		matchTypeCombo.select(0);
		matchTypeCombo.select(matchTypeCombo.indexOf(request.getMatchType().toString()));

		final Combo protocolCombo = new Combo(composite, SWT.READ_ONLY);
		FormData fd_protocolCombo = new FormData();
		fd_protocolCombo.left = new FormAttachment(label_1, 42);
		fd_protocolCombo.right = new FormAttachment(100, -10);
		protocolCombo.setLayoutData(fd_protocolCombo);
		protocolCombo.setItems(new String[] { "HTTP/1.1", "HTTP/1.0" });
		protocolCombo.select(0);
		protocolCombo.select(protocolCombo.indexOf(request.getProtocol()));

		uriText = new Text(composite, SWT.BORDER);
		FormData fd_uriText = new FormData();
		fd_uriText.top = new FormAttachment(protocolCombo, 11);
		fd_uriText.right = new FormAttachment(100, -10);
		uriText.setLayoutData(fd_uriText);
		uriText.setText(request.getURI());

		final Combo methodCombo = new Combo(composite, SWT.READ_ONLY);
		fd_protocolCombo.top = new FormAttachment(methodCombo, 12);
		FormData fd_methodCombo = new FormData();
		fd_methodCombo.right = new FormAttachment(100, -10);
		methodCombo.setLayoutData(fd_methodCombo);
		methodCombo.setItems(new String[] { "GET", "POST", "HEAD", "PUT", "DELETE", "TRACE", "CONNECT" });
		methodCombo.select(0);

		methodCombo.select(methodCombo.indexOf(request.getMethod()));

		Label label_3 = new Label(composite, SWT.NONE);
		fd_label.top = new FormAttachment(label_3, 17);
		fd_contentTypeCombo.left = new FormAttachment(label_3, 15);
		fd_contentTypeCombo.top = new FormAttachment(label_3, -3, SWT.TOP);
		FormData fd_label_3 = new FormData();
		fd_label_3.left = new FormAttachment(0, 10);
		label_3.setLayoutData(fd_label_3);
		label_3.setText("Content Type:");

		Label lblMatchedString = new Label(composite, SWT.NONE);
		fd_matchedString.left = new FormAttachment(lblMatchedString, 6);
		fd_label_3.top = new FormAttachment(lblMatchedString, 18);
		fd_matchedString.top = new FormAttachment(lblMatchedString, -3, SWT.TOP);
		FormData fd_lblMatchedString = new FormData();
		fd_lblMatchedString.left = new FormAttachment(0, 10);
		lblMatchedString.setLayoutData(fd_lblMatchedString);
		lblMatchedString.setText("Matched String:");

		Label lblMatchType = new Label(composite, SWT.NONE);
		fd_lblMatchedString.top = new FormAttachment(lblMatchType, 19);
		fd_matchTypeCombo.left = new FormAttachment(lblMatchType, 21);
		fd_matchTypeCombo.top = new FormAttachment(lblMatchType, -3, SWT.TOP);
		FormData fd_lblMatchType = new FormData();
		fd_lblMatchType.left = new FormAttachment(0, 10);
		lblMatchType.setLayoutData(fd_lblMatchType);
		lblMatchType.setText("Match Type: ");

		Label lblUri = new Label(composite, SWT.NONE);
		fd_lblMatchType.top = new FormAttachment(lblUri, 20);
		fd_uriText.left = new FormAttachment(lblUri, 66);
		FormData fd_lblUri = new FormData();
		fd_lblUri.left = new FormAttachment(0, 10);
		fd_lblUri.top = new FormAttachment(label_1, 19);
		lblUri.setLayoutData(fd_lblUri);
		lblUri.setText("URI: ");

		Label lblMethod = new Label(composite, SWT.NONE);
		fd_methodCombo.left = new FormAttachment(lblMethod, 44);
		fd_label_1.top = new FormAttachment(lblMethod, 20);
		fd_methodCombo.top = new FormAttachment(lblMethod, -3, SWT.TOP);
		FormData fd_lblMethod = new FormData();
		fd_lblMethod.left = new FormAttachment(0, 8);
		fd_lblMethod.top = new FormAttachment(0, 22);
		lblMethod.setLayoutData(fd_lblMethod);
		lblMethod.setText("Method: ");
		
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				HttpMethod method = HttpMethod.valueOf(methodCombo.getItem(methodCombo.getSelectionIndex()));
				String uri = uriText.getText();
				HttpVersion httpVersion = HttpVersion.valueOf(protocolCombo.getItem(protocolCombo.getSelectionIndex()));
				MATCHTYPE type = MATCHTYPE.valueOf(matchTypeCombo.getItem(matchTypeCombo.getSelectionIndex()));
				String matchedStrings = matchedString.getText();
				ContentType contentType = ContentType.parse(contentTypeCombo.getItem(contentTypeCombo.getSelectionIndex()));
				String contentTexts = contentText.getText();

				request.setMethod(method);
				request.setURI(uri);
				request.setVersion(httpVersion);
				request.setMatchType(type);
				request.setMatchString(matchedStrings);
				request.setContent(contentTexts);
				request.setContentType(contentType);

				shlEditRequest.dispose();
			}

		});
	}
}
