package org.eclipse.etools.jar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Lists;

public class SearchJar extends AbstractHandler {
	private static final String LAST_PATH="last.path"; //$NON-NLS-1$
	private static final String LAST_CLASS="last.class"; //$NON-NLS-1$
//	private static final String LAST_FOLDER="last.folder";
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		DirectoryDialog dialog=new DirectoryDialog(shell);
		IDialogSettings dialogSettings=Activator.getDefault().getOrCreateDialogSettings(SearchJar.class);
		String lastPath = dialogSettings.get(LAST_PATH);
		if(StringUtils.isNotEmpty(lastPath))
			dialog.setFilterPath(lastPath);
		dialog.setMessage("Where should I search for Jar Files ? (recursively)");
		String newPath=dialog.open();
		if(newPath!=null) {
			dialogSettings.put(LAST_PATH, newPath);
			ISelection activeMenuSelection = HandlerUtil.getActiveMenuSelection(event);
			String initialFqClassName=null;
			if(activeMenuSelection instanceof ITextSelection)
				initialFqClassName=((ITextSelection)activeMenuSelection).getText();
			if(StringUtils.isEmpty(initialFqClassName))
				initialFqClassName=dialogSettings.get(LAST_CLASS);
			InputDialog dialog2=new InputDialog(shell, shell.getText(), "Enter the name of the class to search:", initialFqClassName, null);
			if(dialog2.open()==Window.OK) {
				dialogSettings.put(LAST_CLASS, dialog2.getValue());
				final String fqClassName=dialog2.getValue().replace('.', '/')+".class";
				try {
					final List<File> candidates=Lists.newArrayList();
					Files.walkFileTree(Paths.get(newPath), new SimpleFileVisitor<Path>(){
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if(!attrs.isDirectory() && file.getFileName().toString().endsWith(".jar")){
								candidates.add(file.toFile());
							}
							return FileVisitResult.CONTINUE;
						}
					});
					ProgressMonitorDialog dialog3=new ProgressMonitorDialog(shell);
					dialog3.run(false, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Searching", candidates.size());
							for (Iterator<File> iterator = candidates.iterator(); iterator.hasNext();) {
								File file = iterator.next();
								try {
									JarFile jarFile=new JarFile(file);
									monitor.subTask(jarFile.getName());
//									if(fqClassName.contains("/")) {
//										if(jarFile.getEntry(fqClassName)==null) {
//											iterator.remove();
//										}
//									} else {
										// Not a fully qualified name :(
										boolean found=false;
										for(JarEntry entry: Collections.list(jarFile.entries())) {
											if(entry.getName().endsWith(fqClassName)) {
												found=true;
												break;
											}
										}
										if(!found)
											iterator.remove();
//									}
									jarFile.close();
								} catch (IOException e) {
									Activator.logError("", e); //$NON-NLS-1$
								}
								monitor.worked(1);
							}
							monitor.done();
						}
					});
					if(candidates.isEmpty()) {
						MessageDialog.openInformation(shell, shell.getText(), "No candidate found :(");
					} else {
						ListDialog dialog4 = new ListDialog(shell);
						dialog4.setAddCancelButton(false);
						dialog4.setTitle(shell.getText());
						dialog4.setMessage("List of matching Jar file(s):");
						dialog4.setContentProvider(ArrayContentProvider.getInstance());
						dialog4.setLabelProvider(new LabelProvider());
//						if(!candidates.isEmpty())
						dialog4.setInitialElementSelections(Collections.singletonList(candidates.get(0)));
						dialog4.setInput(candidates);
						dialog4.open();
					}
//					if(dialog4.open()==Window.OK && dialog4.getResult().length>0) {
//						File jarFile = (File) dialog4.getResult()[0];
//						FolderSelectionDialog dialog5=new FolderSelectionDialog(shell, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
//						dialog5.setMessage("Set the destination folder for the JAR:");
//						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//						dialog5.setInput(root);
//						if(StringUtils.isNotEmpty(dialogSettings.get(LAST_FOLDER)))
//							dialog5.setInitialSelection(root.findMember(dialogSettings.get(LAST_FOLDER)));
//
//						if(dialog5.open()==Window.OK && dialog5.getResult().length>0) {
//							IResource res = (IResource) dialog5.getResult()[0];
//							dialogSettings.put(LAST_FOLDER, res.getFullPath().toString());
//							IContainer folder=res instanceof IContainer ? (IContainer)res : ((IFile)res).getParent();
//							IFile copiedFile = folder.getFile(new org.eclipse.core.runtime.Path(jarFile.getName()));
//							if(copiedFile.exists()) {
//								MessageDialog.openError(shell, shell.getText(), "File already exists");
//							} else {
//								Files.copy(jarFile.toPath(), Paths.get(copiedFile.getLocation().toString()));
//								copiedFile.refreshLocal(0, new NullProgressMonitor());
//								IWorkbenchSite site = HandlerUtil.getActiveSiteChecked(event);
//								AddSelectedLibraryToBuildpathAction action=new AddSelectedLibraryToBuildpathAction(site);
//								action.selectionChanged(new SelectionChangedEvent(site.getSelectionProvider(), new StructuredSelection(copiedFile)));
//								action.run();
//							}
//						}
//					}
				} catch (Throwable t) {
					Activator.logError("Internal error", t); //$NON-NLS-1$
				}
			}
		}
		return null;
	}
}
