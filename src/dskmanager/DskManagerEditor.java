package dskmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.DropMode;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class DskManagerEditor extends JFrame {

    DskManager dm = new DskManager();
    protected DskFile dskFile;
    // filename=> baos
    LinkedHashMap<String, ByteArrayOutputStream> list;

    JPanel bottomMenu = new JPanel();
    JPanel infoContent = new JPanel();
    JTextArea info = new JTextArea();
    private JButton buttonNew = new JButton("New");
    private JButton buttonLoad = new JButton("Load");
    public JTable table;

    DefaultTableModel model = new DefaultTableModel();

    private static DskManagerEditor jFrame;

    public DskManagerEditor() {
        super("CPC Dsk Manager");

        setLayout(new BorderLayout());

        table = new JTable(model) {
            DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();

            { // initializer block
                renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public TableCellRenderer getCellRenderer(int arg0, int arg1) {
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

        infoContent.setLayout(new BorderLayout());
        infoContent.add(scrollPane, BorderLayout.CENTER);
        infoContent.add(table.getTableHeader(), BorderLayout.NORTH);
        info.setPreferredSize(new Dimension(-1,20));
        infoContent.add(info, BorderLayout.SOUTH);
        add(infoContent, BorderLayout.CENTER);
        add(bottomMenu, BorderLayout.SOUTH);
        bottomMenu.setLayout(new BorderLayout());
        bottomMenu.add(buttonNew, BorderLayout.WEST);
        bottomMenu.add(buttonLoad, BorderLayout.EAST);

        buttonNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setDialogTitle("Create new dsk file");
                int response = jfc.showSaveDialog(DskManagerEditor.this);
                if (response == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = jfc.getSelectedFile();
                    try {
                        int dialogButton = JOptionPane.YES_NO_OPTION;
                        int dialogResult = JOptionPane.showConfirmDialog(null, "Format DOSD2 (or else let SS40)", "WARNING", dialogButton);
                        if (dialogResult == 0) {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.DOSD2);
                        } else {
                            dskFile = dm.newDsk(fileToSave.getParentFile(), fileToSave.getName(), DskType.SS40);
                        }
                        updateTable();
                        setTitle("CPC Dsk Manager - " + dskFile.file.getName() + " - " + dskFile.master.type);
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
                        dskFile = dm.loadDsk(fileToLoad.getParentFile(), fileToLoad.getName());
                        updateTable();
                        setTitle("CPC Dsk Manager - " + dskFile.file.getName() + " - " + dskFile.master.type);
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
                            updateTable();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        model.addColumn("Filename");
        model.addColumn("Size");
        model.addColumn("Type");
        model.addColumn("Protected");
        model.addColumn("System");
    }

    public void updateTable() throws IOException {
        model.setRowCount(0);

        list = dm.listFiles(dskFile);
        int size=0;
        if (dskFile.master.type==DskType.SS40) {
        	size = 178;
        } else if (dskFile.master.type==DskType.DOSD2) {
        	size = 712;
        }
        List<String>filenames = new ArrayList<String>(list.keySet());
        Collections.sort(filenames);
        for (String filename : filenames) {
            // has AMSDOS Header, so is a binary (like ManageDsk)
            boolean isBinary = dskFile.master.CheckAMSDOS(list.get(filename).toByteArray());
            int type = 0;
            String Type = "BAS";
            if (isBinary) {
                type = list.get(filename).toByteArray()[18] & 0x0ff;
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
            StringBuilder nameBuilder = new StringBuilder();
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
                nameBuilder.append((char) ((filename.charAt(i)) & 0x7f));
            }
//            String fname = nameBuilder.toString();
//            fname = fname.replace(".", "~~~");
//            String[] test = fname.split("~~~");
//            if (test.length > 1 && test[1] != null) {
//                while (test[1].length() < 3) {
//                    test[1] += " ";
//                }
//                fname = test[0].replace(" ", "");
//                fname+= "." + test[1];
//            }
            String prot = Protected?"*":"";
            String sys = System?"*":"";
            model.addRow(new Object[]{filename, (list.get(filename).size() / 1024) + "kb", isBinary ? Type : "ASC",prot,sys});
            size-=(list.get(filename).size() / 1024);
        }
        info.setText("Free: "+size+"kb");
    }

    public static void main(String[] args) {
        jFrame = new DskManagerEditor();
        jFrame.setSize(300, 200);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

}
