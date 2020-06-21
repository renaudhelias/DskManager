package dskmanager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class DskManagerEditor extends JFrame {

	DskManager dm = new DskManager();
	protected DskFile dskFile;
	// filename=> baos
	LinkedHashMap<String, ByteArrayOutputStream> list;
	
	JPanel bottomMenu = new JPanel();
	private JButton buttonNew = new JButton("New");
	private JButton buttonLoad = new JButton("Load");
	public JTable table;
	
	DefaultTableModel model = new DefaultTableModel();

	private static DskManagerEditor jFrame;
	public DskManagerEditor() {
		super("CPC Dsk Manager");
		
		setLayout(new BorderLayout());
		
		
		table=new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setTransferHandler(new TransferHelper(this));
        table.setDragEnabled(true);
        table.setDropMode(DropMode.USE_SELECTION);
        table.setFillsViewportHeight(true);
        
		add(scrollPane,BorderLayout.CENTER);
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(bottomMenu,BorderLayout.SOUTH);
		bottomMenu.setLayout(new BorderLayout());
		bottomMenu.add(buttonNew,BorderLayout.WEST);
		bottomMenu.add(buttonLoad,BorderLayout.EAST);
		
		buttonNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setDialogTitle("Create new dsk file");
				int response = jfc.showSaveDialog(DskManagerEditor.this);
				if (response == JFileChooser.APPROVE_OPTION) {
				    File fileToSave = jfc.getSelectedFile();
				    try {
				    	dskFile=dm.newDsk(fileToSave.getParentFile(), fileToSave.getName());
				    	updateTable();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		buttonLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setDialogTitle("Load a dsk file");
				int response = jfc.showOpenDialog(DskManagerEditor.this);
				if (response == JFileChooser.APPROVE_OPTION) {
				    File fileToLoad = jfc.getSelectedFile();
				    try {
				    	dskFile=dm.loadDsk(fileToLoad.getParentFile(), fileToLoad.getName());
				    	updateTable();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		
		model.addColumn("Filename");
		model.addColumn("Size");
		
		model.addRow(new Object[]{"TRON.BAS", "2KB"});
		model.addRow(new Object[]{"FRUITY.BAS", "3KB"});
	
		model.removeRow(0);
	}
	
	public void updateTable() throws IOException {
		model.setRowCount(0);
		
		list = dm.listFiles(dskFile);
		for (String filename: list.keySet()) {
			model.addRow(new Object[]{filename, list.get(filename).size()});
		}
	}
	
	public static void main(String[] args) {
		jFrame = new DskManagerEditor();
		jFrame.setSize(300, 200);
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jFrame.setVisible(true);
	}

}


