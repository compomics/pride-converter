package no.uib.prideconverter.gui;

import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import no.uib.prideconverter.gui.ComboBoxInputDialog;
import no.uib.prideconverter.util.ComboBoxInputable;
import uk.ac.ebi.pride.model.implementation.mzData.ContactImpl;

/**
 * A dialog for inserting information about a contact.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class NewContact extends javax.swing.JDialog implements ComboBoxInputable {

    private ExperimentProperties experimentPropertiesFrame;
    private int selectedRow = -1;
    private String contactPath;
    private String currentContactName;
    private String lastSelectedContact;
    private boolean valuesChanged = false;

    /**
     * Opens a new NewContact dialog
     * 
     * @param experimentProperties a reference to the ExperimentProperties frame
     * @param modal
     */
    public NewContact(ExperimentProperties experimentProperties, boolean modal) {
        super(experimentProperties, modal);
        this.experimentPropertiesFrame = experimentProperties;
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

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        institutionJTextArea.setFont(new java.awt.Font("Tahoma", 0, 11));

        currentContactName = null;
        readContactsFromFile();

        setLocationRelativeTo(experimentPropertiesFrame);
        setVisible(true);
    }

    /**
     * Opens a new NewContact dialog
     * 
     * @param experimentProperties a reference to the ExperimentProperties frame
     * @param modal
     * @param selectedRow the row to edit, -1 if adding new row
     * @param name
     */
    public NewContact(ExperimentProperties experimentProperties, boolean modal,
            int selectedRow, String name) {
        super(experimentProperties, modal);
        this.experimentPropertiesFrame = experimentProperties;
        this.selectedRow = selectedRow;
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

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        institutionJTextArea.setFont(new java.awt.Font("Tahoma", 0, 11));

        currentContactName = name;

        readContactsFromFile();

        setLocationRelativeTo(experimentPropertiesFrame);
        setVisible(true);
    }

    /**
     * Read all contacts from file and insert the contact names into the 
     * names combo box. Then inserts the information about the selected 
     * contact.
     */
    public void readContactsFromFile() {

        if (experimentPropertiesFrame.getPRIDEConverter_ref().useHardcodedPaths()) {
            contactPath = "D:/PRIDE_ms_lims/ms_lims_to_PRIDE/PRIDEConverter/Release/Properties/Contacts/";
        } else {
            contactPath = "" +
                    this.getClass().getProtectionDomain().getCodeSource().getLocation();
            contactPath = contactPath.substring(5, contactPath.lastIndexOf("/"));
            contactPath = contactPath.substring(0, contactPath.lastIndexOf("/") +
                    1) + "Properties/Contacts/";
            contactPath = contactPath.replace("%20", " ");
        }

        File file = new File(contactPath);

        try {

            if (!file.exists()) {
                file.mkdir();
            }

            File[] contactFiles = file.listFiles();
            FileReader f;
            BufferedReader b;
            String tempContactName;
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

            java.util.Collections.sort(contactNames);

            contactNames.add("   Create a new contact...");
            contactNames.insertElementAt("- Please select a contact -", 0);
            namesJComboBox.setModel(new DefaultComboBoxModel(contactNames));

            lastSelectedContact = "" + namesJComboBox.getSelectedItem();

            if (currentContactName != null) {
                namesJComboBox.setSelectedItem(currentContactName);
            }

            namesJComboBoxActionPerformed(null);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        valuesChanged = false;
    }

    /**
     * Checks if all mandatory information is filled in. Enables or disables 
     * the "Add Contact" button.
     */
    public void mandatoryFieldsCheck() {
        if ((namesJComboBox.getSelectedIndex() != 0 &&
                namesJComboBox.getSelectedIndex() !=
                namesJComboBox.getModel().getSize() - 1) &&
                contactInfoJTextField.getText().length() > 0 &&
                institutionJTextArea.getText().length() > 0) {
            addJButton.setEnabled(true);
        } else {
            addJButton.setEnabled(false);
        }

        if (namesJComboBox.getSelectedIndex() != 0 &&
                namesJComboBox.getSelectedIndex() !=
                namesJComboBox.getModel().getSize() - 1 &&
                namesJComboBox.getSelectedIndex() != -1) {
            deleteJButton.setEnabled(true);
        } else {
            deleteJButton.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        deleteJMenuItem = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        contactInfoJTextField = new javax.swing.JTextField();
        namesJComboBox = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        institutionJTextArea = new javax.swing.JTextArea();
        deleteJButton = new javax.swing.JButton();
        addJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();

        deleteJMenuItem.setText("Delete Contact");
        deleteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(deleteJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("New Contact");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contact", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        jLabel6.setText("Institution:");

        jLabel7.setText("Name:");

        jLabel8.setText("E-mail:");

        contactInfoJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contactInfoJTextFieldKeyReleased(evt);
            }
        });

        namesJComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        namesJComboBox.setMinimumSize(new java.awt.Dimension(23, 20));
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

        deleteJButton.setFont(new java.awt.Font("Arial", 1, 11));
        deleteJButton.setText("X");
        deleteJButton.setToolTipText("Delete Selected Contact");
        deleteJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteJButton.setIconTextGap(0);
        deleteJButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        deleteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addComponent(contactInfoJTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(namesJComboBox, 0, 397, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteJButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(namesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteJButton))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(contactInfoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aboutJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 288, Short.MAX_VALUE)
                        .addComponent(addJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelJButton)
                            .addComponent(addJButton))
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aboutJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            alreadyInTable = experimentPropertiesFrame.contactAlreadyInTable((String) namesJComboBox.getSelectedItem());
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
                tempInstitution = tempInstitution.substring(0, tempInstitution.length() -
                        1);
            }

            try {
                String newName = contactPath + currentContactName + ".con";

                FileWriter r = new FileWriter(newName);
                BufferedWriter bw = new BufferedWriter(r);

                bw.write("Name: " + currentContactName + "\n");
                bw.write("E-mail: " + contactInfoJTextField.getText() + "\n");
                bw.write("Institution: " + tempInstitution + "\n");

                bw.close();
                r.close();

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            experimentPropertiesFrame.addContact(
                    new ContactImpl(
                    tempInstitution,
                    (String) namesJComboBox.getSelectedItem(),
                    contactInfoJTextField.getText()),
                    selectedRow);

            this.setVisible(false);
            this.dispose();

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_addJButtonActionPerformed

    /**
     * See cancelJButtonActionPerformed
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
        new HelpWindow(this, getClass().getResource("/no/uib/prideconverter/helpfiles/NewContact.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * See mandatoryFieldsCheck
     * 
     * @param evt
     */
    private void contactInfoJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactInfoJTextFieldKeyReleased
        valuesChanged = true;

        mandatoryFieldsCheck();
    }//GEN-LAST:event_contactInfoJTextFieldKeyReleased

    /**
     * Updates the contact information shown, or creates a new contact if 
     * the first row is selected.
     * 
     * @param evt
     */
    private void namesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namesJComboBoxActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        String selectedContactName = (String) namesJComboBox.getSelectedItem();

        boolean cancel = false;

        if (valuesChanged) {
            int value = JOptionPane.showConfirmDialog(this,
                    "The contact has been changed. Do you want to save this for later use?",
                    "Contact Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {
                try {
                    String newName = contactPath + lastSelectedContact + ".con";

                    FileWriter r = new FileWriter(newName);
                    BufferedWriter bw = new BufferedWriter(r);

                    bw.write("Name: " + lastSelectedContact + "\n");
                    bw.write("E-mail: " + contactInfoJTextField.getText() + "\n");
                    bw.write("Institution: " + institutionJTextArea.getText() +
                            "\n");

                    bw.close();
                    r.close();

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
            }
        }

        if (!cancel) {

            lastSelectedContact = "" + namesJComboBox.getSelectedItem();

            contactInfoJTextField.setText("");
            institutionJTextArea.setText("");

            if (namesJComboBox.getSelectedIndex() == 0) {

                institutionJTextArea.setEnabled(false);
                contactInfoJTextField.setEnabled(false);
                institutionJTextArea.setEditable(false);
                contactInfoJTextField.setEditable(false);

            } else if (namesJComboBox.getSelectedIndex() ==
                    namesJComboBox.getItemCount() - 1) {

                ComboBoxInputDialog input = new ComboBoxInputDialog(this, this, true);
                input.setTitle("Create New Contact");
                input.setBorderTitle("New Contact");
                input.setVisible(true);

            } else {

                institutionJTextArea.setEnabled(true);
                contactInfoJTextField.setEnabled(true);
                institutionJTextArea.setEditable(true);
                contactInfoJTextField.setEditable(true);

                selectedContactName = contactPath + selectedContactName + ".con";

                try {
                    String temp, institution = "";

                    FileReader f = new FileReader(selectedContactName);
                    BufferedReader b = new BufferedReader(f);

                    b.readLine();
                    temp = b.readLine();
                    contactInfoJTextField.setText(temp.substring(temp.indexOf(": ") + 2));

                    temp = b.readLine();

                    while (temp != null) {
                        if (temp.indexOf(": ") != -1) {
                            institution += temp.substring(temp.indexOf(": ") + 2) + ", ";
                        } else {
                            institution += temp + ", ";
                        }

                        temp = b.readLine();
                    }

                    if (institution.endsWith(", ")) {
                        institution = institution.substring(0, institution.length() -
                                2);
                    }


                    institutionJTextArea.setText(institution);

                    institutionJTextArea.setEnabled(true);
                    contactInfoJTextField.setEnabled(true);
                    institutionJTextArea.setEditable(true);
                    contactInfoJTextField.setEditable(true);


                    b.close();
                    f.close();

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            valuesChanged = false;
        } else {
            namesJComboBox.setSelectedItem(lastSelectedContact);
        }

        mandatoryFieldsCheck();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_namesJComboBoxActionPerformed

    /**
     * See mandatoryFieldsCheck
     * 
     * @param evt
     */
    private void institutionJTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_institutionJTextAreaKeyReleased
        valuesChanged = true;

        mandatoryFieldsCheck();
    }//GEN-LAST:event_institutionJTextAreaKeyReleased

    /**
     * Sets the current contact name to the one selected in the combo box
     * 
     * @param evt
     */
    private void namesJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_namesJComboBoxItemStateChanged
        currentContactName = (String) namesJComboBox.getSelectedItem();
    }//GEN-LAST:event_namesJComboBoxItemStateChanged

    /**
     * Delete the selected contact.
     * 
     * @param evt
     */
    private void deleteJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int option = JOptionPane.showConfirmDialog(this, "This will delete the selected contact. Continue?");

        if (option == JOptionPane.YES_OPTION) {
            currentContactName = (String) namesJComboBox.getSelectedItem();

            String newName = contactPath + currentContactName + ".con";

            boolean deleted = new File(newName).delete();

            if (!deleted) {
                JOptionPane.showMessageDialog(this, "The file could not be deleted!");
            } else {
                currentContactName = null;
                readContactsFromFile();
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_deleteJMenuItemActionPerformed

    /**
     * Right clicking in the combo box opens a popup menu where one can 
     * delete the selected contact.
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
     * Deletes the currently selected contact.
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton addJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextField contactInfoJTextField;
    private javax.swing.JButton deleteJButton;
    private javax.swing.JMenuItem deleteJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JTextArea institutionJTextArea;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox namesJComboBox;
    private javax.swing.JPopupMenu popupMenu;
    // End of variables declaration//GEN-END:variables
    /**
     * See ComboBoxInputable
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
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * See ComboBoxInputable
     */
    public void resetComboBox() {

        currentContactName = null;
        namesJComboBox.setSelectedItem(null);
        namesJComboBox.setSelectedIndex(0);
        institutionJTextArea.setEnabled(false);
        contactInfoJTextField.setEnabled(false);
        institutionJTextArea.setEditable(false);
        contactInfoJTextField.setEditable(false);
        namesJComboBoxActionPerformed(null);
    }

    /**
     * See ComboBoxInputable
     */
    public boolean alreadyInserted(String currentContactName) {
        this.currentContactName = currentContactName;

        String newName;
        newName = contactPath + currentContactName + ".con";
        File newFile = new File(newName);

        return newFile.exists();
    }
}
