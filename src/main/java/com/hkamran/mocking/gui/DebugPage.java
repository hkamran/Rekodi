package com.hkamran.mocking.gui;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.http.entity.ContentType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hkamran.mocking.Debugger;
import com.hkamran.mocking.FilterManager.State;
import com.hkamran.mocking.Response;

public class DebugPage {
	Display display;
	protected Shell shell;
	final int TEXT_MARGIN = 3;
	private Text requestText;
	Debugger debugger;
	private Text contentText;

	public DebugPage(Shell shell, Debugger debug) {
		this.shell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);;
		this.debugger = debug;
	}

	public DebugPage() {
		shell = new Shell();
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DebugPage window = new DebugPage();
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
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {

		shell.setSize(589, 504);
		shell.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		shell.setText("SWT Application");
		shell.setLayout(new FormLayout());

		SashForm sashForm = new SashForm(shell, SWT.NONE);
		FormData fd_sashForm = new FormData();
		fd_sashForm.bottom = new FormAttachment(100);
		fd_sashForm.right = new FormAttachment(100, -2);
		fd_sashForm.left = new FormAttachment(0, 2);

		sashForm.setLayoutData(fd_sashForm);
		sashForm.setBackground(SWTResourceManager.getColor(new RGB(225, 230, 246)));

		CTabFolder tabFolder_1 = new CTabFolder(sashForm, SWT.FLAT);
		tabFolder_1.setSingle(true);
		tabFolder_1.setSelectionBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		tabFolder_1.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));

		CTabItem tbtmRequest = new CTabItem(tabFolder_1, SWT.NONE);
		tbtmRequest.setText("Request");
		tabFolder_1.setSelection(0);

		requestText = new Text(tabFolder_1, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		tbtmRequest.setControl(requestText);

		CTabFolder tabFolder = new CTabFolder(sashForm, SWT.FLAT);
		tabFolder.setSingle(true);
		tabFolder.setSelectionBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		tabFolder.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));

		CTabItem tbtmResponse_1 = new CTabItem(tabFolder, SWT.NONE);
		tbtmResponse_1.setText("Response");

		Composite composite = new Composite(tabFolder, SWT.BORDER | SWT.NO_FOCUS);
		tbtmResponse_1.setControl(composite);
		composite.setLayout(new FormLayout());
		tabFolder.setSelection(0);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Protocol:");

		final Combo protocolCombo = new Combo(composite, SWT.READ_ONLY);
		fd_lblNewLabel.right = new FormAttachment(protocolCombo, -31);
		FormData fd_protocolCombo = new FormData();
		fd_protocolCombo.left = new FormAttachment(0, 96);
		fd_protocolCombo.right = new FormAttachment(100, -10);
		fd_protocolCombo.top = new FormAttachment(lblNewLabel, -3, SWT.TOP);
		protocolCombo.setLayoutData(fd_protocolCombo);
		protocolCombo.setItems(new String[] { "HTTP/1.1", "HTTP/1.0" });
		protocolCombo.select(0);

		Label lblStatusCode = new Label(composite, SWT.NONE);
		FormData fd_lblStatusCode = new FormData();
		fd_lblStatusCode.top = new FormAttachment(lblNewLabel, 16);
		fd_lblStatusCode.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		fd_lblStatusCode.right = new FormAttachment(0, 82);
		lblStatusCode.setLayoutData(fd_lblStatusCode);
		lblStatusCode.setText("Status Code:");

		final Combo contentTypeCombo = new Combo(composite, SWT.READ_ONLY);
		FormData fd_contentTypeCombo = new FormData();
		fd_contentTypeCombo.right = new FormAttachment(protocolCombo, 0, SWT.RIGHT);
		fd_contentTypeCombo.left = new FormAttachment(protocolCombo, 0, SWT.LEFT);
		contentTypeCombo.setLayoutData(fd_contentTypeCombo);
		contentTypeCombo.setItems(new String[] { "NONE", "application/atom+xml", "application/x-www-form-urlencoded", "application/json",
				"application/octet-stream", "application/svg+xml", "application/xhtml+xml", "application/xml", "multipart/form-data", "text/html",
				"text/plain", "text/xml", "*/*" });
		contentTypeCombo.select(0);

		Label lblContentType = new Label(composite, SWT.NONE);
		fd_contentTypeCombo.top = new FormAttachment(lblContentType, -3, SWT.TOP);
		FormData fd_lblContentType = new FormData();
		fd_lblContentType.top = new FormAttachment(lblStatusCode, 18);
		fd_lblContentType.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		fd_lblContentType.right = new FormAttachment(0, 90);
		lblContentType.setLayoutData(fd_lblContentType);
		lblContentType.setText("Content Type:");

		Label lblContent = new Label(composite, SWT.NONE);
		FormData fd_lblContent = new FormData();
		fd_lblContent.top = new FormAttachment(lblContentType, 18);
		fd_lblContent.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		fd_lblContent.right = new FormAttachment(0, 82);
		lblContent.setLayoutData(fd_lblContent);
		lblContent.setText("Content:");

		contentText = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);

		FormData fd_contentText = new FormData();
		fd_contentText.top = new FormAttachment(lblContent, 6);
		fd_contentText.bottom = new FormAttachment(100, -44);
		fd_contentText.left = new FormAttachment(0, 10);
		fd_contentText.right = new FormAttachment(100, -10);
		contentText.setLayoutData(fd_contentText);

		final Combo statusCodeCombo = new Combo(composite, SWT.READ_ONLY);
		FormData fd_statusCodeCombo = new FormData();
		fd_statusCodeCombo.left = new FormAttachment(lblStatusCode, 14);
		fd_statusCodeCombo.right = new FormAttachment(100, -10);
		fd_statusCodeCombo.top = new FormAttachment(lblStatusCode, -3, SWT.TOP);
		statusCodeCombo.setLayoutData(fd_statusCodeCombo);
		statusCodeCombo.setItems(new String[] { "100", "101", "102", "200", "201", "202", "203", "204", "205", "206", "207", "300", "301", "302",
				"303", "304", "305", "307", "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414",
				"415", "416", "417", "422", "423", "424", "425", "426", "428", "429", "431", "500", "501", "502", "503", "504", "505", "506", "507",
				"510", "511" });
		statusCodeCombo.select(3);

		Button btnNewButton = new Button(composite, SWT.NONE);

		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.left = new FormAttachment(protocolCombo, -67);
		fd_btnNewButton.top = new FormAttachment(contentText, 9);
		fd_btnNewButton.right = new FormAttachment(protocolCombo, 0, SWT.RIGHT);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Send");
		sashForm.setWeights(new int[] { 1, 1 });
		fd_sashForm.top = new FormAttachment(0);

		requestText.setText(debugger.getRequest().toString());

		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String version = protocolCombo.getItem(protocolCombo.getSelectionIndex());
				Integer status = Integer.parseInt(statusCodeCombo.getItem(statusCodeCombo.getSelectionIndex()));

				HttpVersion httpVersion = HttpVersion.valueOf(version);
				HttpResponseStatus statusCode = HttpResponseStatus.valueOf(status);

				String contentType = contentTypeCombo.getItem(contentTypeCombo.getSelectionIndex());
				Boolean isNone = contentType.equalsIgnoreCase("NONE");
				String content = contentText.getText();

				DefaultFullHttpResponse fullRes = new DefaultFullHttpResponse(httpVersion, statusCode);
				Response response = new Response(fullRes, State.PROXY);

				debugger.setResponse(response);
				shell.dispose();

			}
		});

		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				debugger.setResponse(null);
				shell.dispose();

			}
		});
	}
}
