package no.uib.prideconverter.gui;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A window used to display help text in html format.
 * 
 * @author Harald Barsnes
 * 
 * Created November 2005
 */
public class HelpWindow extends javax.swing.JFrame {

    /**
     * Creates a new HelpWindow object with a Frame as a parent.
     *
     * @param parent
     * @param fileName the name of the help file
     */
    public HelpWindow(java.awt.Frame parent, URL fileName) {

        this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        initComponents();

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

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
     * Creates a new HelpWindow-object with a JDialog as a parent.
     *
     * @param parent
     * @param fileName the name of the help file
     */
    public HelpWindow(javax.swing.JDialog parent, URL fileName) {

        this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);

        initComponents();

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addComponent(closeJButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeJButton)
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

                try {
                    Desktop.getDesktop().browse(new URI(evt.getDescription()));
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

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
