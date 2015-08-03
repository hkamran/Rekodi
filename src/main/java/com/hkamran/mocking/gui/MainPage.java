package com.hkamran.mocking.gui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hkamran.mocking.Debugger;
import com.hkamran.mocking.FilterManager;
import com.hkamran.mocking.FilterManager.State;
import com.hkamran.mocking.Request;
import com.hkamran.mocking.Response;
import com.hkamran.mocking.Tape;

public class MainPage {
	
	private final static Logger log = Logger.getLogger(MainPage.class);
	
	protected Shell shell;
	private Display display;
	private Text contentText;
	private FilterManager filter;
	private Tree tree;
	private CTabFolder tapeFolder;
	private Tree logTree;
	private Boolean autoScroll = true;
	private ToolItem redirectToolbarItem;
	private MenuItem mntmRedirectTraffic;

	public MainPage(FilterManager filter) {
		this.filter = filter;
	}

	public MainPage() {

	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainPage window = new MainPage();
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
		shell = new Shell();
		shell.setImage(null);
		shell.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		shell.setSize(569, 619);
		shell.setText("Service Recorder");
		shell.setLayout(new FormLayout());

		Composite toolBarComposite = new Composite(shell, SWT.NONE);
		toolBarComposite.setBackgroundImage(SWTResourceManager.getImage(MainPage.class, "/icons/menu_bg.png"));
		toolBarComposite.setLayout(new FormLayout());
		FormData fd_toolBarComposite = new FormData();
		fd_toolBarComposite.bottom = new FormAttachment(0, 35);
		fd_toolBarComposite.top = new FormAttachment(0);
		fd_toolBarComposite.left = new FormAttachment(0);
		fd_toolBarComposite.right = new FormAttachment(100);
		toolBarComposite.setLayoutData(fd_toolBarComposite);

		ToolBar toolBar = new ToolBar(toolBarComposite, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.right = new FormAttachment(0, 535);
		fd_toolBar.bottom = new FormAttachment(100, -7);
		fd_toolBar.left = new FormAttachment(0, 4);
		toolBar.setLayoutData(fd_toolBar);
		toolBar.setBackgroundImage(SWTResourceManager.getImage(MainPage.class, "/icons/menu_item_bg.png"));
		final ToolItem proxyToolbarItem = new ToolItem(toolBar, SWT.CHECK);

		proxyToolbarItem.setToolTipText("Proxy Mode");
		proxyToolbarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/Button Blank Green.png"));
		proxyToolbarItem.setSelection(filter.getState() == State.PROXY);

		final ToolItem recordToolBarItem = new ToolItem(toolBar, SWT.CHECK);

		recordToolBarItem.setToolTipText("Record Mode");
		recordToolBarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/record_button.png"));
		recordToolBarItem.setSelection(filter.getState() == State.RECORD);

		final ToolItem playbackToolbarItem = new ToolItem(toolBar, SWT.CHECK);

		playbackToolbarItem.setToolTipText("Playback Mode");
		playbackToolbarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/playback_button.png"));
		playbackToolbarItem.setSelection(filter.getState() == State.MOCK);

		@SuppressWarnings("unused")
		ToolItem separatorStates = new ToolItem(toolBar, SWT.SEPARATOR);

		final ToolItem debugToolbarItem = new ToolItem(toolBar, SWT.CHECK);

		debugToolbarItem.setToolTipText("Debug Tape");
		debugToolbarItem.setEnabled(false);
		debugToolbarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/debug_exc.gif"));

		redirectToolbarItem = new ToolItem(toolBar, SWT.CHECK);

		redirectToolbarItem.setToolTipText("Redirect");
		redirectToolbarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/efeoE.gif"));
		redirectToolbarItem.setSelection(filter.getRedirectState());

		@SuppressWarnings("unused")
		ToolItem separatorOne = new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem tltmNewItem = new ToolItem(toolBar, SWT.NONE);
		tltmNewItem.setToolTipText("New Tape");
		tltmNewItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/new_untitled_text_file.gif"));

		final ToolItem exportToolbarItem = new ToolItem(toolBar, SWT.NONE);
		exportToolbarItem.setToolTipText("Export Tape");
		exportToolbarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/export.gif"));

		ToolItem importToolbarItem = new ToolItem(toolBar, SWT.NONE);
		importToolbarItem.setToolTipText("Import Tape");

		importToolbarItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/import_wiz.gif"));

		SashForm mainForm = new SashForm(shell, SWT.VERTICAL);
		mainForm.setBackground(SWTResourceManager.getColor(new RGB(225, 230, 246)));
		FormData fd_mainForm = new FormData();
		fd_mainForm.right = new FormAttachment(toolBarComposite, 0, SWT.RIGHT);
		fd_mainForm.left = new FormAttachment(toolBarComposite, 0, SWT.LEFT);
		fd_mainForm.top = new FormAttachment(toolBarComposite, 0);
		fd_mainForm.bottom = new FormAttachment(100, -10);

		mainForm.setLayoutData(fd_mainForm);

		SashForm tapeForm = new SashForm(mainForm, SWT.NONE);
		tapeForm.setBackground(SWTResourceManager.getColor(new RGB(225, 230, 246)));

		tapeFolder = new CTabFolder(tapeForm, SWT.FLAT);
		tapeFolder.setSingle(true);
		tapeFolder.setSelectionBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		tapeFolder.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));

		CTabItem tbtmTape = new CTabItem(tapeFolder, SWT.NONE);
		tbtmTape.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/ref-15.png"));
		tapeFolder.setSelection(0);
		tbtmTape.setText("Tape");

		tree = new Tree(tapeFolder, SWT.BORDER);

		tbtmTape.setControl(tree);

		CTabFolder contentFolder = new CTabFolder(tapeForm, SWT.FLAT);
		contentFolder.setSelectionBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		contentFolder.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));
		contentFolder.setSingle(true);

		CTabItem tbtmContent = new CTabItem(contentFolder, SWT.NONE);
		tbtmContent.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/icon-webdoc.gif"));
		tbtmContent.setText("Content");

		contentText = new Text(contentFolder, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		contentText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		tbtmContent.setControl(contentText);
		contentFolder.setSelection(0);
		tapeForm.setWeights(new int[] { 152, 380 });
		CTabFolder logFolderTab = new CTabFolder(mainForm, SWT.FLAT);
		logFolderTab.setSingle(true);
		logFolderTab.setSelectionBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));

		ToolBar logToolBar = new ToolBar(logFolderTab, SWT.FLAT);
		logToolBar.setBackground(SWTResourceManager.getColor(new RGB(238, 242, 250)));

		ToolItem clearTapeItem = new ToolItem(logToolBar, SWT.NONE);
		clearTapeItem.setToolTipText("Clear Tape");
		clearTapeItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/clear_output.gif"));

		ToolItem scrollLockItem = new ToolItem(logToolBar, SWT.CHECK);
		scrollLockItem.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/lock.png"));
		scrollLockItem.setToolTipText("Autoscroll When Log Changes");
		scrollLockItem.setSelection(true);
		logFolderTab.setTopRight(logToolBar, SWT.RIGHT);

		CTabItem tbtmLog = new CTabItem(logFolderTab, SWT.NONE);
		tbtmLog.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/icon_console.png"));
		tbtmLog.setText("Log");

		logTree = new Tree(logFolderTab, SWT.BORDER | SWT.FULL_SELECTION);
		logTree.setHeaderVisible(true);
		tbtmLog.setControl(logTree);
		logFolderTab.setSelection(0);

		TreeColumn trclmnAttribute = new TreeColumn(logTree, SWT.NONE);
		trclmnAttribute.setText("Event");
		trclmnAttribute.setWidth(200);

		TreeColumn trclmnValue = new TreeColumn(logTree, SWT.NONE);
		trclmnValue.setText("Value");
		trclmnValue.setWidth(300);
		mainForm.setWeights(new int[] { 296, 217 });

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("File");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmMode = new MenuItem(menu_1, SWT.CASCADE);
		mntmMode.setText("Mode");

		Menu menu_3 = new Menu(mntmMode);
		mntmMode.setMenu(menu_3);

		MenuItem mntmProxy = new MenuItem(menu_3, SWT.RADIO);

		mntmProxy.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/Button Blank Green.png"));
		mntmProxy.setText("Proxy ");

		MenuItem mntmRecord = new MenuItem(menu_3, SWT.RADIO);
		mntmRecord.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/record_button.png"));
		mntmRecord.setText("Record");

		MenuItem mntmMock = new MenuItem(menu_3, SWT.RADIO);
		mntmMock.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/playback_button.png"));
		mntmMock.setText("Mock");

		new MenuItem(menu_1, SWT.SEPARATOR);

		mntmRedirectTraffic = new MenuItem(menu_1, SWT.CHECK);

		mntmRedirectTraffic.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/efeoE.gif"));
		mntmRedirectTraffic.setText("Redirect Traffic");

		final MenuItem mntmDebugTraffic = new MenuItem(menu_1, SWT.CHECK);
		mntmDebugTraffic.setEnabled(false);

		mntmDebugTraffic.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/debug_exc.gif"));
		mntmDebugTraffic.setText("Debug Traffic");

		new MenuItem(menu_1, SWT.SEPARATOR);

		MenuItem mntmLoadTape = new MenuItem(menu_1, SWT.NONE);
		mntmLoadTape.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/import_wiz.gif"));
		mntmLoadTape.setText("Load Tape");

		MenuItem mntmExport = new MenuItem(menu_1, SWT.NONE);

		mntmExport.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/export.gif"));
		mntmExport.setText("Export Tape");

		MenuItem mntmNewTape = new MenuItem(menu_1, SWT.NONE);

		mntmNewTape.setImage(SWTResourceManager.getImage(MainPage.class, "/icons/new_untitled_text_file.gif"));
		mntmNewTape.setText("New Tape");

		new MenuItem(menu_1, SWT.SEPARATOR);

		MenuItem mntmSettings = new MenuItem(menu_1, SWT.NONE);

		mntmSettings.setText("Settings");

		mntmSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SettingsPage settings = new SettingsPage(shell, filter);
				settings.open();
			}
		});

		new MenuItem(menu_1, SWT.SEPARATOR);

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setText("Exit");

		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);

		mntmAbout.setText("About");

		Menu menu_2 = new Menu(shell);
		shell.setMenu(menu_2);

		final Menu menu2 = new Menu(tree);
		tree.setMenu(menu2);

		MenuItem mntmEdit = new MenuItem(menu2, SWT.NONE);
		mntmEdit.setSelection(true);
		mntmEdit.setText("Edit");

		MenuItem mntmRemove = new MenuItem(menu2, SWT.NONE);
		mntmRemove.setText("Remove");

		// Listeners

		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AboutPage page = new AboutPage(shell);
				page.open();

			}
		});

		mntmNewTape.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Tape tape = new Tape();
				filter.setTape(tape);
				updateTree(tape);
				contentText.setText("");
			}
		});

		tltmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Tape tape = new Tape();
				filter.setTape(tape);
				updateTree(tape);
				contentText.setText("");
			}
		});

		clearTapeItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logTree.removeAll();
			}
		});

		scrollLockItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					autoScroll = true;
					return;
				}
				autoScroll = false;
			}
		});

		mntmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = tree.getSelection();
				if (items.length == 1) {
					TreeItem item = items[0];
					TreeItem parent = item.getParentItem();

					if (item.getParentItem() == null) {
						Tape tape = filter.getTape();
						Request request = tape.getRequest(item.getText());
						tape.remove(request);
						updateTree(tape);
					} else {
						Tape tape = filter.getTape();
						int index = Integer.parseInt(item.getText());
						List<Response> responses = tape.getResponses(parent.getText());
						responses.remove(index);
						updateTree(tape);
					}
				}
			}
		});

		mntmEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TreeItem[] items = tree.getSelection();
				if (items.length == 1) {
					TreeItem item = items[0];
					TreeItem parent = item.getParentItem();

					if (item.getParentItem() == null) {
						Tape tape = filter.getTape();
						Request request = tape.getRequest(item.getText());
						List<Response> responses = tape.getResponses(request);
						tape.remove(request);

						RequestPage.open(shell, request);

						tape.put(request, responses);
						contentText.setText("");

					} else {
						Tape tape = filter.getTape();
						Integer index = Integer.parseInt(item.getText());
						Response response = tape.getResponses(parent.getText()).get(index);

						ResponsePage.open(shell, response);

						updateTree(tape);
						contentText.setText("");
					}
				}

			}
		});

		// Import
		importToolbarItem.addSelectionListener(getImportSelection());
		mntmLoadTape.addSelectionListener(getImportSelection());

		// Export
		exportToolbarItem.addSelectionListener(getExportSelection());
		mntmExport.addSelectionListener(getExportSelection());

		// Debug Toolbar
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
				display.dispose();
			}
		});

		debugToolbarItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					mntmDebugTraffic.setSelection(true);
					filter.getDebugger().setState(true);
					return;
				}
				mntmDebugTraffic.setSelection(false);
				filter.getDebugger().setState(false);

			}
		});
		mntmDebugTraffic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					debugToolbarItem.setSelection(true);
					filter.getDebugger().setState(true);
					return;
				}
				debugToolbarItem.setSelection(false);
				filter.getDebugger().setState(false);
			}
		});

		// Mode Selections
		mntmProxy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				proxyToolbarItem.setSelection(true);
				recordToolBarItem.setSelection(false);
				playbackToolbarItem.setSelection(false);
				filter.setState(State.PROXY);
				debugToolbarItem.setEnabled(false);
				mntmDebugTraffic.setEnabled(false);
			}
		});

		mntmRecord.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					proxyToolbarItem.setSelection(false);
					playbackToolbarItem.setSelection(false);
					filter.setState(State.RECORD);
					debugToolbarItem.setEnabled(false);
					mntmDebugTraffic.setEnabled(false);
					recordToolBarItem.setSelection(true);
					return;
				}
				proxyToolbarItem.setSelection(true);
				recordToolBarItem.setSelection(false);
				playbackToolbarItem.setSelection(false);
				filter.setState(State.PROXY);
				debugToolbarItem.setEnabled(false);
				mntmDebugTraffic.setEnabled(false);
			}
		});

		mntmMock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					proxyToolbarItem.setSelection(false);
					recordToolBarItem.setSelection(false);
					filter.setState(State.MOCK);
					debugToolbarItem.setEnabled(true);
					mntmDebugTraffic.setEnabled(true);
					playbackToolbarItem.setSelection(true);
					return;
				}
				proxyToolbarItem.setSelection(true);
				recordToolBarItem.setSelection(false);
				playbackToolbarItem.setSelection(false);
				filter.setState(State.PROXY);
				debugToolbarItem.setEnabled(false);
				mntmDebugTraffic.setEnabled(false);
			}
		});

		proxyToolbarItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				proxyToolbarItem.setSelection(true);
				recordToolBarItem.setSelection(false);
				playbackToolbarItem.setSelection(false);
				filter.setState(State.PROXY);
				debugToolbarItem.setEnabled(false);
				mntmDebugTraffic.setEnabled(false);

			}
		});

		recordToolBarItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					proxyToolbarItem.setSelection(false);
					playbackToolbarItem.setSelection(false);
					filter.setState(State.RECORD);
					debugToolbarItem.setEnabled(false);
					mntmDebugTraffic.setEnabled(false);
					return;
				}
				proxyToolbarItem.setSelection(true);
				recordToolBarItem.setSelection(false);
				playbackToolbarItem.setSelection(false);
				filter.setState(State.PROXY);
				debugToolbarItem.setEnabled(false);
				mntmDebugTraffic.setEnabled(false);
			}
		});

		playbackToolbarItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					proxyToolbarItem.setSelection(false);
					recordToolBarItem.setSelection(false);
					filter.setState(State.MOCK);
					debugToolbarItem.setEnabled(true);
					mntmDebugTraffic.setEnabled(true);
					return;
				}
				proxyToolbarItem.setSelection(true);
				recordToolBarItem.setSelection(false);
				playbackToolbarItem.setSelection(false);
				filter.setState(State.PROXY);
				debugToolbarItem.setEnabled(false);
				mntmDebugTraffic.setEnabled(false);
			}
		});

		// Redirect
		redirectToolbarItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					mntmRedirectTraffic.setSelection(true);
					filter.setRedirectState(true);
					return;
				}
				mntmRedirectTraffic.setSelection(false);
				filter.setRedirectState(false);
			}
		});

		mntmRedirectTraffic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Boolean status = getSelectedStatus(e);
				if (status) {
					redirectToolbarItem.setSelection(true);
					filter.setRedirectState(true);
					return;
				}
				redirectToolbarItem.setSelection(false);
				filter.setRedirectState(false);
			}
		});

		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						Tree tree = (Tree) e.getSource();
						final TreeItem[] items = tree.getSelection();
						if (items.length == 1) {
							TreeItem item = items[0];
							if (item.getParentItem() == null) {
								Request request = filter.getTape().getRequest(item.getText());
								if (request == null) {
									log.warn("Selected Request object is null");
								} else {
									contentText.setText(request.toString());
								}
							} else {
								TreeItem parent = item.getParentItem();
								Response response = filter.getTape().getResponses(parent.getText()).get(Integer.parseInt(item.getText()));
								if (response == null) {
									log.warn("Selected response object is null");
								} else {
									contentText.setText(response.toString());
								}
								
							}
						}
					}
				});
			}
		});

	}

	private SelectionAdapter getExportSelection() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				String[] filterExt = { "*.json" };
				dialog.setFilterExtensions(filterExt);
				String file = dialog.open();
				if (file != null) {
					try {
						filter.getTape().export(file);

					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
	}

	private SelectionAdapter getImportSelection() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String[] filterExt = { "*.json" };
				dialog.setFilterExtensions(filterExt);
				String file = dialog.open();
				if (file != null) {
					try {
						Tape tape = Tape.load(file);
						filter.setTape(tape);
						contentText.setText("");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			}
		};
	}

	public Boolean getSelectedStatus(SelectionEvent e) {
		Object obj = e.getSource();
		if (obj instanceof ToolItem) {
			ToolItem item = (ToolItem) e.getSource();
			return item.getSelection();
		} else if (obj instanceof MenuItem) {
			MenuItem item = (MenuItem) e.getSource();
			return item.getSelection();
		}
		return false;
	}

	public void updateTree(final Tape tape) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {

				tree.removeAll();
				for (Request request : tape.getRequests()) {
					if (request != null) {
						TreeItem requestItem = new TreeItem(tree, SWT.NONE);
						requestItem.setText(request.hashCode() + "");
						List<Response> responses = tape.getResponses(request);
						if (responses != null) {
							for (int i = 0; i < responses.size(); i++) {
								TreeItem subItem = new TreeItem(requestItem, SWT.NONE);
								subItem.setText(i + "");

							}
							requestItem.setExpanded(true);
						}
					} else {
						log.warn("Request is null");
					}
				}
			}
		});

	}

	public void debugger(final Debugger debug) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				DebugPage page = new DebugPage(shell, debug);
				page.open();

			}
		});
	}

	public void updateConsole(final String str) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				Calendar cal = Calendar.getInstance();

				TreeItem incomingItem = new TreeItem(logTree, SWT.NONE);
				incomingItem.setText(dateFormat.format(cal.getTime()) + " - " + str);
			}
		});
	}

	public void updateConsole(final Request req, final Response res) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				Calendar cal = Calendar.getInstance();

				TreeItem incomingItem = new TreeItem(logTree, SWT.NONE);
				incomingItem.setText(dateFormat.format(cal.getTime()) + " - Incoming " + req.hashCode());

				TreeItem requestItem = new TreeItem(incomingItem, SWT.NONE);
				requestItem.setText("Request");

				TreeItem methodItem = new TreeItem(requestItem, SWT.NONE);
				methodItem.setText(new String[] { "Method:", req.getMethod() });

				TreeItem uriItem = new TreeItem(requestItem, SWT.NONE);
				uriItem.setText(new String[] { "URI:", req.getURI() });

				TreeItem hostItem = new TreeItem(requestItem, SWT.NONE);
				hostItem.setText(new String[] { "Host:", req.getHost() });

				TreeItem contentTypeItem = new TreeItem(requestItem, SWT.NONE);
				contentTypeItem.setText(new String[] { "Content-Type:", req.getContentType() });

				TreeItem contentItem = new TreeItem(requestItem, SWT.NONE);
				contentItem.setText(new String[] { "Content:", req.getContent() });

				TreeItem responseItem = new TreeItem(incomingItem, SWT.NONE);
				responseItem.setText("Response");

				TreeItem statusItem = new TreeItem(responseItem, SWT.NONE);
				statusItem.setText(new String[] { "Status:", res.getStatus().toString() });

				TreeItem protocolItem = new TreeItem(responseItem, SWT.NONE);
				protocolItem.setText(new String[] { "Protocol:", res.getProtocol() });

				TreeItem contentTypeItem2 = new TreeItem(responseItem, SWT.NONE);
				contentTypeItem2.setText(new String[] { "Content-Type:", res.getContentType() });

				TreeItem contentItem2 = new TreeItem(responseItem, SWT.NONE);
				contentItem2.setText(new String[] { "Content:", res.getContent() });

				if (autoScroll) {
					TreeItem item = logTree.getItem(logTree.getItems().length - 1);
					logTree.showItem(item);
				}
			}
		});

	}
}
