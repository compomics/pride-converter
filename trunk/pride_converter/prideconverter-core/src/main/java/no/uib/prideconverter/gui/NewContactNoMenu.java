package no.uib.prideconverter.gui;

import java.awt.KeyboardFocusManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import no.uib.prideconverter.util.Util;
import uk.ac.ebi.pride.model.implementation.mzData.ContactImpl;

/**
 * A dialog for inserting information about a contact that is extracted
 * from a data file.
 *
 * @author  Harald Barsnes
 * 
 * Created Januar 2009
 */
public class NewContactNoMenu extends javax.swing.JDialog {

    private ContactInputable contactFrame;
    private int selectedRow = -1;
    private String contactPath;
    private String currentContactName;
    private boolean valuesChanged = false;

    /**
     * Opens a new NewContactNoMenu dialog
     * 
     * @param experimentProperties a reference to the ExperimentProperties frame
     * @param modal
     * @param selectedRow the row to edit, -1 if adding new row
     * @param name
     */
    public NewContactNoMenu(JFrame parent, ContactInputable contactFrame, boolean modal, int selectedRow,
            String name, String eMail, String institution) {
        super(parent, modal);
        this.contactFrame = contactFrame;

        this.selectedRow = selectedRow;

        contactNameJTextField.setText(name);
        contactInfoJTextField.setText(eMail);
        institutionJTextArea.setText(institution);

        setUpDialog();
 
        currentContactName = name;

        readContactsFromFile();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Opens a new NewContactNoMenu dialog
     *
     * @param experimentProperties a reference to the ExperimentProperties frame
     * @param modal
     * @param selectedRow the row to edit, -1 if adding new row
     * @param name
     */
    public NewContactNoMenu(JDialog parent, ContactInputable contactFrame, boolean modal, int selectedRow,
            String name, String eMail, String institution) {
        super(parent, modal);
        this.contactFrame = contactFrame;

        this.selectedRow = selectedRow;

        contactNameJTextField.setText(name);
        contactInfoJTextField.setText(eMail);
        institutionJTextArea.setText(institution);

        setUpDialog();

        currentContactName = name;

        readContactsFromFile();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Contains the code that is used by both constructors to keep from
     * having to duplicate the code.
     */
    private void setUpDialog(){

        initComponents();

        // alters the usage if TAB so that it can be used to move between fields
        KeyStroke ctrlTab = KeyStroke.getKeyStroke("ctrl TAB");
        KeyStroke tab = KeyStroke.getKeyStroke("TAB");
        Set set = new HashSet(institutionJTextArea.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.remove(ctrlTab);
        set.add(tab);
        institutionJTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
        institutionJTextArea.getInputMap().put(ctrlTab, "insert-tab");

        // only works for Java 1.6 and newer
        //        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
        //                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        institutionJTextArea.setFont(new java.awt.Font("Tahoma", 0, 11));
    }

    /**
     * Read all contacts from file.
     */
    public void readContactsFromFile() {

        contactPath = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
        contactPath = contactPath.substring(5, contactPath.lastIndexOf("/"));
        contactPath = contactPath.substring(0, contactPath.lastIndexOf("/") + 1) + "Properties/Contacts/";
        contactPath = contactPath.replace("%20", " ");

        File file = new File(contactPath);
        String tempContactName = null;

        try {

            if (!file.exists()) {
                file.mkdir();
            }

            File[] contactFiles = file.listFiles();
            FileReader f;
            BufferedReader b;

            Vector contactNames = new Vector();

            for (int i = 0; i < contactFiles.length; i++) {

                if (contactFiles[i].getAbsolutePath().endsWith(".con")) {

                    f = new FileReader(contactFiles[i]);
                    b = new BufferedReader(f);

                    tempContactName = b.readLine();
                    tempContactName = tempContactName.substring(tempContactName.indexOf(": ") + 2);
                    contactNames.add(tempContactName);

                    b.close();
                    f.close();
                }
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this, "The file " + tempContactName + " could not be found.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to read file: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this, "An error occured when trying to read the file " + tempContactName + ".",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to read file: ");
            ex.printStackTrace();
        }

        valuesChanged = false;
    }

    /**
     * Checks if all mandatory information is filled in. Enables or disables 
     * the "Add Contact" button.
     */
    public void mandatoryFieldsCheck() {
        if (contactNameJTextField.getText().length() > 0 &&
                contactInfoJTextField.getText().length() > 0 &&
                institutionJTextArea.getText().length() > 0) {
            addJButton.setEnabled(true);
        } else {
            addJButton.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        contactInfoJTextField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        institutionJTextArea = new javax.swing.JTextArea();
        contactNameJTextField = new javax.swing.JTextField();
        addJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("New Contact");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contact", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        jLabel6.setText("Institution:");

        jLabel7.setText("Name:");

        jLabel8.setText("E-mail:");

        contactInfoJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contactInfoJTextFieldKeyReleased(evt);
            }
        });

        jScrollPane2.setEnabled(false);

        institutionJTextArea.setColumns(20);
        institutionJTextArea.setLineWrap(true);
        institutionJTextArea.setRows(4);
        institutionJTextArea.setWrapStyleWord(true);
        institutionJTextArea.setBorder(null);
        institutionJTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                institutionJTextAreaKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(institutionJTextArea);

        contactNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contactNameJTextFieldKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jLabel7)
                    .add(jLabel6))
                .add(16, 16, 16)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(contactNameJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .add(contactInfoJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(contactNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(9, 9, 9)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(contactInfoJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(27, 27, 27)
                        .add(jLabel6))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        addJButton.setText("OK");
        addJButton.setEnabled(false);
        addJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJButtonActionPerformed(evt);
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
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 288, Short.MAX_VALUE)
                        .add(addJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 15, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton)
                            .add(addJButton))
                        .add(12, 12, 12))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Clicking the add button inserts the new contact into the contact 
     * table in the ExperimentProperies frame. Also writes the contact 
     * to file.
     * 
     * @param evt
     */
    private void addJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        boolean alreadyInTable = false;

        if (selectedRow == -1) {
            alreadyInTable = contactFrame.contactAlreadyInTable(contactNameJTextField.getText());
        }

        if (!alreadyInTable) {

            String tempInstitution = institutionJTextArea.getText();

            tempInstitution = tempInstitution.trim();

            tempInstitution = tempInstitution.replaceAll("\n", ", ");
            tempInstitution = tempInstitution.replaceAll(" , ", "");

            while (tempInstitution.lastIndexOf(",,") != -1) {
                tempInstitution = tempInstitution.replaceAll(",,", ",");
            }

            if (tempInstitution.endsWith(",")) {
                tempInstitution = tempInstitution.substring(0, tempInstitution.length() - 1);
            }

            String newName = contactPath + contactNameJTextField.getText() + ".con";

            try {
                FileWriter r = new FileWriter(newName);
                BufferedWriter bw = new BufferedWriter(r);

                bw.write("Name: " + contactNameJTextField.getText() + "\n");
                bw.write("E-mail: " + contactInfoJTextField.getText() + "\n");
                bw.write("Institution: " + tempInstitution + "\n");

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

            contactFrame.addContact(
                    new ContactImpl(
                    tempInstitution,
                    contactNameJTextField.getText(),
                    contactInfoJTextField.getText()),
                    selectedRow);

            this.setVisible(false);
            this.dispose();

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_addJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
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
     * Opens a Help frame.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/NewContactNoMenu.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * @see #mandatoryFieldsCheck()
     * 
     * @param evt
     */
    private void contactInfoJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactInfoJTextFieldKeyReleased
        valuesChanged = true;
        mandatoryFieldsCheck();
    }//GEN-LAST:event_contactInfoJTextFieldKeyReleased

    /**
     * @see #mandatoryFieldsCheck()
     * 
     * @param evt
     */
    private void institutionJTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_institutionJTextAreaKeyReleased
        valuesChanged = true;
        mandatoryFieldsCheck();
    }//GEN-LAST:event_institutionJTextAreaKeyReleased

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
     * @see #mandatoryFieldsCheck()
     *
     * @param evt
     */
    private void contactNameJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactNameJTextFieldKeyReleased
        valuesChanged = true;
        mandatoryFieldsCheck();
}//GEN-LAST:event_contactNameJTextFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton addJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextField contactInfoJTextField;
    private javax.swing.JTextField contactNameJTextField;
    private javax.swing.JButton helpJButton;
    private javax.swing.JTextArea institutionJTextArea;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    /**
     * @see ComboBoxInputable
     */
    public void insertIntoComboBox(String text) {

        this.currentContactName = text;

        String newName;
        newName = contactPath + currentContactName + ".con";

        try {
            FileWriter r = new FileWriter(newName);
            BufferedWriter bw = new BufferedWriter(r);

            bw.write("Name: " + currentContactName + "\n");
            bw.write("E-mail: " + "\n");
            bw.write("Institution: " + "\n");

            bw.close();
            r.close();

            institutionJTextArea.setEnabled(true);
            contactInfoJTextField.setEnabled(true);
            institutionJTextArea.setEditable(true);
            contactInfoJTextField.setEditable(true);

            readContactsFromFile();

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
    public boolean alreadyInserted(String currentContactName) {
        this.currentContactName = currentContactName;

        String newName;
        newName = contactPath + currentContactName + ".con";
        File newFile = new File(newName);

        return newFile.exists();
    }
}
