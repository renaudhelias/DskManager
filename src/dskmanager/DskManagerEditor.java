package dskmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class DskManagerEditor extends JFrame {

	 private static Logger LOGGER;

	  static {
	      System.setProperty("java.util.logging.config",
	              "c:\\logging.properties");
	      //must initialize loggers after setting above property
	      LOGGER = Logger.getLogger(DskManager.class.getName());
	  }
	
    DskManager dm = new DskManager();
    protected DskFile dskFile;
    // filename=> baos
    LinkedHashMap<String, ByteArrayOutputStream> list;
	Map<String, Integer> users;

    JPanel bottomMenu = new JPanel();
    JPanel infoContent = new JPanel();
    JTextArea info = new JTextArea();
    private JButton buttonNew = new JButton("New");
    private JButton buttonLoad = new JButton("Load");
    public JTable table;

    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                return true;
            }
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            String avant = (String) model.getValueAt(row, column);
            String apres = (String) value;
            apres = dskFile.master.realname2realname(apres);
            try {
                dm.renameFile(dskFile, avant, apres);
                super.setValueAt(apres, row, column);
                updateTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    };
    public int freeSize = 0;

    private static DskManagerEditor jFrame;

    public DskManagerEditor() {
        super("CPC Dsk Manager");
        URL iconURL = getClass().getResource("/dskmanager/Save-16x16.png");
        // iconURL is null when not found
        final ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());

        setLayout(new BorderLayout());

        table = new JTable(model) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
                if (column == 2) {
                    renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (column > 2 || column == 0) {
                    renderRight.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    renderRight.setHorizontalAlignment(SwingConstants.LEFT);
                }
                return renderRight;
            }
        };
        Font font = new Font("Monospaced", 1, 11);
        table.setFont(font);
        info.setFont(font);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setTransferHandler(new TransferHelper(this));
        table.setDragEnabled(true);
        table.setDropMode(DropMode.USE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.LIGHT_GRAY);

        infoContent.setLayout(new BorderLayout());
        infoContent.add(scrollPane, BorderLayout.CENTER);
        infoContent.add(table.getTableHeader(), BorderLayout.NORTH);
        info.setPreferredSize(new Dimension(-1, 20));
        infoContent.add(info, BorderLayout.SOUTH);
        info.setDisabledTextColor(Color.BLACK);
        info.setBackground(Color.LIGHT_GRAY);
        info.setEnabled(false);

        add(infoContent, BorderLayout.CENTER);
        add(bottomMenu, BorderLayout.SOUTH);
        bottomMenu.setLayout(new BorderLayout());
        bottomMenu.setBackground(Color.LIGHT_GRAY);

        bottomMenu.add(buttonNew, BorderLayout.WEST);
        bottomMenu.add(buttonLoad, BorderLayout.EAST);

        buttonNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setDialogTitle("Create new dsk file");
                String path = Settings.get(Settings.lastpath, null);
                if (path != null) {
                    jfc.setCurrentDirectory(new File(path));
                }
                FileFilter ff = new FileFilter() {
                    @Override
                    public String getDescription() {
                        return "CPC DSK files (*.dsk)";
                    }

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        } else {
                            return f.getName().toLowerCase().endsWith(".dsk");
                        }
                    }
                };
                jfc.addChoosableFileFilter(ff);
                jfc.setFileFilter(ff);
                int response = jfc.showSaveDialog(DskManagerEditor.this);
                if (response == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = jfc.getSelectedFile();
                    if (!fileToSave.getName().toLowerCase().endsWith(".dsk")) {
                    	fileToSave=new File(fileToSave.getParent(), fileToSave.getName()+".dsk");
                    }
                    try {
                        Object[] values = new Object[] {DskType.PARADOS80, DskType.PARADOS41, DskType.PARADOS40D, DskType.SS40, DskType.DOSD2, DskType.DOSD10, DskType.DOSD20, DskType.DOSD40, DskType.SDOS,  DskType.SYSTEM, DskType.VORTEX};
						Object value = DskType.SS40;
						Object dialogResult = JOptionPane.showInputDialog(DskManagerEditor.this, "Format DOSD2 (or else let SS40)", "WARNING",  JOptionPane.PLAIN_MESSAGE, icon, values, value );
                        if (DskType.PARADOS80.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.PARADOS80);
                        } else if (DskType.PARADOS41.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.PARADOS41);
                        } else if (DskType.PARADOS40D.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.PARADOS40D);
                        } else if (DskType.SS40.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.SS40);
                        } else if (DskType.SYSTEM.equals(dialogResult)) {
                        	dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.SYSTEM);
                        } else if (DskType.VORTEX.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.VORTEX);
                        } else if (DskType.DOSD2.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.DOSD2);
                        } else if (DskType.DOSD10.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.DOSD10);
                        } else if (DskType.DOSD20.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.DOSD20);
                        } else if (DskType.DOSD40.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.DOSD40);
                        } else if (DskType.SDOS.equals(dialogResult)) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.SDOS);
                        }
                        if (dialogResult != null) {
	                        updateTable();
                        }
                        Settings.set(Settings.lastpath, fileToSave.getParent() + "/");
                        Settings.set(Settings.lastopened, fileToSave.getAbsolutePath());
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
                String path = Settings.get(Settings.lastpath, null);
                if (path != null){
                    jfc.setCurrentDirectory(new File(path));
                }
                jfc.setDialogTitle("Load a dsk file");
                FileFilter ff = new FileFilter() {
                    public String getDescription() {
                        return "CPC DSK files (*.dsk)";
                    }

                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        } else {
                            return f.getName().toLowerCase().endsWith(".dsk");
                        }
                    }
                };
                jfc.addChoosableFileFilter(ff);
                jfc.setFileFilter(ff);
                int response = jfc.showOpenDialog(DskManagerEditor.this);
                if (response == JFileChooser.APPROVE_OPTION) {
                    File fileToLoad = jfc.getSelectedFile();
                    try {
                        dskFile = dm.loadDsk(fileToLoad.getParentFile(), fileToLoad.getName());
                        if (dskFile == null || dskFile.master.type == null) {
                        	ejectTable();
                            JOptionPane.showMessageDialog(DskManagerEditor.this, "Disk unknown");
                        } else {
	                        updateTable();
	                        Settings.set(Settings.lastpath, jfc.getSelectedFile().getParent()+"/");
	                        Settings.set(Settings.lastopened, jfc.getSelectedFile().getAbsolutePath());
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        table.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // press delete key
                if (e.getKeyCode() == 127 && table.getSelectedRows().length > 0) {
                    for (int t : table.getSelectedRows()) {
                        try {
                            dm.eraseFile(dskFile, (String) table.getValueAt(t, 0));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    try {
						updateTable();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                }
            }
        });

        model.addColumn("Filename");
        model.addColumn("User");
        model.addColumn("Size");
        model.addColumn("Type");
        model.addColumn("Attributes");
        
        String file = Settings.get(Settings.lastopened, null);
        if (file != null){
            
                    File fileToLoad = new File(file);
                    try {
                        dskFile = dm.loadDsk(fileToLoad.getParentFile(), fileToLoad.getName());
                        updateTable();
                        Settings.set(Settings.lastpath, fileToLoad.getParent()+"/");
                        Settings.set(Settings.lastopened, fileToLoad.getAbsolutePath());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
        }
    }

    public void updateTable() throws IOException {
        model.setRowCount(0);

        list = dm.listFiles(dskFile);
        users = dm.getUserPerFile(dskFile);
        freeSize = 0;
        if (dskFile.master.type == DskType.PARADOS80) {
            freeSize = 396;
        } else if (dskFile.master.type == DskType.PARADOS41) {
            freeSize = 203;
        } else if (dskFile.master.type == DskType.PARADOS40D) {
            freeSize = 396;
        } else if (dskFile.master.type == DskType.SS40) {
            freeSize = 178;
        } else if (dskFile.master.type == DskType.SYSTEM) {
        	freeSize = 169;
        } else if (dskFile.master.type == DskType.VORTEX) {
        	freeSize = 704;
        } else if (dskFile.master.type == DskType.DOSD2) {
            freeSize = 712;
        } else if (dskFile.master.type == DskType.DOSD10) {
        	freeSize = 796;
        } else if (dskFile.master.type == DskType.DOSD20) {
        	freeSize = 792;
        } else if (dskFile.master.type == DskType.DOSD40) {
        	freeSize = 396;
        } else if (dskFile.master.type == DskType.SDOS) {
        	freeSize = 396;
        }
        List<String> filenames = new ArrayList<String>(list.keySet());
        Collections.sort(filenames);
        for (String filename : filenames) {
        	Integer user = users.get(filename);
            byte[] content = list.get(filename).toByteArray();
            // has AMSDOS Header, so is a binary (like ManageDsk)
            boolean isBinary = dskFile.master.CheckAMSDOS(content);
            int type = 0;
            String Type = "BAS";
            if (isBinary) {
                type = content[18] & 0x0ff;
                switch (type) {
                    case 0:
                        Type = "BAS";
                        break;
                    case 1:
                        Type = "BPT";
                        break;
                    case 2:
                        Type = "BIN";
                        break;
                    default:
                        Type = "UNK";
                }

            }
            boolean Protected = false;
            boolean System = false;
            for (int i = 0; i < filename.length(); i++) {
                int check = (char) (filename.charAt(i)) & 0xff;
                if (i == 9 && check > 0x7f) {
                    Protected = true;
                }
                if (i == 10 && check > 0x7f) {
                    System = true;
                }
            }
            String attr = Protected ? "R " : "  ";
            attr = System ? attr + "S" : attr + " ";
            int taille = list.get(filename).size() / 1024;
            if (list.get(filename).size() % 1024 > 0) {
                taille += 1;
            }
            // FIXME Vortex 4KB
            if (dskFile.master.type == DskType.PARADOS80 || dskFile.master.type == DskType.PARADOS40D || dskFile.master.type == DskType.DOSD2 || dskFile.master.type == DskType.DOSD20 || dskFile.master.type == DskType.DOSD40 || dskFile.master.type == DskType.SDOS || dskFile.master.type == DskType.VORTEX) {
                if ((taille / 2) * 2 != taille) {
                    taille += 1; // DOSD2 min file size is 2KB
                }
            }
            model.addRow(new Object[]{filename, ":"+user, (taille) + "kb", isBinary ? Type : "ASC", attr});
            freeSize -= taille;
        }
        
        if (dskFile.master.type == null) {
        	ejectTable();
        } else {
            info.setText("Free: " + freeSize + "kb");
        	setTitle("CPC Dsk Manager - " + dskFile.file.getName() + " - " + dskFile.master.type);
        	table.setBackground(Color.WHITE);
        }
    }
    
	public void ejectTable() {
        Settings.remove(Settings.lastopened);
		
		model.setRowCount(0);
		info.setText("");
		if (dskFile != null) {
			setTitle("CPC Dsk Manager - " + dskFile.file.getName());
		} else {
			setTitle("CPC Dsk Manager");
		}
    	table.setBackground(Color.LIGHT_GRAY);
	}


    public static void main(String[] args) throws SecurityException, IOException {
    	if (args.length == 0) {
    		InputStream stream = DskManager.class.getClassLoader().
    	              getResourceAsStream("dskmanager/logging.properties");
    	    LogManager.getLogManager().readConfiguration(stream);
    		LOGGER.info("OK");
	        jFrame = new DskManagerEditor();
	        jFrame.setSize(475, 400);
	        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	        jFrame.setVisible(true);
    	} else {
    		LOGGER.setLevel(Level.OFF);
    		LOGGER.info("OK");
    		DskManagerConsole console = new DskManagerConsole();
    		console.execute(args);
    	}
    }


}
