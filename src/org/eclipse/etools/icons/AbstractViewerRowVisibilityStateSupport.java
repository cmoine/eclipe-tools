package org.eclipse.etools.icons;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewerRow;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;

public abstract class AbstractViewerRowVisibilityStateSupport {
  private List<TreeViewerRow> currentItems = new ArrayList<TreeViewerRow>();
  protected ColumnViewer columnViewer;
  private ListenerList<ViewerRowStateChangeListener> listenerList = new ListenerList<ViewerRowStateChangeListener>();
 
  public AbstractViewerRowVisibilityStateSupport(ColumnViewer columnViewer) {
    this.columnViewer = columnViewer;
 
    Listener l = new Listener() {
      public void handleEvent(Event event) {
        List<TreeViewerRow> list = recalculateVisibleItems();
        List<TreeViewerRow> itemsVisible = new ArrayList<TreeViewerRow>();
  
//        Iterator<TreeViewerRow> it = list.iterator();
//        Object obj;
 
//        while( it.hasNext() ) {
//          obj = it.next();
        for(TreeViewerRow obj: list) {
          if( ! currentItems.remove(obj) ) {
            itemsVisible.add(obj);
          }
        }
 
        List<TreeViewerRow> hiddenItems = currentItems;
        currentItems = list;
 
        if( itemsVisible.size() > 0 || hiddenItems.size() > 0 ) {
          if( ! listenerList.isEmpty() ) {
            ColumnViewer v;
            v = AbstractViewerRowVisibilityStateSupport.this.columnViewer;
 
            ViewerRowStateChangedEvent ev = new ViewerRowStateChangedEvent(v);
            ev.itemsHidden = hiddenItems;
            ev.itemsVisible = itemsVisible;
 
            Object[] listeners = listenerList.getListeners();
            ViewerRowStateChangeListener l;
 
            for( int i = 0; i < listeners.length; i++ ) {
              l = (ViewerRowStateChangeListener)listeners[i];
              l.itemStateChangedListener(ev);
            }
          }
        } 
      }
    };
    addListeners(getControl(),l);
  }
 
  protected abstract void addListeners(Scrollable control, Listener l);
 
  protected abstract TreeViewerRow getTopRow();
  
  protected Scrollable getControl() {
    return (Scrollable)columnViewer.getControl();
  }
 
  public void addItemStateListener(ViewerRowStateChangeListener listener) {
    listenerList.add(listener);
  }
 
  private List<TreeViewerRow> recalculateVisibleItems() {
    List<TreeViewerRow> list = new ArrayList<TreeViewerRow>(100);
    TreeViewerRow topRow = getTopRow();
 
    if( topRow != null ) {
      int totalHeight = getControl().getClientArea().height;
      int itemHeight = topRow.getBounds().height;
 
      list.add(topRow);
 
      int tmp = topRow.getBounds().x+itemHeight;
      // tmp += itemHeight;
      // this would be more precise but half rows
      // would be marked as non-visible
      // run until we reached the end of the client-area
      while( tmp < totalHeight ) {
        tmp += itemHeight;
        topRow = (TreeViewerRow) topRow.getNeighbor(ViewerRow.BELOW, false);
  
        if( topRow == null ) {
          break;
        }
        list.add(topRow);
      }
    }
    return list;
  }
}