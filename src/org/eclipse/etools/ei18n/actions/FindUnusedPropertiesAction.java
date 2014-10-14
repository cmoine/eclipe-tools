package org.eclipse.etools.ei18n.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ListDialog;

import com.google.common.collect.Lists;

@RemoveMe
public class FindUnusedPropertiesAction extends CompilationUnitAction {
	public FindUnusedPropertiesAction() {
		super("Find unused properties"); //$NON-NLS-1$
	}

	@Override
	public void run() {
		List<ICompilationUnit> cus=getCompilationUnits();

		if (cus != null) {
			for (ICompilationUnit cu : cus) {
				try {
					IType type=cu.getTypes()[0];
					Properties props=getBundle(cu.getResource(), type);
					IFile res=lookupBundle(cu.getResource(), type);
					if (props != null && res != null) {
						// res.delete(true, null);
						final List<String> unusedKeys=Lists.newArrayList();
						for (Object key : props.keySet()) {
							IField field=type.getField((String) key);
							if (field == null || !field.exists()) {
								unusedKeys.add((String) key);
							}
						}
						if (!unusedKeys.isEmpty()) {
							ListDialog dialog=new ListDialog(getShell()) {
								@Override
								protected Control createDialogArea(Composite container) {
									Control result=super.createDialogArea(container);
									Table table=getTableViewer().getTable();
									Menu menu=new Menu(table);
									MenuItem item=new MenuItem(menu, SWT.PUSH);
									item.setText("Copy All in raw mode"); //$NON-NLS-1$
									item.addListener(SWT.Selection, new Listener() {
                                        public void handleEvent(Event event) {
                                            copyToClipboard(unusedKeys);
                                        }
									});
									table.setMenu(menu);
									return result;
								}
							};
							dialog.setMessage("Unused key in '" + res + "': do you want to overwrite file ?"); //$NON-NLS-1$ //$NON-NLS-2$
							dialog.setInput(unusedKeys);
							dialog.setContentProvider(ArrayContentProvider.getInstance());
							dialog.setLabelProvider(new LabelProvider());
							if (dialog.open() == Window.OK) {
								InputStream is=null;
								try {
									// res.delete(true, null);
									is=res.getContents();
									List<String> lines=IOUtils.readLines(is);
									is.close();
									main: for (Iterator<String> iterator=lines.iterator(); iterator.hasNext();) {
										String line=iterator.next();
										for (String unusedKey : unusedKeys) {
											if (line.trim().startsWith(unusedKey)) {
												iterator.remove();
												continue main;
											}
										}
									}
									// copyToClipboard(lines);
									res.delete(true, null);
									res.create(new ByteArrayInputStream(StringUtils.join(lines.toArray(), IOUtils.LINE_SEPARATOR).getBytes()), true, null);
								} catch (IOException e) {
									Activator.log(IStatus.ERROR, "Failed patching " + res, e); //$NON-NLS-1$
								} finally {
									IOUtils.closeQuietly(is);
								}
							}
						}
					}
				} catch (CoreException e) {
					Activator.log(IStatus.ERROR, "Failed to patch " + cu.getResource(), e); //$NON-NLS-1$
				}
			}
			return;
		}
		MessageDialog.openError(getShell(), "Error", "This selection is not supproted"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void copyToClipboard(final List<String> strs) {
		Clipboard cb=new Clipboard(getShell().getDisplay());
		TextTransfer textTransfer=TextTransfer.getInstance();
		cb.setContents(new Object[] { StringUtils.join(strs.toArray(), "\n") }, new Transfer[] { textTransfer }); //$NON-NLS-1$
	}
}
