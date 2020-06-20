package dskmanager;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TransferHelper extends TransferHandler {

	private DefaultTableModel model;
	private JTable table;

	public TransferHelper(JTable table) {
		this.model = (DefaultTableModel) table.getModel();
		this.table = table;
		 table.addMouseMotionListener(new MouseMotionListener() {
			    public void mouseDragged(MouseEvent e) {
			        e.consume();
			        JComponent c = (JComponent) e.getSource();
			        exportAsDrag(c, e, TransferHandler.MOVE);
			    }

			    public void mouseMoved(MouseEvent e) {
			    }
			});
	}
    public boolean canImport(TransferHandler.TransferSupport info) {
        // Spammed => bien pour le curseur
//    	System.out.println("canImport?");
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor) && !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        	table.setCursor(DragSource.DefaultMoveNoDrop);
            return false;
        }

        JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
        if (dl.getRow() == -1) {
        	table.setCursor(DragSource.DefaultMoveNoDrop);
            return true;
        }
        
        table.setCursor(DragSource.DefaultMoveDrop);
        return true;
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
         
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            System.out.println("List doesn't accept a drop of this type.");
            return false;
        }
//        System.out.println("from Desktop");
        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        List<File> data;
        try {
            data = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
        } 
        catch (Exception e) {
        	return false;
        }
         
        for (File f:data) {
        	model.addRow(new Object []{f.getName(),f.length()});
        }
         
        return true;
    }
     
    public int getSourceActions(JComponent c) {
        return COPY;
    }
     
    @Override
    protected Transferable createTransferable(JComponent c) {
//    	System.out.println("to Desktop");
    	
    	JTable list = (JTable)c;
        int[] values = list.getSelectedRows();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(10);
        
        List<File> files= new ArrayList<File>();
        for (int v :values) {
        	File dossierTmp = new File("tmp");
        	dossierTmp.mkdirs();
        	File tmpFile = new File(dossierTmp,(String)model.getValueAt(v, 0));
        	
        	System.out.println("Creating File to move : "+tmpFile.getAbsolutePath());
        	try {
				FileOutputStream fos = new FileOutputStream(tmpFile);
				fos.write(bos.toByteArray());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
        	files.add(tmpFile);
        }
		return new FileTransferable(files);
    }
	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		// Spammed => bien pour le curseur
//		System.out.println("exportDone?");
		if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
	       table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    }
		super.exportDone(c, t, act);
	}
	
	private class FileTransferable implements Transferable {

        private List<File> files;

        public FileTransferable(List<File> files) {
            this.files = files;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.javaFileListFlavor);
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return files;
        }
    }

}
