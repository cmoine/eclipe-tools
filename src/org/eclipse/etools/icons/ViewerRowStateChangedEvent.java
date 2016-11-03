package org.eclipse.etools.icons;

import java.util.EventObject;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewerRow;

public class ViewerRowStateChangedEvent extends EventObject {
  private static final long serialVersionUID = 1L;
 
  public List<TreeViewerRow> itemsHidden;
  public List<TreeViewerRow> itemsVisible;
 
  public ViewerRowStateChangedEvent(Object source) {
    super(source);
  }
}