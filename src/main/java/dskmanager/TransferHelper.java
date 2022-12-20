package dskmanager;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class TransferHelper extends TransferHandler {

	private final static Logger LOGGER = Logger.getLogger(DskManager.class.getName());
	
	private DskManagerEditor dskManagerEditor;

    public TransferHelper(DskManagerEditor dskManagerEditor) {
		this.dskManagerEditor = dskManagerEditor;
	}
    
	public boolean canImport(TransferHandler.TransferSupport info) {
        // Spammed => bien pour le curseur
    	LOGGER.finest("canImport?");
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        	dskManagerEditor.table.setCursor(DragSource.DefaultMoveNoDrop);
            return false;
        }

        dskManagerEditor.table.setCursor(DragSource.DefaultMoveDrop);
        return true;
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        
        dskManagerEditor.table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        	LOGGER.info("List doesn't accept a drop of this type.");
            return false;
        }
        LOGGER.finer("from Desktop");
        

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        return importDataTransferable(t);
    }
    
    public boolean importDataTransferable(Transferable t) {
        try {
        	boolean generateAMSDOSHeader = false;
        	boolean generateAMSDOSHeaderDone = false;
        	for (File file : (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor)) {
        		String realrealname = (dskManagerEditor.dskFile == null ? null : dskManagerEditor.dskFile.master.realname2realname(file.getName()));
    			if (file.getName().toUpperCase().endsWith(".DSK")) {
    				// load dsk
    				dskManagerEditor.dskFile = dskManagerEditor.dm.loadDsk(file.getParentFile(), file.getName());
    				if (dskManagerEditor.dskFile.master.type == null) {
    					dskManagerEditor.ejectTable();
    					JOptionPane.showMessageDialog(dskManagerEditor, "Disk unknown");
    				} else {
    					Settings.set(Settings.lastpath, file.getParent()+"/");
                        Settings.set(Settings.lastopened,file.getAbsolutePath());
    				}
    			} else if (dskManagerEditor.dskFile == null || dskManagerEditor.dskFile.master.type == null) {
    				dskManagerEditor.ejectTable();
    				JOptionPane.showMessageDialog(dskManagerEditor, "Disk unknown");
    			} else if (!dskManagerEditor.dm.listFiles(dskManagerEditor.dskFile).containsKey(realrealname)) {
        			if (dskManagerEditor.freeSize*1024 >= file.length()) {
        				if (!generateAMSDOSHeaderDone) {
        					// Ask only one time this question
        					generateAMSDOSHeader = (JOptionPane.showConfirmDialog(dskManagerEditor, "Add AMSDOS Header", "WARNING",
        				        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
        					generateAMSDOSHeaderDone = true;
        				}
        				dskManagerEditor.dm.addFile(dskManagerEditor.dskFile,file.getParentFile(), file.getName(), generateAMSDOSHeader ? true : null);
            		} else {
            			JOptionPane.showMessageDialog(dskManagerEditor, "Full disk.", "Warning", JOptionPane.ERROR_MESSAGE);
            			break;
        			}
        		} else {
        			// replace ? no.
        			JOptionPane.showMessageDialog(dskManagerEditor, "File already present.", "Warning", JOptionPane.ERROR_MESSAGE);
        		}
        	}
        	if (dskManagerEditor.dskFile != null && dskManagerEditor.dskFile.master.type != null) {
        		dskManagerEditor.updateTable();
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }
         
        return true;
    }
     
    public int getSourceActions(JComponent c) {
        return COPY;
    }
     
    @Override
    public Transferable createTransferable(JComponent c) {
    	LOGGER.finer("to Desktop");
    	
    	JTable list = (JTable)c;
        int[] values = list.getSelectedRows();
        
        List<File> files= new ArrayList<File>();
        for (int v :values) {
        	File dossierTmp = new File("tmp");
        	dossierTmp.deleteOnExit();
        	dossierTmp.mkdirs();
        	String filename = (String) dskManagerEditor.model.getValueAt(v, 0);
    		// FutureOS filename contains space in middle of filename
    		// left trim.
        	String filenameDrop=filename.substring(0,8).replaceAll("\\s+$","")+filename.substring(8,12).replaceAll("\\s+$","");
        	File tmpFile = new File(dossierTmp,filenameDrop);
        	tmpFile.deleteOnExit();
        	
        	LOGGER.finer("Creating File to move : "+tmpFile.getAbsolutePath());
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
		LOGGER.finest("exportDone?");
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
