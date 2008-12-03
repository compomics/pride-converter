package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import be.proteomics.mascotdatfile.util.mascot.PeptideHit;
import be.proteomics.mascotdatfile.util.mascot.ProteinHit;
import java.awt.Toolkit;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.prideconverter.util.RadioButtonEditor;
import no.uib.prideconverter.util.RadioButtonRenderer;

/**
 * This dialog lets you choose between a different protein isoforms.
 *
 * @author  Harald Barsnes
 * 
 * Created June 2008
 */
public class ProteinIsoFormSelection extends javax.swing.JDialog {

    private static JTable proteinIsoformsJTable;
    private JTableHeader proteinIsoformsTableHeader;
    private OutputDetails outputDetails;
    private DefaultTableModel proteinIsoformsTableModel = null;
    private ButtonGroup buttonGroup;
    private ProgressDialog progressDialog;
    private PRIDEConverter prideConverter;
    private PeptideHit peptideHit;

    /**
     * Opens a new ProteinIsoFormSelection dialog and inserts information 
     * about the detected isoforms.
     * 
     * @param parent
     * @param modal
     * @param peptideHit
     * @param progressDialog
     */
    public ProteinIsoFormSelection(java.awt.Frame parent, boolean modal, PeptideHit peptideHit,
            ProgressDialog progressDialog) {
        super(parent, modal);
        initComponents();

        this.peptideHit = peptideHit;
        outputDetails = (OutputDetails) parent;
        this.progressDialog = progressDialog;
        this.prideConverter = outputDetails.getPRIDEConverterReference();

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        peptideMassJTextField.setText("" + peptideHit.getPeptideMr());
        peptideSequenceJTextField.setText(peptideHit.getSequence());
        
        proteinIsoformsJTable = new JTable(new DefaultTableModel()) {

            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }

            public boolean isCellEditable(int row, int column) {
                if (column == 3) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        proteinIsoformsJTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {

                int index = proteinIsoformsJTable.columnAtPoint(evt.getPoint());

                if (index == 3) {
                    okJButton.setEnabled(true);
                }
            }
        });

        insertProteinIsoforms(peptideHit);

        proteinIsoformsTableHeader = proteinIsoformsJTable.getTableHeader();
        proteinIsoformsTableHeader.setReorderingAllowed(false);
        jScrollPane1.setViewportView(proteinIsoformsJTable);

        setLocationRelativeTo(outputDetails);
        setVisible(true);
    }

    /**
     * Retrieves and inserts information about the detected protein isoforms.
     * 
     * @param peptideHit
     */
    private void insertProteinIsoforms(PeptideHit peptideHit) {

        UIDefaults ui = UIManager.getLookAndFeel().getDefaults();
        UIManager.put("RadioButton.focus", ui.getColor("control"));

        proteinIsoformsTableModel = new DefaultTableModel();
        proteinIsoformsTableModel.setColumnIdentifiers(
                new Object[]{"Accession", "Start", "Stop", " "});
        buttonGroup = new ButtonGroup();

        JRadioButton tempJRadioButton;

        for (int i = 0; i < peptideHit.getProteinHits().size(); i++) {
            proteinIsoformsTableModel.addRow(new Object[]{
                ((ProteinHit) peptideHit.getProteinHits().get(i)).getAccession(),
                ((ProteinHit) peptideHit.getProteinHits().get(i)).getStart(),
                ((ProteinHit) peptideHit.getProteinHits().get(i)).getStop(),
                new JRadioButton()
            });

            buttonGroup.add((JRadioButton) proteinIsoformsTableModel.getValueAt(i, 3));
        }

        tempJRadioButton = new JRadioButton();
        tempJRadioButton.setSelected(true);
        proteinIsoformsTableModel.setValueAt(tempJRadioButton, 0, 3);
        buttonGroup.add((JRadioButton) proteinIsoformsTableModel.getValueAt(0, 3));

        proteinIsoformsJTable.setModel(proteinIsoformsTableModel);

        proteinIsoformsJTable.getColumn(" ").setCellRenderer(new RadioButtonRenderer());
        proteinIsoformsJTable.getColumn(" ").setCellEditor(new RadioButtonEditor(new JCheckBox()));
        proteinIsoformsJTable.getColumn(" ").setMaxWidth(20);
        proteinIsoformsJTable.getColumn(" ").setMinWidth(20);
        proteinIsoformsJTable.getColumn("Start").setMaxWidth(50);
        proteinIsoformsJTable.getColumn("Start").setMinWidth(50);
        proteinIsoformsJTable.getColumn("Stop").setMaxWidth(50);
        proteinIsoformsJTable.getColumn("Stop").setMinWidth(50);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        peptideMassJTextField = new javax.swing.JTextField();
        peptideSequenceJTextField = new javax.swing.JTextField();
        alwaysChooseIsoformJCheckBox = new javax.swing.JCheckBox();
        aboutJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Protein Isoforms Detected");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        okJButton.setText("OK");
        okJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Protein Isoforms", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabel1.setText("Protein isoforms detected. Please select the wanted isoform in the list.");

        jLabel2.setText("Peptide Mass:");

        jLabel3.setText("Peptide Sequence:");

        peptideMassJTextField.setEditable(false);
        peptideMassJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        peptideSequenceJTextField.setEditable(false);
        peptideSequenceJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        alwaysChooseIsoformJCheckBox.setSelected(true);
        alwaysChooseIsoformJCheckBox.setText("Always Choose The Selected Protein Isoform");
        alwaysChooseIsoformJCheckBox.setToolTipText("If selected the given peptide sequence will always be mapped to the selected protein isoform");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(peptideMassJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                            .addComponent(peptideSequenceJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)))
                    .addComponent(jLabel1)
                    .addComponent(alwaysChooseIsoformJCheckBox))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(peptideMassJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(peptideSequenceJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addGap(9, 9, 9)
                .addComponent(alwaysChooseIsoformJCheckBox))
        );

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aboutJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelJButton)
                            .addComponent(okJButton))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aboutJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog and cancels the conversion.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        prideConverter.setCancelConversion(true);
        progressDialog.setVisible(false);
        progressDialog.dispose();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Gives the information about the selected isoform to the main class 
     * (PRIDEConverter) and closes the dialog.
     * 
     * @param evt
     */
    private void okJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed
        this.setVisible(false);

        int selectedIndex = -1;

        for (int i = 0; i < proteinIsoformsJTable.getRowCount() &&
                selectedIndex == -1; i++) {
            if (((JRadioButton) proteinIsoformsJTable.getValueAt(i, 3)).isSelected()) {
                selectedIndex = i;
            }
        }

        if(selectedIndex == -1){
            selectedIndex = 0;
        }
        
        prideConverter.getProperties().setTempProteinHit((ProteinHit) peptideHit.getProteinHits().get(selectedIndex));

        if (alwaysChooseIsoformJCheckBox.isSelected()) {
            if (!prideConverter.getProperties().getSelectedProteinHits().contains(((ProteinHit) peptideHit.getProteinHits().get(selectedIndex)).getAccession())) {
                prideConverter.getProperties().getSelectedProteinHits().add(((ProteinHit) peptideHit.getProteinHits().get(selectedIndex)).getAccession());
            }
        }
        
        this.dispose();
    }//GEN-LAST:event_okJButtonActionPerformed

    /**
     * See cancelJButtonActionPerformed
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens an About PRIDE Converter dialog.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpWindow(this, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Opens a Help dialog.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpWindow(this, getClass().getResource("/no/uib/prideconverter/helpfiles/ProteinIsoformSelection.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JCheckBox alwaysChooseIsoformJCheckBox;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okJButton;
    private javax.swing.JTextField peptideMassJTextField;
    private javax.swing.JTextField peptideSequenceJTextField;
    // End of variables declaration//GEN-END:variables
}