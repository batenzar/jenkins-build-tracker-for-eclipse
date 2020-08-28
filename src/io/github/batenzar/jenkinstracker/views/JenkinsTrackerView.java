package io.github.batenzar.jenkinstracker.views;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import io.github.batenzar.jenkinstracker.Activator;
import io.github.batenzar.jenkinstracker.preferences.PreferenceConstants;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class JenkinsTrackerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "io.batenzar.views.JenkinsTrackerView";

	@Inject IWorkbench workbench;
	
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private TreeObject toUrl1,toUrl2,toUrl3; 
	 
	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		private String url;
		private String status;
		
		public TreeObject(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		@Override
		public String toString() {
			return getName() + ": " + status;
		}
		@Override
		public <T> T getAdapter(Class<T> key) {
			return null;
		}
		
	}
	
	class TreeParent extends TreeObject {
		private ArrayList children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
	}

	class ViewContentProvider implements ITreeContentProvider {
		private TreeParent invisibleRoot;

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
/*
 * We will set up a dummy model to initialize tree hierarchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
			TreeObject to1 = new TreeObject("Leaf 1");
			TreeObject to2 = new TreeObject("Leaf 2");
			TreeObject to3 = new TreeObject("Leaf 3");
			TreeParent p1 = new TreeParent("Parent 1");
			p1.addChild(to1);
			p1.addChild(to2);
			p1.addChild(to3);
			
			TreeObject to4 = new TreeObject("Leaf 4");
			TreeParent p2 = new TreeParent("Parent 2");
			p2.addChild(to4);
			
			TreeParent root = new TreeParent("Root");
			root.addChild(p1);
			root.addChild(p2);
			
			/////
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			String url1 = store.getString(PreferenceConstants.URL01);
			String url2 = store.getString(PreferenceConstants.URL02);
			String url3 = store.getString(PreferenceConstants.URL03);
			
			toUrl1 = new TreeObject(url1);
			toUrl2 = new TreeObject(url2);
			toUrl3 = new TreeObject(url3);
			
			root.addChild(toUrl1);
			root.addChild(toUrl2);
			root.addChild(toUrl3);
			/////
			
			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild(root);
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_FILE;
			if (obj instanceof TreeObject) {
				// TODO Icon
				// "/static/??/images/32x32/nobuilt.png";
				// "/static/??/images/32x32/blue.png";
				// "/static/??/images/32x32/red.png";
				String status = ((TreeObject) obj).status;
				if ("Failure".equalsIgnoreCase(status))
					imageKey = ISharedImages.IMG_OBJS_ERROR_TSK;
			}
			if (obj instanceof TreeParent)
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return workbench.getSharedImages().getImage(imageKey);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		
	viewer.setContentProvider(new ViewContentProvider());
	viewer.setInput(getViewSite());
	viewer.setLabelProvider(new ViewLabelProvider());

		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(viewer.getControl(), "jenkins-build-tracker-for-eclipse.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				JenkinsTrackerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				Runnable r = new Runnable() {

					@Override
					public void run() {
						IPreferenceStore store = Activator.getDefault().getPreferenceStore();
						try {

							// Temporary auto accept all cert
							TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
								public java.security.cert.X509Certificate[] getAcceptedIssuers() {
									return null;
								}

								public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
										String authType) {
								}

								public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
										String authType) {
								}
							} };

							SSLContext sc = SSLContext.getInstance("SSL");
							sc.init(null, trustAllCerts, new java.security.SecureRandom());
							HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

							Object urlsPref[][] = new Object[][] {
									{ toUrl1, store.getString(PreferenceConstants.URL01) },
									{ toUrl2, store.getString(PreferenceConstants.URL02) },
									{ toUrl3, store.getString(PreferenceConstants.URL03) } };

							for (Object[] u : urlsPref) {
								TreeObject treeObj = (TreeObject) u[0];
								URL url = new URL((String) u[1]);
								try {
									HttpURLConnection conn = (HttpURLConnection) url.openConnection();
									conn.setRequestMethod("GET");
									conn.setRequestProperty("Accept", "application/json");
									if (conn.getResponseCode() != 200) {
										throw new RuntimeException(
												"Failed : HTTP Error code : " + conn.getResponseCode());
									}

									StringBuilder response = new StringBuilder();
									InputStreamReader in = new InputStreamReader(conn.getInputStream());
									BufferedReader br = new BufferedReader(in);
									String output;
									while ((output = br.readLine()) != null) {
										response.append(output);
									}

									// use JSON parser from JavaScript ScriptEngine
									ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
									String json = response.toString();
									String script = "Java.asJSONCompatible(" + json + ")";
									Object result = engine.eval(script);

									Map contents = (Map) result;
									treeObj.url = (String) u[1];
									treeObj.name = (String) contents.get("fullDisplayName");
									treeObj.status = (String) contents.get("result");
									conn.disconnect();
								} catch (Exception e) {
									treeObj.name = e.getMessage();
									System.err.println(e.getMessage());
								}
							}
						} catch (Exception e) {
							System.err.println(e.getMessage());
						}
						
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								viewer.refresh();
							}
						});
					}
				};

				Thread t = new Thread(r);
				t.setDaemon(true);
				t.start();
			}
		};
		action1.setText("Refresh All");
		action1.setToolTipText("Refresh All url");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"JenkinsTracker View",
			message);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
