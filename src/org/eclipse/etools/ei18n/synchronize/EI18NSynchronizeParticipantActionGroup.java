package org.eclipse.etools.ei18n.synchronize;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.StorageTypedElement;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

@RemoveMe
public class EI18NSynchronizeParticipantActionGroup extends SynchronizePageActionGroup {
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.add(new Action("Overwrite") { //$NON-NLS-1$
			@Override
			public void run() {

				Object[] objects=((IStructuredSelection) getContext().getSelection()).toArray();
				Stack<Object> stack=new Stack<Object>();
				stack.addAll(Arrays.asList(objects));
				while (!stack.isEmpty()) {
					Object o=stack.pop();
					if (o instanceof ISynchronizeModelElement) {
						ITypedElement left=((ISynchronizeModelElement) o).getLeft();
						ITypedElement right=((ISynchronizeModelElement) o).getRight();
						if (left instanceof ResourceNode && right instanceof StorageTypedElement) {
							IResource resource=((ResourceNode) left).getResource();
							if (resource instanceof IFile) {
								IFile file=(IFile) resource;
								InputStream is=null;
								try {
									if (file.exists())
										file.delete(true, new NullProgressMonitor());
									if (!file.getParent().exists()) {
										file.getParent().getRawLocation().toFile().mkdirs();
										file.getParent().refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
									}
									is=((StorageTypedElement) right).getContents();
									file.create(is, true, new NullProgressMonitor());
								} catch (CoreException e) {
                                    Activator.logError("Faield to overwrite " + file, e); //$NON-NLS-1$
								} finally {
									IOUtils.closeQuietly(is);
								}
							}
						}
						stack.addAll(Arrays.asList(((ISynchronizeModelElement) o).getChildren()));
					}
				}
			}
		});
	}
}
