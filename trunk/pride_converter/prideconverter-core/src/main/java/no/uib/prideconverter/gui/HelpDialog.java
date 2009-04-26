package no.uib.prideconverter.gui;

import java.awt.Container;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import no.uib.prideconverter.util.BareBonesBrowserLaunch;

/**
 * A dialog used to display help text in html format.
 *
 * @author Harald Barsnes
 *
 * Created April 2009
 */
public class HelpDialog extends javax.swing.JDialog {

    /**
     * Creates a new HelpDialog with a Frame as a parent.
     *
     * @param parent
     * @param fileName the name of the help file
     */
    public HelpDialog(java.awt.Frame parent, boolean modal, URL fileName) {
        super(parent, modal);

        //this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        initComponents();

        // only works in Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/help.GIF")));

        boolean aboutFile = false;

        try {
            InputStream stream = fileName.openStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader b = new BufferedReader(streamReader);
            String s = b.readLine();
            String helpText = "";

            while (s != null) {
                helpText += s;
                s = b.readLine();
            }

            textJEditorPane.setText(helpText);

            if (fileName.getPath().endsWith("AboutPRIDE_Converter.html") ||
                    fileName.getPath().endsWith("AboutOLS.html")) {
                aboutFile = true;

                // only works in Java 1.6 and newer
//                if (fileName.getPath().endsWith("AboutPRIDE_Converter.html")) {
//                    setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                            getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));
//                } else {
//                    setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                            getResource("/no/uib/prideconverter/icons/ols_transparent_small.GIF")));
//                }
            }
        } catch (Exception e) {

            try {
                textJEditorPane.setPage(getClass().getResource(
                        "/no/uib/prideconverter/helpfiles/DefaultHelpFile.html"));
            } catch (Exception ex) {
                textJEditorPane.setText("The selected help file is not yet available.");
            }
        }

        textJEditorPane.setCaretPosition(0);

        if (!aboutFile) {

            // positions the Help frame to the right of the main frame
            // moves the main frame to the left if needed
            this.setSize(this.getWidth(), parent.getHeight());

            int xCoordinate = (int) parent.getLocation().getX() +
                    parent.getWidth() + 2;

            int movedDistance = 0;

            while (xCoordinate + this.getWidth() >=
                    Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
                xCoordinate -= 2;
                movedDistance -= 2;
            }

            if (movedDistance != 0) {
                parent.setLocation((int) parent.getLocation().getX() +
                        movedDistance,
                        (int) parent.getLocation().getY());
            }

            setLocation(xCoordinate,
                    (int) parent.getLocation().getY());
        } else {

            setTitle("About");
            setSize(400, 400);
            setLocationRelativeTo(parent);
        }

        setVisible(true);
    }

    /**
     * Creates a new HelpDialog with a JDialog as a parent.
     *
     * @param parent
     * @param fileName the name of the help file
     */
    public HelpDialog(javax.swing.JDialog parent, boolean modal, URL fileName) {
        super(parent, modal);

        //this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);

        initComponents();

        // only works in Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        if (fileName.getPath().endsWith("OLSDialog.html")) {
            setTitle("OLS - Help");

            // only works in Java 1.6 and newer
//            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                    getResource("/no/uib/prideconverter/icons/ols_transparent_small.GIF")));
        }

        boolean aboutFile = false;

        try {
            InputStream stream = fileName.openStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader b = new BufferedReader(streamReader);
            String s = b.readLine();
            String helpText = "";

            while (s != null) {
                helpText += s;
                s = b.readLine();
            }

            textJEditorPane.setText(helpText);

            if (fileName.getPath().endsWith("AboutPRIDE_Converter.html") ||
                    fileName.getPath().endsWith("AboutOLS.html")) {
                aboutFile = true;

                // only works in Java 1.6 and newer
//                if (fileName.getPath().endsWith("AboutPRIDE_Converter.html")) {
//                    setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                            getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));
//                } else {
//                    setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                            getResource("/no/uib/prideconverter/icons/ols_transparent_small.GIF")));
//                }
            }
        } catch (Exception e) {
            try {
                textJEditorPane.setPage(getClass().getResource(
                        "/no/uib/prideconverter/helpfiles/DefaultHelpFile.html"));
            } catch (Exception ex) {
                textJEditorPane.setText("The selected help file is not yet available.");
            }
        }

        textJEditorPane.setCaretPosition(0);

        if (!aboutFile) {

            // positions the Help frame to the right of the dialog
            // moves the dialog to the left if needed
            Container parentContainer = parent.getParent();

            boolean extraParent = false;

            if (parentContainer.getName().lastIndexOf("dialog") != -1) {
                parentContainer = parentContainer.getParent();
                extraParent = true;
            }

            this.setSize(this.getWidth(), parentContainer.getHeight());

            int xCoordinate = (int) parentContainer.getLocation().getX() +
                    parentContainer.getWidth() + 2;

            int movedDistance = 0;

            while (xCoordinate + this.getWidth() >=
                    Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
                xCoordinate -= 2;
                movedDistance -= 2;
            }

            if (movedDistance != 0) {
                parent.getParent().setLocation((int) parent.getParent().getLocation().getX() +
                        movedDistance,
                        (int) parent.getParent().getLocation().getY());

                parent.setLocation((int) parent.getLocation().getX() +
                        movedDistance,
                        (int) parent.getLocation().getY());

                if (extraParent) {
                    parent.getParent().getParent().setLocation(
                            (int) parent.getParent().getParent().getLocation().getX() +
                            movedDistance,
                            (int) parent.getParent().getParent().getLocation().getY());
                }
            }

            setLocation(xCoordinate,
                    (int) parentContainer.getLocation().getY());
        } else {
            setTitle("About");
            setSize(400, 400);
            setLocationRelativeTo(parent);
        }

        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeJButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textJEditorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PRIDE Converter - Help");

        closeJButton.setText("Close");
        closeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeJButtonActionPerformed(evt);
            }
        });

        textJEditorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        textJEditorPane.setContentType("text/html");
        textJEditorPane.setEditable(false);
        textJEditorPane.setMinimumSize(new java.awt.Dimension(10, 10));
        textJEditorPane.setPreferredSize(new java.awt.Dimension(10, 10));
        textJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                textJEditorPaneHyperlinkUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(textJEditorPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 255, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .add(closeJButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 550, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(closeJButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog 
     *
     * @param evt
     */
    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
}//GEN-LAST:event_closeJButtonActionPerformed

    /**
     * Makes the links active.
     *
     * @param evt
     */
    private void textJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_textJEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ENTERED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.EXITED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {

            if (evt.getDescription().startsWith("#")) {
                textJEditorPane.scrollToReference(evt.getDescription());
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                BareBonesBrowserLaunch.openURL(evt.getDescription());
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
}//GEN-LAST:event_textJEditorPaneHyperlinkUpdate

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeJButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane textJEditorPane;
    // End of variables declaration//GEN-END:variables
}
