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
import java.util.jar.JarFile;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.FolderSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.newsourcepage.AddSelectedLibraryToBuildpathAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.google.common.collect.Lists;

public class SearchAndAddJar extends AbstractHandler {
	private static final IDialogSettings DIALOG_SETTINGS=Activator.getDefault().getOrCreateDialogSettings(SearchAndAddJar.class);
	private static final String LAST_PATH="last.path";
	private static final String LAST_CLASS="last.class";
	private static final String LAST_FOLDER="last.folder";
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		DirectoryDialog dialog=new DirectoryDialog(shell);
		String lastPath = DIALOG_SETTINGS.get(LAST_PATH);
		if(StringUtils.isNotEmpty(lastPath))
			dialog.setFilterPath(lastPath);
		dialog.setMessage("Where should I search for Jar Files ? (recursively)");
		String newPath=dialog.open();
		if(newPath!=null) {
			DIALOG_SETTINGS.put(LAST_PATH, newPath);
			ISelection activeMenuSelection = HandlerUtil.getActiveMenuSelection(event);
			String initialFqClassName=null;
			if(activeMenuSelection instanceof ITextSelection)
				initialFqClassName=((ITextSelection)activeMenuSelection).getText();
			if(StringUtils.isEmpty(initialFqClassName))
				initialFqClassName=DIALOG_SETTINGS.get(LAST_CLASS);
			InputDialog dialog2=new InputDialog(shell, shell.getText(), "Enter the full qualified name of the class to search:", initialFqClassName, null);
			if(dialog2.open()==Window.OK) {
				DIALOG_SETTINGS.put(LAST_CLASS, dialog2.getValue());
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
									if(jarFile.getEntry(fqClassName)==null) {
										iterator.remove();
									}
									jarFile.close();
								} catch (IOException e) {
									Activator.logError("", e); //$NON-NLS-1$
								}
								monitor.worked(1);
							}
							monitor.done();
						}
					});
					ListDialog dialog4 = new ListDialog(shell);
					dialog4.setTitle(shell.getText());
					dialog4.setMessage("Which Jar do you want to add to build path ?");
					dialog4.setContentProvider(ArrayContentProvider.getInstance());
					dialog4.setLabelProvider(new LabelProvider());
					if(!candidates.isEmpty())
						dialog4.setInitialElementSelections(Collections.singletonList(candidates.get(0)));
					dialog4.setInput(candidates);
					if(dialog4.open()==Window.OK && dialog4.getResult().length>0) {
						File jarFile = (File) dialog4.getResult()[0];
						FolderSelectionDialog dialog5=new FolderSelectionDialog(shell, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
						dialog5.setMessage("Set the destination folder for the JAR:");
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						dialog5.setInput(root);
						if(StringUtils.isNotEmpty(DIALOG_SETTINGS.get(LAST_FOLDER)))
							dialog5.setInitialSelection(root.findMember(DIALOG_SETTINGS.get(LAST_FOLDER)));

						if(dialog5.open()==Window.OK && dialog5.getResult().length>0) {
							IResource res = (IResource) dialog5.getResult()[0];
							DIALOG_SETTINGS.put(LAST_FOLDER, res.getFullPath().toString());
							IContainer folder=res instanceof IContainer ? (IContainer)res : ((IFile)res).getParent();
							IFile copiedFile = folder.getFile(new org.eclipse.core.runtime.Path(jarFile.getName()));
							if(copiedFile.exists()) {
								MessageDialog.openError(shell, shell.getText(), "File already exists");
							} else {
								Files.copy(jarFile.toPath(), Paths.get(copiedFile.getLocation().toString()));
								copiedFile.refreshLocal(0, new NullProgressMonitor());
								IWorkbenchSite site = HandlerUtil.getActiveSiteChecked(event);
								AddSelectedLibraryToBuildpathAction action=new AddSelectedLibraryToBuildpathAction(site);
								action.selectionChanged(new SelectionChangedEvent(site.getSelectionProvider(), new StructuredSelection(copiedFile)));
								action.run();
							}
						}
					}
				} catch (Exception e) {
					Activator.logError("", e); //$NON-NLS-1$
				}
			}
		}
		return null;
	}
}
