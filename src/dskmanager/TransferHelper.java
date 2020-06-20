package dskmanager;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TransferHelper extends TransferHandler {

	private DefaultTableModel model;

	public TransferHelper(DefaultTableModel model) {
		this.model = model;
	}
    public boolean canImport(TransferHandler.TransferSupport info) {
        // we only import Strings
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor) && !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }

        JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
        if (dl.getRow() == -1) {
            return false;
        }
        return true;
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
         
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            System.out.println("List doesn't accept a drop of this type.");
            return false;
        }

        JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
        DefaultTableModel listModel = model;
        int index = dl.getIndex();
        boolean insert = dl.isInsert();
        // Get the current string under the drop.
        String value = (String)listModel.getValueAt(index,0);

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        String data;
        try {
            data = (String)t.getTransferData(DataFlavor.stringFlavor);
        } 
        catch (Exception e) { return false; }
         
        // Display a dialog with the drop information.
        String dropValue = "\"" + data + "\" dropped ";
        if (dl.isInsert()) {
            if (dl.getIndex() == 0) {
                System.out.println(dropValue + "at beginning of list");
            } else if (dl.getIndex() >= model.getRowCount()) {
            	System.out.println(dropValue + "at end of list");
            } else {
                String value1 = (String)model.getValueAt(dl.getIndex() - 1,0);
                String value2 = (String)model.getValueAt(dl.getIndex(),0);
                System.out.println(dropValue + "between \"" + value1 + "\" and \"" + value2 + "\"");
            }
        } else {
        	System.out.println(dropValue + "on top of " + "\"" + value + "\"");
        }
         
/**  This is commented out for the basicdemo.html tutorial page.
         **  If you add this code snippet back and delete the
         **  "return false;" line, the list will accept drops
         **  of type string.
        // Perform the actual import.  
        if (insert) {
            listModel.add(index, data);
        } else {
            listModel.set(index, data);
        }
        return true;
*/
return false;
    }
     
    public int getSourceActions(JComponent c) {
        return COPY;
    }
     
    protected Transferable createTransferable(JComponent c) {
    	JTable list = (JTable)c;
        int[] values = list.getSelectedRows();
 
        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < values.length; i++) {
            Object val = model.getValueAt(values[i],0);
            buff.append(val == null ? "" : val.toString());
            if (i != values.length - 1) {
                buff.append("\n");
            }
        }
        return new StringSelection(buff.toString());
    }

}
