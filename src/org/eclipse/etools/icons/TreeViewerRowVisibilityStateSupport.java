package org.eclipse.etools.icons;

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.google.common.base.Throwables;

// see https://tomsondev.bestsolution.at/2007/01/05/what-items-are-visible-in-tabletree/
public class TreeViewerRowVisibilityStateSupport extends AbstractViewerRowVisibilityStateSupport {
	private Method method;

	public TreeViewerRowVisibilityStateSupport(TreeViewer columnViewer) {
		super(columnViewer);
		try {
			method = TreeViewer.class.getDeclaredMethod("getViewerRowFromItem", Widget.class); //$NON-NLS-1$
			method.setAccessible(true);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	protected void addListeners(Scrollable control, Listener l) {
		control.getVerticalBar().addListener(SWT.Selection, l);
		control.addListener(SWT.Resize, l);
		control.addListener(SWT.MouseUp, l);
		control.addListener(SWT.KeyUp, l);
	}

	@Override
	protected TreeViewerRow getTopRow() {
		TreeItem topItem = ((Tree) getControl()).getTopItem();
		if (topItem != null) {
			try {
				return (TreeViewerRow) method.invoke(columnViewer, topItem);
			} catch (Exception e) {
				// NO-OP
			}
		}
		return null;
	}
}