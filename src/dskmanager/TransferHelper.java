package dskmanager;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
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

public class TransferHelper extends TransferHandler {

	private DskManagerEditor dskManagerEditor;

    public TransferHelper(DskManagerEditor dskManagerEditor) {
		this.dskManagerEditor = dskManagerEditor;
	}
    
	public boolean canImport(TransferHandler.TransferSupport info) {
        // Spammed => bien pour le curseur
//    	System.out.println("canImport?");
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor) && !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        	dskManagerEditor.table.setCursor(DragSource.DefaultMoveNoDrop);
            return false;
        }

        JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
        if (dl.getRow() == -1) {
        	dskManagerEditor.table.setCursor(DragSource.DefaultMoveNoDrop);
            return true;
        }
        
        dskManagerEditor.table.setCursor(DragSource.DefaultMoveDrop);
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
        dskManagerEditor.table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        try {
        	for (File file : (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor)) {
        		if (!dskManagerEditor.dm.listFiles(dskManagerEditor.dskFile).containsKey(dskManagerEditor.dskFile.master.realname2realname(file.getName()))) {
            		dskManagerEditor.dm.addFile(dskManagerEditor.dskFile,file.getParentFile(), file.getName(), false);
                	dskManagerEditor.model.addRow(new Object []{file.getName(),file.length()});
        		}
        	}
        	dskManagerEditor.updateTable();
        } 
        catch (Exception e) {
        	return false;
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
        	String filename = (String) dskManagerEditor.model.getValueAt(v, 0);
        	File tmpFile = new File(dossierTmp,filename);
        	
        	System.out.println("Creating File to move : "+tmpFile.getAbsolutePath());
        	try {
				FileOutputStream fos = new FileOutputStream(tmpFile);
				fos.write(dskManagerEditor.list.get(filename).toByteArray());
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
			dskManagerEditor.table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
