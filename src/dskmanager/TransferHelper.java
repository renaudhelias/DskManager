package dskmanager;

import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class TransferHelper extends TransferHandler {

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// TODO Auto-generated method stub
		super.exportAsDrag(comp, e, action);
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		// TODO Auto-generated method stub
		return super.importData(comp, t);
	}

	@Override
	public boolean importData(TransferSupport support) {
		// TODO Auto-generated method stub
		return super.importData(support);
	}

}
