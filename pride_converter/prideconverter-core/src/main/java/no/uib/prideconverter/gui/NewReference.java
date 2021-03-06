package no.uib.prideconverter.gui;

import java.awt.KeyboardFocusManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.KeyStroke;
import no.uib.prideconverter.util.BareBonesBrowserLaunch;
import uk.ac.ebi.pride.model.implementation.core.ReferenceImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;

/**
 * A dialog to insert information about a reference.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class NewReference extends javax.swing.JDialog {

    private ExperimentProperties experimentProperties;
    private int selectedRow = -1;

    /**
     * Opens a new NewReference dialog.
     * 
     * @param experimentProperties a reference to the ExperimentProperties frame
     * @param modal
     */
    public NewReference(ExperimentProperties experimentProperties, boolean modal) {
        super(experimentProperties, modal);
        this.experimentProperties = experimentProperties;
        initComponents();

        // alters the usage if TAB so that it can be used to move between fields
        KeyStroke ctrlTab = KeyStroke.getKeyStroke("ctrl TAB");
        KeyStroke tab = KeyStroke.getKeyStroke("TAB");
        Set set = new HashSet(referenceJTextArea.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.remove(ctrlTab);
        set.add(tab);
        referenceJTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
        referenceJTextArea.getInputMap().put(ctrlTab, "insert-tab");

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        setLocationRelativeTo(experimentProperties);
        setVisible(true);
    }

    /**
     * Opens a new NewReference dialog.
     * 
     * @param experimentProperties a reference to the ExperimentProperties frame
     * @param modal
     * @param selectedRow the row to edit, -1 if adding new row
     * @param currentRef
     */
    public NewReference(ExperimentProperties experimentProperties, boolean modal,
            int selectedRow, ReferenceImpl currentRef) {
        super(experimentProperties, modal);
        this.selectedRow = selectedRow;
        this.experimentProperties = experimentProperties;
        initComponents();

        // alters the usage if TAB so that it can be used to move between fields
        KeyStroke ctrlTab = KeyStroke.getKeyStroke("ctrl TAB");
        KeyStroke tab = KeyStroke.getKeyStroke("TAB");
        Set set = new HashSet(referenceJTextArea.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.remove(ctrlTab);
        set.add(tab);
        referenceJTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
        referenceJTextArea.getInputMap().put(ctrlTab, "insert-tab");

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        referenceJTextArea.setText(currentRef.getReferenceLine());

        if (currentRef.getReferenceCvParameterList() != null) {

            Iterator ids = currentRef.getReferenceCvParameterList().iterator();

            CvParamImpl temp;

            if (ids.hasNext()) {

                temp = (CvParamImpl) ids.next();

                if (temp.getCVLookup().equalsIgnoreCase("PubMed")) {
                    pmidIDJTextField.setText(temp.getName());
                } else if (temp.getCVLookup().equalsIgnoreCase("DOI")) {
                    doiJTextField.setText(temp.getName());
                }
            }

            if (ids.hasNext()) {

                temp = (CvParamImpl) ids.next();

                if (temp.getCVLookup().equalsIgnoreCase("PubMed")) {
                    pmidIDJTextField.setText(temp.getName());
                } else if (temp.getCVLookup().equalsIgnoreCase("DOI")) {
                    doiJTextField.setText(temp.getName());
                }
            }
        }
        setLocationRelativeTo(experimentProperties);
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        referenceJTextArea = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        doiJTextField = new javax.swing.JTextField();
        pmidIDJTextField = new javax.swing.JTextField();
        pubMedJButton = new javax.swing.JButton();
        insertRefJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Reference");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Reference"));

        jLabel1.setText("Reference:");

        referenceJTextArea.setColumns(20);
        referenceJTextArea.setFont(referenceJTextArea.getFont());
        referenceJTextArea.setLineWrap(true);
        referenceJTextArea.setRows(3);
        referenceJTextArea.setWrapStyleWord(true);
        referenceJTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                referenceJTextAreaKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(referenceJTextArea);

        jLabel2.setText("PMID:");
        jLabel2.setToolTipText("PubMed ID");

        jLabel3.setText("DOI:");
        jLabel3.setToolTipText("Digital Object Identifier");

        doiJTextField.setToolTipText("Digital Object Identifier");
        doiJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                doiJTextFieldKeyReleased(evt);
            }
        });

        pmidIDJTextField.setToolTipText("PubMed ID");
        pmidIDJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pmidIDJTextFieldKeyReleased(evt);
            }
        });

        pubMedJButton.setBackground(new java.awt.Color(255, 255, 255));
        pubMedJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/pubmed_2.JPG"))); // NOI18N
        pubMedJButton.setToolTipText("Go to www.pubmed.gov");
        pubMedJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pubMedJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pubMedJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pubMedJButtonMouseExited(evt);
            }
        });
        pubMedJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pubMedJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabel1))
                .add(10, 10, 10)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(doiJTextField)
                            .add(pmidIDJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pubMedJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(pmidIDJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(7, 7, 7)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel3)
                            .add(doiJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(pubMedJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        insertRefJButton.setText("OK");
        insertRefJButton.setEnabled(false);
        insertRefJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertRefJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(14, 14, 14))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 245, Short.MAX_VALUE)
                        .add(insertRefJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(cancelJButton)
                        .add(insertRefJButton))
                    .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Insert the reference into the ExperimentProperties frame.
     * 
     * @param evt
     */
    private void insertRefJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertRefJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        String tempRef = referenceJTextArea.getText();
        tempRef = tempRef.trim();
        tempRef = tempRef.replaceAll("\n", ", ");
        tempRef = tempRef.replaceAll(" , ", "");

        while (tempRef.lastIndexOf(",,") != -1) {
            tempRef = tempRef.replaceAll(",,", ",");
        }

        if (tempRef.endsWith(",")) {
            tempRef = tempRef.substring(0, tempRef.length() - 1);
        }

        experimentProperties.addReference(tempRef,
                this.pmidIDJTextField.getText(),
                this.doiJTextField.getText(),
                selectedRow);

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        formWindowClosing(null);
    }//GEN-LAST:event_insertRefJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    /**
     * Closes the dialog.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Opens a help frame.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/NewReference.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Opens the PubMed web page in web browser.
     * 
     * @param evt
     */
    private void pubMedJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pubMedJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.pubmed.gov");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_pubMedJButtonActionPerformed

    /**
     * Enables or disabled the Insert button if there is text in the 
     * reference field.
     * 
     * @param evt
     */
    private void referenceJTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_referenceJTextAreaKeyReleased
        if (referenceJTextArea.getText().length() > 0) {
            insertRefJButton.setEnabled(true);
        } else {
            insertRefJButton.setEnabled(false);
        }
    }//GEN-LAST:event_referenceJTextAreaKeyReleased

    /**
     * @see #referenceJTextAreaKeyReleased(java.awt.event.KeyEvent)
     */
    private void pmidIDJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pmidIDJTextFieldKeyReleased
        referenceJTextAreaKeyReleased(null);
    }//GEN-LAST:event_pmidIDJTextFieldKeyReleased

    /**
     * @see #referenceJTextAreaKeyReleased(java.awt.event.KeyEvent)
     */
    private void doiJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_doiJTextFieldKeyReleased
        referenceJTextAreaKeyReleased(null);
    }//GEN-LAST:event_doiJTextFieldKeyReleased

    /**
     * Opens an About PRIDE Converter dialog.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Changes the cursor to a hand cursor if the user hovers over the PubMed link
     * 
     * @param evt
     */
    private void pubMedJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pubMedJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_pubMedJButtonMouseEntered
 
    /**
     * Changes the cursor back to the default cursor when the user leaves the PubMed link
     * 
     * @param evt
     */
    private void pubMedJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pubMedJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_pubMedJButtonMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextField doiJTextField;
    private javax.swing.JButton helpJButton;
    private javax.swing.JButton insertRefJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField pmidIDJTextField;
    private javax.swing.JButton pubMedJButton;
    private javax.swing.JTextArea referenceJTextArea;
    // End of variables declaration//GEN-END:variables
}
