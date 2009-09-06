package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import be.proteomics.lims.db.accessors.Project;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.event.TableModelListener;
import no.uib.prideconverter.util.Util;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;

/**
 * A frame that lets the user select one or more ms_lims projects to convert 
 * into PRIDE XML.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class ProjectSelection extends javax.swing.JFrame {

    private static JXTable projectJXTable;
    private JXTableHeader projectJXTableHeader;
    private DefaultTableModel projectModel = null;
    private Project[] projects;
    private ProgressDialog progressDialog;
    private Vector columnToolTips;

    /**
     * Opens a new ProjectSelection frame.
     * 
     * @param location where to locate the frame on the screen
     */
    public ProjectSelection(Point location) {

        // sets the default wizard frame size
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

        projectDescriptionJTextArea.setFont(new java.awt.Font("Tahoma", 0, 11));

        setTitle(PRIDEConverter.getWizardName() + " " +
                PRIDEConverter.getPrideConverterVersionNumber() + " - " + getTitle());

        columnToolTips = new Vector();
        columnToolTips.add("Project ID");
        columnToolTips.add("Project Title");
        columnToolTips.add("Creation Date of Project");
        columnToolTips.add(null);

        projectJXTable = new JXTable(new DefaultTableModel()) {

            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 3);
            }

            @Override
            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return java.lang.Integer.class;
                }
                if (columnIndex == 1 || columnIndex == 2) {
                    return java.lang.String.class;
                } else {
                    //return java.sql.Timestamp.class;
                    return java.lang.Boolean.class;
                }
            }

            @Override
            protected JXTableHeader createDefaultTableHeader() {
                return new JXTableHeader(columnModel) {

                    @Override
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex =
                                columnModel.getColumn(index).getModelIndex();
                        return (String) columnToolTips.get(realIndex);
                    }
                };
            }
        };

        projectJXTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        projectJXTable.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                int row = projectJXTable.getSelectedRow();

                if (row != -1) {

                    for (int i = 0; i < projects.length; i++) {
                        if (projects[i].getTitle().equalsIgnoreCase((String) projectJXTable.getValueAt(row, 1))) {
                            projectDescriptionJTextArea.setText(projects[i].getDescription());
                            projectDescriptionJTextArea.setCaretPosition(0);
                        }
                    }
                }

                boolean projectsSelected = false;

                for (int i = 0; i < projectJXTable.getRowCount() &&
                        !projectsSelected; i++) {

                    if (((Boolean) projectJXTable.getValueAt(i, 3)).booleanValue()) {
                        projectsSelected = true;
                    }
                }

                nextJButton.setEnabled(projectsSelected);
            }
        });

        projectJXTable.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                projectJXTableKeyReleased(evt);
            }
        });

        projectJXTableHeader = (JXTableHeader) projectJXTable.getTableHeader();
        projectJXTableHeader.setReorderingAllowed(false);

        projectJXTableHeader.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                projectJXTableHeaderMouseExited(evt);
            }
        });

        projectJXTableHeader.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                projectJXTableHeaderMouseMoved(evt);
            }
        });

        projectModel = new DefaultTableModel();
        projectModel.setColumnIdentifiers(new Object[]{
            "PID", "Title", "Creation Date", " "
        });

        projectModel.addTableModelListener(new MyTableModelListener());

        jScrollPane1.setViewportView(projectJXTable);


        if (location != null) {
            setLocation(location);
        } else {
            setLocationRelativeTo(null);
        }

        insertProjects();
        setVisible(true);
    }

    /**
     * Updates the project description based on which project is selected.
     * 
     * @param evt
     */
    private void projectJXTableKeyReleased(java.awt.event.KeyEvent evt) {

        int row = projectJXTable.getSelectedRow();

        for (int i = 0; i < projects.length; i++) {
            if (row != -1) {
                if (projects[i].getTitle().equalsIgnoreCase((String) projectJXTable.getValueAt(row, 1))) {
                    projectDescriptionJTextArea.setText(projects[i].getDescription());
                    projectDescriptionJTextArea.setCaretPosition(0);
                }
            }
        }

        boolean projectsSelected = false;

        for (int i = 0; i < projectJXTable.getRowCount() && !projectsSelected; i++) {

            if (((Boolean) projectJXTable.getValueAt(i, 3)).booleanValue()) {
                projectsSelected = true;
            }
        }

        nextJButton.setEnabled(projectsSelected);
    }

    /**
     * Makes sure the cursor changes back to the default
     * cursor when exiting the column headers.
     *
     * @param evt
     */
    private void projectJXTableHeaderMouseExited(java.awt.event.MouseEvent evt) {
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Makes sure the cursor changes to a resize cursor
     * when the mouse pointer is located between to
     * columns in the table headers.
     *
     * @param evt
     */
    private void projectJXTableHeaderMouseMoved(java.awt.event.MouseEvent evt) {

        java.awt.Rectangle rectangle = projectJXTableHeader.getHeaderRect(
                projectJXTable.columnAtPoint(evt.getPoint()));

        if (((evt.getX() < rectangle.getMinX() + 2) &&
                (projectJXTable.columnAtPoint(evt.getPoint()) != 0)) || ((evt.getX() > rectangle.getMaxX() - 4)
                && (projectJXTable.columnAtPoint(evt.getPoint()) !=
                projectJXTable.getColumnCount() - 1))) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));
        } else {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Retrieves the project files and inserts them in the table.
     */
    public void insertProjects() {

        progressDialog = new ProgressDialog(this, true);
        progressDialog.setTitle("Retrieving Project Data. Please Wait...");
        progressDialog.setIntermidiate(true);
        progressDialog.doNothingOnClose();

        Thread t = new Thread(new Runnable() {

            public void run() {
                progressDialog.setVisible(true);
            }
        });

        t.start();

        // Wait until progress dialog is visible.
        //
        // The following is not needed in Java 1.6, but seemed to be needed in 1.5.
        //
        // Not including the lines _used to_ result in a crash on Windows, but not anymore.
        // Including the lines results in a crash on Linux and Mac.
        if(System.getProperty("os.name").toLowerCase().lastIndexOf("windows") != -1){
            while (!progressDialog.isVisible()) {
            }
        }

        Thread t2 = new Thread(new Runnable() {

            public void run() {

                try {
                    projects = Project.getAllProjects(PRIDEConverter.getConn());

                    //sort projects on project id
                    int min;

                    for (int i = 0; i < projects.length - 1; i++) {

                        min = i;

                        for (int j = i + 1; j < projects.length; j++) {
                            if (projects[min].getProjectid() > projects[j].getProjectid()) {
                                min = j;
                            }
                        }

                        Project tempProject = projects[min];
                        projects[min] = projects[i];
                        projects[i] = tempProject;
                    }

                    boolean projectsSelected = false;

                    for (int i = 0; i < projects.length; i++) {

                        if (PRIDEConverter.getProperties().getProjectIds().contains(
                                new Long(projects[i].getProjectid()))) {
                            projectsSelected = true;
                        }

                        projectModel.addRow(new Object[]{
                            projects[i].getProjectid(),
                            projects[i].getTitle(),
                            projects[i].getCreationdate(),
                            new Boolean(
                            PRIDEConverter.getProperties().getProjectIds().contains(
                                    new Long(projects[i].getProjectid())))
                        });
                    }

                    projectJXTable.setModel(projectModel);

                    if (projectsSelected) {
                        nextJButton.setEnabled(true);
                    }

                    projectJXTable.setOpaque(false);

                    projectJXTable.getColumn("Creation Date").setMaxWidth(135);
                    projectJXTable.getColumn("Creation Date").setMinWidth(135);
                    projectJXTable.getColumn("PID").setMaxWidth(52);
                    projectJXTable.getColumn("PID").setMinWidth(52);
                    projectJXTable.getColumn(" ").setMaxWidth(40);
                    projectJXTable.getColumn(" ").setMinWidth(40);

                    progressDialog.setVisible(false);
                    progressDialog.dispose();

                    projectJXTable.requestFocus();

                } catch (SQLException sqle) {
                    progressDialog.setVisible(false);
                    progressDialog.dispose();

                    JOptionPane.showMessageDialog(null, 
                            "An error occured while retrieving project details.\n" +
                            "See ../Properties/ErrorLog.txt for more detals");
                    Util.writeToErrorLog("Error retrieving project details: ");
                    sqle.printStackTrace();
                }
            }
        });

        t2.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        projectDescriptionJTextArea = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Project Selection - Step 1 of 8");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        nextJButton.setText("Next  >");
        nextJButton.setEnabled(false);
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

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Project Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Project Description", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        projectDescriptionJTextArea.setColumns(20);
        projectDescriptionJTextArea.setEditable(false);
        projectDescriptionJTextArea.setLineWrap(true);
        projectDescriptionJTextArea.setRows(5);
        projectDescriptionJTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(projectDescriptionJTextArea);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabel3.setText("Select the ms_lims project(s) to convert to PRIDE XML, and click on 'Next' to continue.");

        jSeparator1.setMaximumSize(new java.awt.Dimension(560, 10));
        jSeparator1.setMinimumSize(new java.awt.Dimension(560, 10));
        jSeparator1.setPreferredSize(new java.awt.Dimension(560, 10));

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
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 239, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .add(jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
     * Stores the selected project id and returns to the previous frame 
     * (DataBaseDetails).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        saveInsertedInformation();
        new DataBaseDetails(this.getLocation());
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_backJButtonActionPerformed

    /**
     * Closes the frame, and the wizard.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
        PRIDEConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Stores the selected project id and opens the next frame (SpectraSelection_ms_lims).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        saveInsertedInformation();
        new SpectraSelectionWithIdentifications(this.getLocation());
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * See cancelJButtonActionPerformed
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
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/ProjectSelection.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

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
     * Save inserted information to the Properties object.
     */
    private void saveInsertedInformation(){
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        ArrayList oldProjectIds = PRIDEConverter.getProperties().getProjectIds();

        PRIDEConverter.getProperties().setProjectIds(new ArrayList());

        for (int i = 0; i < projectJXTable.getRowCount(); i++) {
            if (((Boolean) projectJXTable.getValueAt(i, 3)).booleanValue()) {
                PRIDEConverter.getProperties().getProjectIds().add((Long) projectJXTable.getValueAt(i, 0));
            }
        }

        boolean projectSelectionHasChanged = false;

        if (PRIDEConverter.getProperties().getProjectIds().size() != oldProjectIds.size()) {
            projectSelectionHasChanged = true;
        }

        for (int i = 0; i < PRIDEConverter.getProperties().getProjectIds().size() &&
                !projectSelectionHasChanged; i++) {
            if (!oldProjectIds.contains(PRIDEConverter.getProperties().getProjectIds().get(i))) {
                projectSelectionHasChanged = true;
            }
        }

        for (int i = 0; i < oldProjectIds.size() && !projectSelectionHasChanged; i++) {
            if (!PRIDEConverter.getProperties().getProjectIds().contains(oldProjectIds.get(i))) {
                projectSelectionHasChanged = true;
            }
        }

        if (projectSelectionHasChanged) {
            PRIDEConverter.getProperties().setSpectrumTableModel(null);
            PRIDEConverter.getProperties().setSpectraSelectionCriteria(null);
        }
        
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton nextJButton;
    private javax.swing.JTextArea projectDescriptionJTextArea;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Test method is fired if the content of the table changes. Used to 
     * detect project selection using the space bar.
     */
    private class MyTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            projectJXTableKeyReleased(null);
        }
    }
}
