package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import no.uib.prideconverter.util.ComboBoxInputable;
import no.uib.prideconverter.util.MyComboBoxRenderer;
import no.uib.prideconverter.util.Util;
import uk.ac.ebi.pride.model.implementation.core.ProtocolStepImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.interfaces.core.ProtocolStep;

/**
 * A frame where information about the protocol used can be inserted.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class ProtocolDetails extends javax.swing.JFrame implements ComboBoxInputable {

    private boolean valuesChanged = false;
    private String lastSelectedProtocolName;
    private String protocolPath;

    /**
     * Opens a new ProtocolDetails frame.
     * 
     * @param location where to locate the frame on the screen
     */
    public ProtocolDetails(Point location) {

        this.setPreferredSize(new Dimension(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT));
        this.setSize(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT);
        this.setMaximumSize(new Dimension(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT));
        this.setMinimumSize(new Dimension(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT));

        initComponents();

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        protocolStepsJTable.getTableHeader().setReorderingAllowed(false);
        protocolStepsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        protocolStepsJTable.setRowHeight(protocolStepsJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        setTitle(PRIDEConverter.getWizardName() + " " +
                PRIDEConverter.getPrideConverterVersionNumber() + " - " + getTitle());

        protocolStepsJTable.getColumn(" ").setMinWidth(40);
        protocolStepsJTable.getColumn(" ").setMaxWidth(40);

        protocolPath = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
        protocolPath = protocolPath.substring(5, protocolPath.lastIndexOf("/"));
        protocolPath = protocolPath.substring(0, protocolPath.lastIndexOf("/") +
                1) + "Properties/Protocols/";
        protocolPath = protocolPath.replace("%20", " ");

        // set the identification type (2D gel or gel free)
        if (PRIDEConverter.getProperties().isGelFree()) {
            gelFreeJRadioButton.setSelected(true);
            twoDimmensionalGelJRadioButton.setSelected(false);
        } else {
            gelFreeJRadioButton.setSelected(false);
            twoDimmensionalGelJRadioButton.setSelected(true);
        }

        readProtocolsFromFile();
        mandatoryFieldsCheck();

        setLocation(location);
        setVisible(true);
    }

    /**
     * Reads all the protocols from file, inserts the names into the 
     * name combo box and updates the protocol details to the selected 
     * protocol.
     */
    private void readProtocolsFromFile() {

        File file = new File(protocolPath);
        String tempProtocolName = null;

        try {
            if (!file.exists()) {
                file.mkdir();
            }

            File[] protocolFiles = file.listFiles();

            FileReader f = null;
            BufferedReader b = null;
            Vector protocolNames = new Vector();

            for (int i = 0; i < protocolFiles.length; i++) {

                if (protocolFiles[i].getAbsolutePath().endsWith(".pro")) {

                    f = new FileReader(protocolFiles[i]);
                    b = new BufferedReader(f);

                    tempProtocolName = b.readLine();
                    tempProtocolName = tempProtocolName.substring(tempProtocolName.indexOf(": ") + 2);
                    protocolNames.add(tempProtocolName);

                    b.close();
                    f.close();
                }
            }

            java.util.Collections.sort(protocolNames);

            Vector comboboxTooltips = new Vector();
            comboboxTooltips.add(null);

            for (int i = 0; i < protocolNames.size(); i++) {
                comboboxTooltips.add(protocolNames.get(i));
            }

            comboboxTooltips.add(null);

            protocolNames.insertElementAt("- Please Select a Protocol -", 0);
            protocolNames.add("   Create a New Protocol...");

            namesJComboBox.setRenderer(new MyComboBoxRenderer(comboboxTooltips, SwingConstants.CENTER));
            namesJComboBox.setModel(new DefaultComboBoxModel(protocolNames));
            namesJComboBox.setSelectedItem(PRIDEConverter.getUserProperties().getCurrentProtocol());

            lastSelectedProtocolName = PRIDEConverter.getUserProperties().getCurrentProtocol();

            namesJComboBoxActionPerformed(null);

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this, "The file " + tempProtocolName + " could not be found.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to read file: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this, "An error occured when trying to save the file " + tempProtocolName + ".",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to read file: ");
            ex.printStackTrace();
        }
    }

    /**
     * If a valid protocol is selected the Next button is enabled, otherwise 
     * it is disabled.
     */
    private void mandatoryFieldsCheck() {
        if (protocolStepsJTable.getRowCount() > 0) {
            nextJButton.setEnabled(true);
        } else {
            nextJButton.setEnabled(false);
        }

        if (namesJComboBox.getSelectedIndex() != 0 &&
                namesJComboBox.getSelectedIndex() !=
                namesJComboBox.getItemCount() - 1 &&
                namesJComboBox.getSelectedIndex() != -1) {
            deleteJButton.setEnabled(true);
        } else {
            deleteJButton.setEnabled(false);
        }
    }

    /**
     * Inserts a protocol step into the table.
     * 
     * @param terms
     * @param accessions
     * @param cv
     * @param values
     * @param modifiedRow the row to modify, use -1 if adding a new row
     */
    public void insertProtocolStep(Vector terms, Vector accessions, Vector cv, Vector values, int modifiedRow) {

        valuesChanged = true;

        String temp = "";
        Collection protocolStepsCVParams = new ArrayList(terms.size());

        for (int i = 0; i < terms.size(); i++) {
            if (values.get(i) != null) {
                temp += "[" + terms.get(i) + ": " + values.get(i) + "] ";

                protocolStepsCVParams.add(new CvParamImpl(
                        (String) accessions.get(i),
                        (String) cv.get(i),
                        (String) terms.get(i),
                        new Long(i).longValue(),
                        (String) values.get(i)));
            } else {
                temp += "[" + terms.get(i) + "] ";

                protocolStepsCVParams.add(new CvParamImpl(
                        (String) accessions.get(i),
                        (String) cv.get(i),
                        (String) terms.get(i),
                        new Long(i).longValue(),
                        null));
            }
        }

        if (modifiedRow == -1) {

            PRIDEConverter.getProperties().getExperimentProtocolSteps().add(
                    new ProtocolStepImpl(protocolStepsJTable.getRowCount(),
                    protocolStepsCVParams, null));

            ((DefaultTableModel) protocolStepsJTable.getModel()).addRow(
                    new Object[]{
                        new Integer(protocolStepsJTable.getRowCount() + 1),
                        temp
                    });
        } else {

            protocolStepsJTable.setValueAt(temp, modifiedRow, 1);

            Collection<ProtocolStep> tmp = PRIDEConverter.getProperties().getExperimentProtocolSteps();
            ProtocolStep[] protocolSteps = tmp.toArray(new ProtocolStep[tmp.size()]);

            protocolSteps[modifiedRow] = new ProtocolStepImpl(protocolStepsJTable.getRowCount(),
                    protocolStepsCVParams, null);

            PRIDEConverter.getProperties().setExperimentProtocolSteps(
                    new ArrayList(protocolStepsJTable.getRowCount()));

            for (ProtocolStep protocolStep : protocolSteps) {
                PRIDEConverter.getProperties().getExperimentProtocolSteps().add(protocolStep);
            }
        }

        mandatoryFieldsCheck();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupJMenu = new javax.swing.JPopupMenu();
        editJMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        moveUpJMenuItem = new javax.swing.JMenuItem();
        moveDownJMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        deleteSelectedRowJMenuItem = new javax.swing.JMenuItem();
        popupMenu = new javax.swing.JPopupMenu();
        deleteJMenuItem = new javax.swing.JMenuItem();
        identificationTypeButtonGroup = new javax.swing.ButtonGroup();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        protocolStepsJTable = new javax.swing.JTable();
        addJButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        namesJComboBox = new javax.swing.JComboBox();
        deleteJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        gelFreeJRadioButton = new javax.swing.JRadioButton();
        twoDimmensionalGelJRadioButton = new javax.swing.JRadioButton();

        editJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        editJMenuItem.setMnemonic('E');
        editJMenuItem.setText("Edit");
        editJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(editJMenuItem);
        popupJMenu.add(jSeparator3);

        moveUpJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        moveUpJMenuItem.setMnemonic('U');
        moveUpJMenuItem.setText("Move Up");
        moveUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(moveUpJMenuItem);

        moveDownJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        moveDownJMenuItem.setMnemonic('D');
        moveDownJMenuItem.setText("Move Down");
        moveDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(moveDownJMenuItem);
        popupJMenu.add(jSeparator4);

        deleteSelectedRowJMenuItem.setMnemonic('L');
        deleteSelectedRowJMenuItem.setText("Delete");
        deleteSelectedRowJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedRowJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(deleteSelectedRowJMenuItem);

        deleteJMenuItem.setText("Delete Protocol");
        deleteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(deleteJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Protocol Properties - Step 5 of 8");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        nextJButton.setText("Next  >");
        nextJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });

        backJButton.setText("< Back");
        backJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        backJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC), jLabel3.getFont().getSize()-2));
        jLabel3.setText("Select a protocol from the list, or create your own, and click on 'Next' to continue.");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Protocol"));

        jLabel21.setText("Name:");

        protocolStepsJTable.setFont(protocolStepsJTable.getFont());
        protocolStepsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Terms"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        protocolStepsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                protocolStepsJTableMouseClicked(evt);
            }
        });
        protocolStepsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                protocolStepsJTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(protocolStepsJTable);

        addJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        addJButton.setText("Add Protocol Step");
        addJButton.setEnabled(false);
        addJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        addJButton.setMaximumSize(new java.awt.Dimension(151, 23));
        addJButton.setMinimumSize(new java.awt.Dimension(151, 23));
        addJButton.setPreferredSize(new java.awt.Dimension(151, 23));
        addJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Protocol Steps:");

        namesJComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                namesJComboBoxMouseClicked(evt);
            }
        });
        namesJComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                namesJComboBoxItemStateChanged(evt);
            }
        });
        namesJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namesJComboBoxActionPerformed(evt);
            }
        });

        deleteJButton.setFont(new java.awt.Font("Arial", 1, 11));
        deleteJButton.setText("X");
        deleteJButton.setToolTipText("Delete Selected Protocol");
        deleteJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteJButton.setIconTextGap(0);
        deleteJButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        deleteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel21)
                        .add(18, 18, 18)
                        .add(namesJComboBox, 0, 468, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteJButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, addJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(namesJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deleteJButton))
                .add(18, 18, 18)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(jPanel1Layout.createSequentialGroup()
                .add(3, 3, 3)
                .add(jLabel21)
                .add(346, 346, 346))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {deleteJButton, namesJComboBox}, org.jdesktop.layout.GroupLayout.VERTICAL);

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Identifications"));

        jLabel2.setText("Identification Type:");

        identificationTypeButtonGroup.add(gelFreeJRadioButton);
        gelFreeJRadioButton.setText("Other");
        gelFreeJRadioButton.setToolTipText("Everything that's not 2D gel based");

        identificationTypeButtonGroup.add(twoDimmensionalGelJRadioButton);
        twoDimmensionalGelJRadioButton.setText("2D Gel");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .add(18, 18, 18)
                .add(twoDimmensionalGelJRadioButton)
                .add(18, 18, 18)
                .add(gelFreeJRadioButton)
                .addContainerGap(294, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(gelFreeJRadioButton)
                    .add(twoDimmensionalGelJRadioButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 267, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Delete the selected protocol step.
     * 
     * @param evt
     */
    private void deleteSelectedRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowJMenuItemActionPerformed

        if (protocolStepsJTable.getSelectedRow() != -1) {

            int selectedRow = protocolStepsJTable.getSelectedRow();
            int selectedColumn = protocolStepsJTable.getSelectedColumn();

            int[] selectedRows = protocolStepsJTable.getSelectedRows();

            for (int i = protocolStepsJTable.getSelectedRows().length - 1; i >= 0; i--) {
                ((DefaultTableModel) protocolStepsJTable.getModel()).removeRow(selectedRows[i]);
            }

            protocolStepsJTable.changeSelection(selectedRow, selectedColumn, false, false);
            protocolStepsJTable.editingCanceled(null);

            //remove from datastructure as well
            Object[] protocolSteps = PRIDEConverter.getProperties().getExperimentProtocolSteps().toArray();

            PRIDEConverter.getProperties().setExperimentProtocolSteps(new ArrayList());

            ProtocolStepImpl tempProtocolStepImpl;

            for (int i = 0; i < protocolSteps.length; i++) {

                if (i != selectedRow) {
                    tempProtocolStepImpl = (ProtocolStepImpl) protocolSteps[i];
                    tempProtocolStepImpl = new ProtocolStepImpl(i,
                            tempProtocolStepImpl.getProtocolStepCvParameterList(), null);

                    PRIDEConverter.getProperties().getExperimentProtocolSteps().add(tempProtocolStepImpl);
                }
            }

            valuesChanged = true;

            fixIndices();

            mandatoryFieldsCheck();
        }
    }//GEN-LAST:event_deleteSelectedRowJMenuItemActionPerformed

    /**
     * Move the selected protocol step down one position in the table.
     * 
     * @param evt
     */
    private void moveDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownJMenuItemActionPerformed
        int selectedRow = protocolStepsJTable.getSelectedRow();
        int selectedColumn = protocolStepsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            protocolStepsJTable.getValueAt(selectedRow + 1, 0),
            protocolStepsJTable.getValueAt(selectedRow + 1, 1)};

        ((DefaultTableModel) protocolStepsJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) protocolStepsJTable.getModel()).insertRow(selectedRow, tempRow);

        protocolStepsJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        fixIndices();

        valuesChanged = true;

        //move in data structure as well
        Object[] protocolSteps = PRIDEConverter.getProperties().getExperimentProtocolSteps().toArray();

        Object tempProtocolStep = protocolSteps[selectedRow + 1];
        protocolSteps[selectedRow + 1] = protocolSteps[selectedRow];
        protocolSteps[selectedRow] = tempProtocolStep;

        PRIDEConverter.getProperties().setExperimentProtocolSteps(new ArrayList(protocolSteps.length));

        ProtocolStepImpl tempProtocolStepImpl;

        for (int i = 0; i < protocolSteps.length; i++) {

            tempProtocolStepImpl = (ProtocolStepImpl) protocolSteps[i];
            tempProtocolStepImpl = new ProtocolStepImpl(i,
                    tempProtocolStepImpl.getProtocolStepCvParameterList(), null);

            PRIDEConverter.getProperties().getExperimentProtocolSteps().add(tempProtocolStepImpl);
        }
    }//GEN-LAST:event_moveDownJMenuItemActionPerformed

    /**
     * Move the selected protocol step up one position in the table.
     * 
     * @param evt
     */
    private void moveUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpJMenuItemActionPerformed
        int selectedRow = protocolStepsJTable.getSelectedRow();
        int selectedColumn = protocolStepsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            protocolStepsJTable.getValueAt(selectedRow - 1, 0),
            protocolStepsJTable.getValueAt(selectedRow - 1, 1),};

        ((DefaultTableModel) protocolStepsJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) protocolStepsJTable.getModel()).insertRow(selectedRow, tempRow);

        protocolStepsJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        fixIndices();

        valuesChanged = true;

        //move in data structure as well
        Object[] protocolSteps = PRIDEConverter.getProperties().getExperimentProtocolSteps().toArray();

        Object tempProtocolStep = protocolSteps[selectedRow - 1];
        protocolSteps[selectedRow - 1] = protocolSteps[selectedRow];
        protocolSteps[selectedRow] = tempProtocolStep;

        PRIDEConverter.getProperties().setExperimentProtocolSteps(new ArrayList(protocolSteps.length));

        ProtocolStepImpl tempProtocolStepImpl;

        for (int i = 0; i < protocolSteps.length; i++) {

            tempProtocolStepImpl = (ProtocolStepImpl) protocolSteps[i];
            tempProtocolStepImpl = new ProtocolStepImpl(i,
                    tempProtocolStepImpl.getProtocolStepCvParameterList(), null);

            PRIDEConverter.getProperties().getExperimentProtocolSteps().add(tempProtocolStepImpl);
        }
    }//GEN-LAST:event_moveUpJMenuItemActionPerformed

    /**
     * Opens a NewProtocolStep where the selected step can be edited.
     * 
     * @param evt
     */
    private void editJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editJMenuItemActionPerformed
        Object[] protocolSteps = PRIDEConverter.getProperties().getExperimentProtocolSteps().toArray();

        new NewProtocolStep(this, true, protocolStepsJTable.getSelectedRow(),
                (ProtocolStepImpl) protocolSteps[protocolStepsJTable.getSelectedRow()]);
    }//GEN-LAST:event_editJMenuItemActionPerformed

    /**
     * Opens a NewProtocolStep where a new step can be created.
     * 
     * @param evt
     */
    private void addJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewProtocolStep(this, true);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_addJButtonActionPerformed

    /**
     * If the user double clicks on a row in the table the NewProtocolStep 
     * dialog is shown where the information about the given step can be 
     * altered. If the user right clicks a pop up menu is shown for editing, 
     * moving or delting the selected step.
     * 
     * @param evt
     */
    private void protocolStepsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_protocolStepsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = protocolStepsJTable.rowAtPoint(evt.getPoint());
            int column = protocolStepsJTable.columnAtPoint(evt.getPoint());

            protocolStepsJTable.changeSelection(row, column, false, false);

            this.moveUpJMenuItem.setEnabled(true);
            this.moveDownJMenuItem.setEnabled(true);

            if (row == protocolStepsJTable.getRowCount() - 1) {
                this.moveDownJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.moveUpJMenuItem.setEnabled(false);
            }

            popupJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_protocolStepsJTableMouseClicked

    /**
     * Stores the selected protocol and opens the next frame (Instrument).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        if (saveInsertedInformation()) {
            new Instrument(this.getLocation());
            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * Stores the selected protocol and opens the previous frame (SampleDetails).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        if (saveInsertedInformation()) {
            new SampleDetails(this.getLocation());
            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_backJButtonActionPerformed

    /**
     * Closes the frame, and end the wizard.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
        PRIDEConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens a help frame.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/ProtocolDetails.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Opens the delete protocol if the user right clicks in the combo box.
     * 
     * @param evt
     */
    private void namesJComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_namesJComboBoxMouseClicked
        if (evt.getButton() == 3) {
            if (namesJComboBox.getSelectedIndex() != 0 &&
                    namesJComboBox.getSelectedIndex() !=
                    namesJComboBox.getItemCount() - 1) {

                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_namesJComboBoxMouseClicked

    /**
     * Sets the current protocol to the protocol selected in the combo box.
     * 
     * @param evt
     */
    private void namesJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_namesJComboBoxItemStateChanged
        PRIDEConverter.getUserProperties().setCurrentProtocol((String) namesJComboBox.getSelectedItem());
        mandatoryFieldsCheck();
    }//GEN-LAST:event_namesJComboBoxItemStateChanged

    /**
     * If the information as been altered, the option to store the data will 
     * be given. Then the information about the selected protocol will be 
     * retrieved.
     * 
     * @param evt
     */
    private void namesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namesJComboBoxActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        boolean cancel = false;

        if (valuesChanged) {

            int value = JOptionPane.showConfirmDialog(this,
                    "Protocol has been changed. Save changes?",
                    "Protocol Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing protocol?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    String newName;
                    newName = protocolPath + lastSelectedProtocolName +
                            ".pro";

                    try {

                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("Name: " + lastSelectedProtocolName + "\n");
                        bw.write(PRIDEConverter.getProperties().getExperimentProtocolSteps().size() +
                                "\n");

                        Iterator steps = PRIDEConverter.getProperties().getExperimentProtocolSteps().iterator();
                        Iterator cvTerms;

                        ProtocolStepImpl tempStep;
                        CvParamImpl cvParams;
                        String tempS;
                        int numberOfTerms;

                        while (steps.hasNext()) {

                            tempStep = (ProtocolStepImpl) steps.next();
                            cvTerms = tempStep.getProtocolStepCvParameterList().iterator();
                            bw.write(tempStep.getProtocolStepCvParameterList().size() + "\n");

                            tempS = "";
                            numberOfTerms = 0;

                            while (cvTerms.hasNext()) {
                                cvParams = (CvParamImpl) cvTerms.next();

                                bw.write("Accession: " + cvParams.getAccession() + "\n");
                                bw.write("CVLookup: " + cvParams.getCVLookup() + "\n");
                                bw.write("Name: " + cvParams.getName() + "\n");
                                bw.write("Value: " + cvParams.getValue() + "\n\n");
                            }
                        }

                        valuesChanged = false;

                        bw.close();
                        r.close();

                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(
                                this, "The file " + newName + " could not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to save file: ");
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                this, "An error occured when trying to save the file " + newName + ".",
                                "File Error", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to save file: ");
                        ex.printStackTrace();
                    }
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;

                    namesJComboBox.setSelectedItem(lastSelectedProtocolName);

                } else { //value == NO

                    String protocolName = JOptionPane.showInputDialog(this,
                            "Provide the name of the new protocol: ",
                            "Protocol Name", JOptionPane.PLAIN_MESSAGE);

                    if (protocolName != null) {

                        String newName;

                        newName = protocolPath +
                                protocolName +
                                ".pro";

                        while (new File(newName).exists()) {
                            protocolName = JOptionPane.showInputDialog(this,
                                    "Name is already in use. Provide a different name: ",
                                    "Protocol Name", JOptionPane.PLAIN_MESSAGE);

                            newName = protocolPath +
                                    protocolName +
                                    ".pro";
                        }

                        if (protocolName != null) {

                            try {

                                FileWriter r = new FileWriter(newName);
                                BufferedWriter bw = new BufferedWriter(r);

                                bw.write("Name: " +
                                        protocolName +
                                        "\n");
                                bw.write(PRIDEConverter.getProperties().getExperimentProtocolSteps().size() +
                                        "\n");

                                Iterator steps = PRIDEConverter.getProperties().getExperimentProtocolSteps().iterator();
                                Iterator cvTerms;

                                ProtocolStepImpl tempStep;
                                CvParamImpl cvParams;
                                String tempS;
                                int numberOfTerms;

                                while (steps.hasNext()) {

                                    tempStep = (ProtocolStepImpl) steps.next();

                                    cvTerms = tempStep.getProtocolStepCvParameterList().iterator();

                                    bw.write(tempStep.getProtocolStepCvParameterList().size() + "\n");

                                    tempS = "";
                                    numberOfTerms = 0;

                                    while (cvTerms.hasNext()) {
                                        cvParams = (CvParamImpl) cvTerms.next();

                                        bw.write("Accession: " +
                                                cvParams.getAccession() + "\n");
                                        bw.write("CVLookup: " +
                                                cvParams.getCVLookup() + "\n");
                                        bw.write("Name: " + cvParams.getName() + "\n");
                                        bw.write("Value: " + cvParams.getValue() + "\n\n");
                                    }
                                }

                                valuesChanged = false;

                                bw.close();
                                r.close();

                                //prideConverter.setCurrentProtocol(protocolName);
                                namesJComboBox.insertItemAt(protocolName, namesJComboBox.getItemCount() - 2);

                            } catch (FileNotFoundException ex) {
                                JOptionPane.showMessageDialog(
                                        this, "The file " + newName + " could not be found.",
                                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Error when trying to save file: ");
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(
                                        this, "An error occured when trying to save the file " + newName + ".",
                                        "File Error", JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Error when trying to save file: ");
                                ex.printStackTrace();
                            }
                        } else {
                            cancel = true;
                            namesJComboBox.setSelectedItem(lastSelectedProtocolName);
                            PRIDEConverter.getUserProperties().setCurrentProtocol(lastSelectedProtocolName);
                        }
                    } else {
                        cancel = true;
                        namesJComboBox.setSelectedItem(lastSelectedProtocolName);
                        PRIDEConverter.getUserProperties().setCurrentProtocol(lastSelectedProtocolName);
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
                namesJComboBox.setSelectedItem(lastSelectedProtocolName);
                PRIDEConverter.getUserProperties().setCurrentProtocol(lastSelectedProtocolName);
            }
        }

        if (!cancel) {
            lastSelectedProtocolName = (String) namesJComboBox.getSelectedItem();
            String selectedProtocolName = (String) namesJComboBox.getSelectedItem();

            PRIDEConverter.getUserProperties().setCurrentProtocol(selectedProtocolName);

            //empty the table
            while (protocolStepsJTable.getRowCount() > 0) {
                ((DefaultTableModel) protocolStepsJTable.getModel()).removeRow(0);
            }

            PRIDEConverter.getProperties().setExperimentProtocolSteps(new ArrayList());

            if (namesJComboBox.getSelectedIndex() == 0) {
                valuesChanged = false;
                addJButton.setEnabled(false);
            } else if (namesJComboBox.getSelectedIndex() ==
                    namesJComboBox.getItemCount() - 1) {

                addJButton.setEnabled(false);
                valuesChanged = false;

                ComboBoxInputDialog input = new ComboBoxInputDialog(this, this, true);
                input.setTitle("Create New Protocol");
                input.setBorderTitle("New Protocol");
                input.setVisible(true);
            } else {
                selectedProtocolName = protocolPath + selectedProtocolName + ".pro";

                addJButton.setEnabled(true);

                try {
                    String temp;

                    FileReader f = new FileReader(selectedProtocolName);
                    BufferedReader b = new BufferedReader(f);

                    b.readLine();
                    int numberOfSteps = new Integer(b.readLine()).intValue();
                    int numberOfCVTerms;

                    Vector names, accessions, cvLookUps, values;

                    for (int i = 0; i < numberOfSteps; i++) {

                        numberOfCVTerms = new Integer(b.readLine()).intValue();

                        names = new Vector();
                        accessions = new Vector();
                        cvLookUps = new Vector();
                        values = new Vector();

                        for (int j = 0; j < numberOfCVTerms; j++) {

                            temp = b.readLine();
                            accessions.add(temp.substring(temp.indexOf(": ") + 2));
                            temp = b.readLine();
                            cvLookUps.add(temp.substring(temp.indexOf(": ") + 2));
                            temp = b.readLine();
                            names.add(temp.substring(temp.indexOf(": ") + 2));
                            temp = b.readLine();
                            temp = temp.substring(temp.indexOf(": ") + 2);

                            if (temp.equalsIgnoreCase("null")) {
                                values.add(null);
                            } else {
                                values.add(temp);
                            }

                            b.readLine();
                        }

                        insertProtocolStep(names, accessions, cvLookUps, values, -1);
                    }

                    b.close();
                    f.close();

                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(
                            this, "The file " + selectedProtocolName + " could not be found.",
                            "File Not Found", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when trying to read file: ");
                    ex.printStackTrace();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            this, "An error occured when trying to read the file " + selectedProtocolName + ".",
                            "File Error", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when trying to read file: ");
                    ex.printStackTrace();
                }

                valuesChanged = false;
            }
        }

        mandatoryFieldsCheck();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_namesJComboBoxActionPerformed

    /**
     * Delete the selected protocol.
     * 
     * @param evt
     */
    private void deleteJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int option = JOptionPane.showConfirmDialog(this, "This will delete the selected protocol. Continue?");

        if (option == JOptionPane.YES_OPTION) {

            String newName = protocolPath +
                    (String) namesJComboBox.getSelectedItem() + ".pro";

            boolean deleted = new File(newName).delete();

            if (!deleted) {
                JOptionPane.showMessageDialog(this, "The file could not be deleted!");
            } else {
                lastSelectedProtocolName = null;
                valuesChanged = false;
                readProtocolsFromFile();
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_deleteJMenuItemActionPerformed

    /**
     * Opens an About PRIDE Converter dialog.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Deletes the selected protocol.
     * 
     * @param evt
     */
    private void deleteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJButtonActionPerformed
        if (namesJComboBox.getSelectedIndex() != 0 &&
                namesJComboBox.getSelectedIndex() !=
                namesJComboBox.getItemCount() - 1 &&
                namesJComboBox.getSelectedIndex() != -1) {

            deleteJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_deleteJButtonActionPerformed

    /**
     * If the delete key is pressed the selected rows in the table are removed.
     * 
     * @param evt
     */
    private void protocolStepsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_protocolStepsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_protocolStepsJTableKeyReleased

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixIndices() {
        for (int row = 0; row < ((DefaultTableModel) protocolStepsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) protocolStepsJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }

    /**
     * Saves the inserted information to the Properties object.
     * 
     * @return true if save was succesfull, false otherwise.
     */
    private boolean saveInsertedInformation() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        PRIDEConverter.getUserProperties().setCurrentProtocol((String) namesJComboBox.getSelectedItem());

        PRIDEConverter.getProperties().setGelFree(gelFreeJRadioButton.isSelected());

        boolean cancel = false;

        if (valuesChanged) {
            int value = JOptionPane.showConfirmDialog(this,
                    "Protocol has been changed. Save changes?",
                    "Protcol Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing protocol?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    String newName;
                    newName = protocolPath +
                            PRIDEConverter.getUserProperties().getCurrentProtocol() + ".pro";

                    try {

                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("Name: " +
                                PRIDEConverter.getUserProperties().getCurrentProtocol() + "\n");
                        bw.write(PRIDEConverter.getProperties().getExperimentProtocolSteps().size() + "\n");

                        Iterator steps = PRIDEConverter.getProperties().getExperimentProtocolSteps().iterator();
                        Iterator cvTerms;

                        ProtocolStepImpl tempStep;
                        CvParamImpl cvParams;
                        String tempS;
                        int numberOfTerms;

                        while (steps.hasNext()) {

                            tempStep = (ProtocolStepImpl) steps.next();
                            cvTerms = tempStep.getProtocolStepCvParameterList().iterator();

                            bw.write(tempStep.getProtocolStepCvParameterList().size() + "\n");

                            tempS = "";
                            numberOfTerms = 0;

                            while (cvTerms.hasNext()) {
                                cvParams = (CvParamImpl) cvTerms.next();

                                bw.write("Accession: " + cvParams.getAccession() + "\n");
                                bw.write("CVLookup: " + cvParams.getCVLookup() + "\n");
                                bw.write("Name: " + cvParams.getName() + "\n");
                                bw.write("Value: " + cvParams.getValue() + "\n\n");
                            }
                        }

                        valuesChanged = false;

                        bw.close();
                        r.close();

                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(
                                this, "The file " + newName + " could not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to save file: ");
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                this, "An error occured when trying to save the file " + newName + ".",
                                "File Error", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to save file: ");
                        ex.printStackTrace();
                    }
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;
                } else { //value == NO

                    String protocolName = JOptionPane.showInputDialog(this,
                            "Provide the name of the new protocol: ",
                            "Protocol Name", JOptionPane.PLAIN_MESSAGE);

                    if (protocolName != null) {

                        String newName;

                        newName = protocolPath + protocolName + ".pro";

                        while (new File(newName).exists()) {
                            protocolName = JOptionPane.showInputDialog(this,
                                    "Name already in use. Provide a different name: ",
                                    "Protocol Name", JOptionPane.PLAIN_MESSAGE);

                            newName = protocolPath + protocolName + ".pro";
                        }

                        if (protocolName != null) {

                            try {

                                FileWriter r = new FileWriter(newName);
                                BufferedWriter bw = new BufferedWriter(r);

                                bw.write("Name: " + protocolName + "\n");
                                bw.write(PRIDEConverter.getProperties().getExperimentProtocolSteps().size() + "\n");

                                Iterator steps = PRIDEConverter.getProperties().getExperimentProtocolSteps().iterator();
                                Iterator cvTerms;

                                ProtocolStepImpl tempStep;
                                CvParamImpl cvParams;
                                String tempS;
                                int numberOfTerms;

                                while (steps.hasNext()) {

                                    tempStep = (ProtocolStepImpl) steps.next();

                                    cvTerms = tempStep.getProtocolStepCvParameterList().iterator();

                                    bw.write(tempStep.getProtocolStepCvParameterList().size() + "\n");

                                    tempS = "";
                                    numberOfTerms = 0;

                                    while (cvTerms.hasNext()) {
                                        cvParams = (CvParamImpl) cvTerms.next();

                                        bw.write("Accession: " +
                                                cvParams.getAccession() + "\n");
                                        bw.write("CVLookup: " +
                                                cvParams.getCVLookup() + "\n");
                                        bw.write("Name: " + cvParams.getName() + "\n");
                                        bw.write("Value: " + cvParams.getValue() + "\n\n");
                                    }
                                }

                                valuesChanged = false;

                                bw.close();
                                r.close();

                                PRIDEConverter.getUserProperties().setCurrentProtocol(protocolName);

                            } catch (FileNotFoundException ex) {
                                JOptionPane.showMessageDialog(
                                        this, "The file " + newName + " could not be found.",
                                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Error when trying to save file: ");
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(
                                        this, "An error occured when trying to save the file " + newName + ".",
                                        "File Error", JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Error when trying to save file: ");
                                ex.printStackTrace();
                            }
                        } else {
                            cancel = true;
                        }
                    } else {
                        cancel = true;
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        return !cancel;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton addJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton deleteJButton;
    private javax.swing.JMenuItem deleteJMenuItem;
    private javax.swing.JMenuItem deleteSelectedRowJMenuItem;
    private javax.swing.JMenuItem editJMenuItem;
    private javax.swing.JRadioButton gelFreeJRadioButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.ButtonGroup identificationTypeButtonGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JMenuItem moveDownJMenuItem;
    private javax.swing.JMenuItem moveUpJMenuItem;
    private javax.swing.JComboBox namesJComboBox;
    private javax.swing.JButton nextJButton;
    private javax.swing.JPopupMenu popupJMenu;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable protocolStepsJTable;
    private javax.swing.JRadioButton twoDimmensionalGelJRadioButton;
    // End of variables declaration//GEN-END:variables

    /**
     * @see ComboBoxInputable
     */
    public void insertIntoComboBox(String text) {

        PRIDEConverter.getUserProperties().setCurrentProtocol(text);

        String newName;
        newName = protocolPath + text + ".pro";

        try {
            FileWriter r = new FileWriter(newName);
            BufferedWriter bw = new BufferedWriter(r);

            bw.write("Name: " + text + "\n");
            bw.write("0");

            bw.close();
            r.close();

            readProtocolsFromFile();

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this, "The file " + newName + " could not be found.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to save file: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this, "An error occured when trying to save the file " + newName + ".",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to save file: ");
            ex.printStackTrace();
        }
    }

    /**
     * @see ComboBoxInputable
     */
    public void resetComboBox() {
        lastSelectedProtocolName = null;
        namesJComboBox.setSelectedIndex(0);
        readProtocolsFromFile();
    }

    /**
     * @see ComboBoxInputable
     */
    public boolean alreadyInserted(String currentProtocolName) {

        String newName;
        newName = protocolPath + currentProtocolName + ".pro";
        File newFile = new File(newName);

        return newFile.exists();
    }
}
