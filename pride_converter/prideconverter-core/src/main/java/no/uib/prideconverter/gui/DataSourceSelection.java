package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.HTMLDocument;
import no.uib.prideconverter.util.BareBonesBrowserLaunch;

/**
 * This frame handles the selection of the dataformat to be converted.
 * 
 * @author Harald Barsnes
 * 
 * Created March 2008
 */
public class DataSourceSelection extends javax.swing.JFrame {


    /** 
     * Opens a new DataSourceSelection frame, and inserts stored information.
     * 
     * @param location where to position the frame
     */
    public DataSourceSelection(Point location) {

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

        //descriptionJEditorPane.setFont(new java.awt.Font("Tahoma", 0, 11));

        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)descriptionJEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");

        // insert the information about the selected data source
        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("ms_lims")) {
            ms_limsJRadioButton.setSelected(true);
            ms_limsJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot DAT File")) {
            mascotDatFileJRadioButton.setSelected(true);
            mascotDatFileJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Generic File")) {
            mgfJRadioButton.setSelected(true);
            mgfJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST DTA File")) {
            sequestDTAFilesJRadioButton.setSelected(true);
            sequestDTAFilesJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
            xTandemJRadioButton.setSelected(true);
            xTandemJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Micromass PKL File")) {
            pklFilesJRadioButton.setSelected(true);
            pklFilesJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
            spectrumMillJRadioButton.setSelected(true);
            spectrumMillJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
            sequestResultsJRadioButton.setSelected(true);
            sequestResultsJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {
            mzXMLJRadioButton.setSelected(true);
            mzXMLJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("OMSSA")) {
            omssaJRadioButton.setSelected(true);
            omssaJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzData")) {
            mzDataJRadioButton.setSelected(true);
            mzDataJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("VEMS")) {
            vemsJRadioButton.setSelected(true);
            vemsJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("MS2")) {
            ms2JRadioButton.setSelected(true);
            ms2JRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            tppJRadioButton.setSelected(true);
            tppJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("DTASelect")) {
            dtaSelectJRadioButton.setSelected(true);
            dtaSelectJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else {
            ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Welcome Message");
            descriptionJEditorPane.setText("<br><b>PRIDE Converter</b><br><br>" +
                    "<b>PRIDE Converter</b> converts mass spectrometry data sets into valid PRIDE XML<br>" +
                    "for submission to the online <a href=\"http://www.ebi.ac.uk/pride/\">PRIDE repository</a>.<br><br>" +
                    "To get started select one of the supported data sources above and click Next.<br><br>" +
                    "For additional help click the help icons in the lower left corner of each frame.<br><br>" +
                    "More information about <b>PRIDE Converter</b> can be found here:<br>" +
                    "<a href=\"http://pride-converter.googlecode.com\">" +
                    "http://pride-converter.googlecode.com</a>");
            descriptionJEditorPane.setCaretPosition(0);
        }

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        //helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("../resources/icons/help.GIF")));

        setTitle(PRIDEConverter.getWizardName() + " " +
                PRIDEConverter.getPrideConverterVersionNumber() + " - " + getTitle());

        if (location != null) {
            setLocation(location);
        } else {
            setLocationRelativeTo(null);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        vemsJRadioButton = new javax.swing.JRadioButton();
        ms2JRadioButton = new javax.swing.JRadioButton();
        mzDataJRadioButton = new javax.swing.JRadioButton();
        mzXMLJRadioButton = new javax.swing.JRadioButton();
        dtaSelectJRadioButton = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        ms_limsJRadioButton = new javax.swing.JRadioButton();
        tppJRadioButton = new javax.swing.JRadioButton();
        omssaJRadioButton = new javax.swing.JRadioButton();
        sequestDTAFilesJRadioButton = new javax.swing.JRadioButton();
        sequestResultsJRadioButton = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        pklFilesJRadioButton = new javax.swing.JRadioButton();
        spectrumMillJRadioButton = new javax.swing.JRadioButton();
        xTandemJRadioButton = new javax.swing.JRadioButton();
        mgfJRadioButton = new javax.swing.JRadioButton();
        mascotDatFileJRadioButton = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        descriptionJPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionJEditorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Data Source Selection");
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
        backJButton.setEnabled(false);
        backJButton.setPreferredSize(new java.awt.Dimension(81, 23));

        cancelJButton.setText("Cancel");
        cancelJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Source"));

        buttonGroup1.add(vemsJRadioButton);
        vemsJRadioButton.setFont(vemsJRadioButton.getFont());
        vemsJRadioButton.setText("VEMS PKX Files");
        vemsJRadioButton.setToolTipText("Virtual Expert Mass Spectrometrist ");
        vemsJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        vemsJRadioButton.setIconTextGap(10);
        vemsJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vemsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vemsJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(ms2JRadioButton);
        ms2JRadioButton.setFont(ms2JRadioButton.getFont());
        ms2JRadioButton.setText("MS2");
        ms2JRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ms2JRadioButton.setIconTextGap(10);
        ms2JRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ms2JRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ms2JRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mzDataJRadioButton);
        mzDataJRadioButton.setFont(mzDataJRadioButton.getFont());
        mzDataJRadioButton.setText("mzData");
        mzDataJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mzDataJRadioButton.setIconTextGap(10);
        mzDataJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mzDataJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mzDataJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mzXMLJRadioButton);
        mzXMLJRadioButton.setFont(mzXMLJRadioButton.getFont());
        mzXMLJRadioButton.setText("mzXML");
        mzXMLJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mzXMLJRadioButton.setIconTextGap(10);
        mzXMLJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mzXMLJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mzXMLJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(dtaSelectJRadioButton);
        dtaSelectJRadioButton.setFont(dtaSelectJRadioButton.getFont());
        dtaSelectJRadioButton.setText("DTASelect");
        dtaSelectJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dtaSelectJRadioButton.setIconTextGap(10);
        dtaSelectJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dtaSelectJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dtaSelectJRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(vemsJRadioButton)
                    .add(ms2JRadioButton)
                    .add(mzXMLJRadioButton)
                    .add(mzDataJRadioButton)
                    .add(dtaSelectJRadioButton))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {dtaSelectJRadioButton, ms2JRadioButton, mzDataJRadioButton, mzXMLJRadioButton, vemsJRadioButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(vemsJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ms2JRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mzDataJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mzXMLJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dtaSelectJRadioButton)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {dtaSelectJRadioButton, ms2JRadioButton, mzDataJRadioButton, mzXMLJRadioButton, vemsJRadioButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        buttonGroup1.add(ms_limsJRadioButton);
        ms_limsJRadioButton.setFont(ms_limsJRadioButton.getFont());
        ms_limsJRadioButton.setText("ms_lims 7.3");
        ms_limsJRadioButton.setToolTipText("Mass Spectrometry Laboratory Information Managment System");
        ms_limsJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ms_limsJRadioButton.setIconTextGap(10);
        ms_limsJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ms_limsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ms_limsJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(tppJRadioButton);
        tppJRadioButton.setFont(tppJRadioButton.getFont());
        tppJRadioButton.setText("Peptide- and ProteinProphet");
        tppJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tppJRadioButton.setIconTextGap(10);
        tppJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tppJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tppJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(omssaJRadioButton);
        omssaJRadioButton.setFont(omssaJRadioButton.getFont());
        omssaJRadioButton.setText("OMSSA");
        omssaJRadioButton.setToolTipText("Open Mass Spectrometry Search Algorithm ");
        omssaJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        omssaJRadioButton.setIconTextGap(10);
        omssaJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        omssaJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(sequestDTAFilesJRadioButton);
        sequestDTAFilesJRadioButton.setFont(sequestDTAFilesJRadioButton.getFont());
        sequestDTAFilesJRadioButton.setText("SEQUEST DTA Files");
        sequestDTAFilesJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sequestDTAFilesJRadioButton.setIconTextGap(10);
        sequestDTAFilesJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sequestDTAFilesJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequestDTAFilesJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(sequestResultsJRadioButton);
        sequestResultsJRadioButton.setFont(sequestResultsJRadioButton.getFont());
        sequestResultsJRadioButton.setText("SEQUEST Result Files");
        sequestResultsJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sequestResultsJRadioButton.setIconTextGap(10);
        sequestResultsJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sequestResultsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequestResultsJRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sequestResultsJRadioButton)
                    .add(omssaJRadioButton)
                    .add(tppJRadioButton)
                    .add(sequestDTAFilesJRadioButton)
                    .add(ms_limsJRadioButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(sequestResultsJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sequestDTAFilesJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(omssaJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tppJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ms_limsJRadioButton)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        buttonGroup1.add(pklFilesJRadioButton);
        pklFilesJRadioButton.setFont(pklFilesJRadioButton.getFont());
        pklFilesJRadioButton.setText("Micromass PKL Files");
        pklFilesJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pklFilesJRadioButton.setIconTextGap(10);
        pklFilesJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pklFilesJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pklFilesJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(spectrumMillJRadioButton);
        spectrumMillJRadioButton.setFont(spectrumMillJRadioButton.getFont());
        spectrumMillJRadioButton.setText("Spectrum Mill");
        spectrumMillJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        spectrumMillJRadioButton.setIconTextGap(10);
        spectrumMillJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        spectrumMillJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumMillJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(xTandemJRadioButton);
        xTandemJRadioButton.setFont(xTandemJRadioButton.getFont());
        xTandemJRadioButton.setText("X!Tandem");
        xTandemJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        xTandemJRadioButton.setIconTextGap(10);
        xTandemJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        xTandemJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xTandemJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mgfJRadioButton);
        mgfJRadioButton.setFont(mgfJRadioButton.getFont());
        mgfJRadioButton.setText("Mascot Generic Files");
        mgfJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mgfJRadioButton.setIconTextGap(10);
        mgfJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mgfJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mgfJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mascotDatFileJRadioButton);
        mascotDatFileJRadioButton.setFont(mascotDatFileJRadioButton.getFont());
        mascotDatFileJRadioButton.setText("Mascot DAT Files");
        mascotDatFileJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mascotDatFileJRadioButton.setIconTextGap(10);
        mascotDatFileJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mascotDatFileJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mascotDatFileJRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(mgfJRadioButton)
                    .add(xTandemJRadioButton)
                    .add(mascotDatFileJRadioButton)
                    .add(spectrumMillJRadioButton)
                    .add(pklFilesJRadioButton))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(mascotDatFileJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mgfJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(xTandemJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spectrumMillJRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pklFilesJRadioButton)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(new java.awt.Component[] {jPanel1, jPanel3, jPanel5}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4Layout.linkSize(new java.awt.Component[] {jPanel1, jPanel3, jPanel5}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC), jLabel3.getFont().getSize()-2));
        jLabel3.setText("Select the source you want to convert data from, and click on 'Next'  to continue.");

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

        descriptionJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Source Description"));

        descriptionJEditorPane.setContentType("text/html");
        descriptionJEditorPane.setEditable(false);
        descriptionJEditorPane.setFont(descriptionJEditorPane.getFont());
        descriptionJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                descriptionJEditorPaneHyperlinkUpdate(evt);
            }
        });
        jScrollPane2.setViewportView(descriptionJEditorPane);

        org.jdesktop.layout.GroupLayout descriptionJPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionJPanel);
        descriptionJPanel.setLayout(descriptionJPanelLayout);
        descriptionJPanelLayout.setHorizontalGroup(
            descriptionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionJPanelLayout.setVerticalGroup(
            descriptionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 360, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, descriptionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descriptionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, 0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the frame and the wizard.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
        PRIDEConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Closes the frame and opens the next step (DataFileSelection/DataFileSelection_TwoFileTypes).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        if (ms_limsJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("ms_lims");
            new DataBaseDetails(this.getLocation());
        } else if (mascotDatFileJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("Mascot DAT File");
            new DataFileSelection(this.getLocation());
        } else if (mgfJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("Mascot Generic File");
            new DataFileSelection(this.getLocation());
        } else if (sequestDTAFilesJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("SEQUEST DTA File");
            new DataFileSelection(this.getLocation());
        } else if (xTandemJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("X!Tandem");
            new DataFileSelectionTwoFileTypes(this.getLocation());
        } else if (pklFilesJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("Micromass PKL File");
            new DataFileSelection(this.getLocation());
        } else if (spectrumMillJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("Spectrum Mill");
            new DataFileSelectionTwoFileTypes(this.getLocation());
        } else if (sequestResultsJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("SEQUEST Result File");
            new DataFileSelectionTwoFileTypes(this.getLocation());
        } else if (mzXMLJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("mzXML");
            new DataFileSelection(this.getLocation());
        } else if (omssaJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("OMSSA");
            new DataFileSelection(this.getLocation());
        } else if (mzDataJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("mzData");
            new DataFileSelection(this.getLocation());
        } else if (vemsJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("VEMS");
            new DataFileSelection(this.getLocation());
        } else if (ms2JRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("MS2");
            new DataFileSelection(this.getLocation());
        } else if (tppJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("TPP");
            new DataFileSelectionTPP(this.getLocation());
        } else if (dtaSelectJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setDataSource("DTASelect");
            new DataFileSelectionDTASelect(this.getLocation());
        }

        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens a Help frame.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/DataSourceSelection.html"));
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
     * Inserts information about the selected data source in the "Data Source 
     * Description" text area, and resets any stored information (list of 
     * selected source files etc.)
     * 
     * @param evt
     */
    private void ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ms_limsJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>ms_lims 7</b><br><br>" +
                "The ms_lims suite of software is mainly designed to " +
                "automate data flow in the high-throughput proteomics lab. Taking in spectrum " +
                "files from a variety of fileformats, it transforms these to the Mascot " +
                "Generic Format and stores them in the database.<br><br>" +
                "These can then be submitted to a search engine, e.g., Mascot from Matrix Science.<br><br>" +
                "PRIDE Converter currently supports ms_lims 7.3. " +
                "If you are using and older version you first need to update ms_lims.<br><br>" +
                "Homepage: <a href=\"http://genesis.ugent.be/ms_lims\">http://genesis.ugent.be/ms_lims</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("ms_lims")) {
            resetDataSourceProperties();
        }
    }//GEN-LAST:event_ms_limsJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void mascotDatFileJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mascotDatFileJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>Mascot DAT Files</b><br><br>" +
                "Mascot DAT Files are result files " +
                "from a Mascot search. <br>Both the spectra and the identifications are included.<br><br>" +
                "File Extension: .dat<br><br>" +
                "Homepage: <a href=\"http://www.matrixscience.com\">www.matrixscience.com</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot DAT File")) {
            resetDataSourceProperties();
        }
    }//GEN-LAST:event_mascotDatFileJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void mgfJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mgfJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>Mascot Generic Files</b><br><br>" +
                "The Mascot Generic File format is a generic format for submitting " +
                "data to Mascot. <br>It only contains information about the spectra.<br><br>" +
                "File Extension: .mgf<br><br>" +
                "Homepage: <a href=\"http://www.matrixscience.com\">www.matrixscience.com</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Generic File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mgfJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void xTandemJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xTandemJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>X!Tandem</b><br><br>" +
                "X!Tandem is open source software that matches " +
                "tandem mass spectra to peptide sequences.<br><br>" +
                "The output format is described here:<br>" +
                "<a href=\"http://www.thegpm.org/docs/X_series_output_form.pdf\">www.thegpm.org/docs/X_series_output_form.pdf</a>" +
                "<br><br>" +
                "File Extension: .xml (the identifications) and dta, mgf, pkl, mzData or mzXML (the spectra)<br><br>" +
                "Homepage: <a href=\"http://www.thegpm.org/TANDEM\">www.thegpm.org/TANDEM</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_xTandemJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void sequestResultsJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequestResultsJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>SEQUEST Result File</b><br><br>" + "SEQUEST Result File " +
                "are result files from a SEQUEST search.<br> " +
                "Includes two sets of files: *.dta (the spectrum files) and *.out " +
                "(the identification files).<br><br>" +
                "File Extensions: .dta and .out<br><br>" +
                "Homepage: <a href=\"http://fields.scripps.edu/?q=content/software\">http://fields.scripps.edu/?q=content/software</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_sequestResultsJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void omssaJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>OMSSA</b><br><br>" +
                "OMSSA MS/MS result files from the Open Mass Spectrometry Search Algorithm [OMSSA].<br><br>" +
                "File Extension: .omx<br><br>" +
                "Homepage: <a href=\"http://pubchem.ncbi.nlm.nih.gov/omssa/\">" +
                "http://pubchem.ncbi.nlm.nih.gov/omssa</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("OMSSA")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_omssaJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void spectrumMillJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumMillJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>Spectrum Mill</b><br><br>" +
                "Spectrum Mill search results consist of two sets of files: *.pkl (the spectrum files) and *.pkl.spo " +
                "(the identification files)." + "<br><br>" +
                "File Extensions: .pkl and .pkl.spo<br><br>" +
                "Homepage: <a href=\"http://www.chem.agilent.com\">" +
                "www.chem.agilent.com</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_spectrumMillJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void sequestDTAFilesJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequestDTAFilesJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>SEQUEST DTA Files</b><br><br>" +
                "SEQUEST DTA File is a simple MS/MS data format without identifications." +
                "<br>The first line contains the singly protonated peptide mass (MH+) and the peptide charge " +
                "state. <br>Subsequent lines contain space separated pairs of fragment ion m/z and intensity values. " +
                "<br><br>NB: Each file contains a single MS/MS data set.<br><br>" +
                "File Extension: .dta<br><br>" +
                "More Information: <a href=\"http://www.matrixscience.com/help/data_file_help.html#DTA\">" +
                "www.matrixscience.com/help/data_file_help.html#DTA</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST DTA File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_sequestDTAFilesJRadioButtonActionPerformed

    /**
     * Makes the hyperlinks active.
     * 
     * @param evt
     */
    private void descriptionJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_descriptionJEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ENTERED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.EXITED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {
            if (evt.getDescription().startsWith("#")) {
                descriptionJEditorPane.scrollToReference(evt.getDescription());
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                BareBonesBrowserLaunch.openURL(evt.getDescription());
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_descriptionJEditorPaneHyperlinkUpdate

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void pklFilesJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pklFilesJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>Micromass PKL Files</b><br><br>" + "Micromass PKL File " +
                "is a simple MS/MS data format without identifications. The first line contains the " +
                "observed m/z, intensity, and the charge state of the precursor peptide. <br>Subsequent lines " +
                "contain space separated pairs of fragment ion m/z and intensity values.<br><br>" +
                "Each file can include multiple MS/MS spectra.<br><br>" +
                "File Extension: .pkl<br><br>" +
                "More Information: <a href=\"http://www.matrixscience.com/help/data_file_help.html#QTOF\">" +
                "www.matrixscience.com/help/data_file_help.html#QTOF</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Micromass PKL File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_pklFilesJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void mzXMLJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mzXMLJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>mzXML</b><br><br>" +
                "mzXML is a common file format for proteomics mass spectrometric data.<br><br>" +
                "File Extension: .mzXML<br><br>" +
                "More Information: <a href=\"http://tools.proteomecenter.org/wiki/index.php?title=Formats:mzXML\">" +
                "http://tools.proteomecenter.org/wiki/index.php?title=Formats:mzXML</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mzXMLJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void tppJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tppJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>Peptide- and ProteinProphet</b><br><br>" +
                "PeptideProphet/ProteinProphet validates peptide assignments and protein identifications made " +
                "on the basis of peptides assigned to MS/MS spectra made by database search programs " +
                "such as SEQUEST.<br><br>" +
                "File Extensions: .pepXML, .pep.xml, .protXML and .prot.xml<br><br>" +
                "More Information: <br>" +
                "<a href=\"http://peptideprophet.sourceforge.net\">http://peptideprophet.sourceforge.net</a><br>" +
                "and<br>" +
                "<a href=\"http://proteinprophet.sourceforge.net\">http://proteinprophet.sourceforge.net</a><br><br><br>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_tppJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void mzDataJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mzDataJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>mzData</b><br><br>" +
                "mzData is a common file format for proteomics mass spectrometric data.<br><br>" +
                "File Extension: .mzData<br><br>" +
                "More Information: <a href=\"http://www.psidev.info/index.php?q=node/80#mzdata\">" +
                "www.psidev.info/index.php?q=node/80#mzdata</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzData")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mzDataJRadioButtonActionPerformed


    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void vemsJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vemsJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>VEMS: Virtual Expert Mass Spectrometrist</b><br><br>" +
                "The *.pkx format is a VEMS format and is very similar to the pkl format, but " +
                "also includes the retention time of the precursor and the charge state of the fragments.<br><br>" +
                "File Extension: .pkx<br><br>" +
                "More Information: <a href=\"http://personal.cicbiogune.es/rmatthiesen/\">" +
                "http://personal.cicbiogune.es/rmatthiesen/</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("VEMS")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_vemsJRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void ms2JRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ms2JRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>MS2</b><br><br>" +
                "MS2 files stores MS/MS data and can replace a folder of thousands of DTA files.<br>" +
                "It contains all the spectral information necessary for database searching algorithms.<br><br>" +
                "File Extension: .ms2<br><br>" +
                "More Information: <a href=\"http://doi.wiley.com/10.1002/rcm.1603\">" +
                "http://doi.wiley.com/10.1002/rcm.1603</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("MS2")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_ms2JRadioButtonActionPerformed

    /**
     * @see #ms_limsJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void dtaSelectJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dtaSelectJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<br>" +
                "<b>DTASelect</b><br><br>" +
                "DTASelect reassembles SEQUEST peptide information into protein information.<br><br>" +
                "File Extensions: .txt for the identifications (the DTASelect file) and .ms2 for the spectra<br><br>" +
                "More Information: <a href=\"http://fields.scripps.edu/DTASelect\">" +
                "http://fields.scripps.edu/DTASelect</a><br><br><br>" +
                "<b>NB: DTASelect support is currently in a beta status. " +
                "<br>If you find any bugs please let us know.</b>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("DTASelect")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_dtaSelectJRadioButtonActionPerformed

    /**
     * Resets all the data source properties. Like the list of selected files, 
     * the spectra table etc.
     */
    private void resetDataSourceProperties() {
        PRIDEConverter.getProperties().setSelectedSourceFiles(null);
        PRIDEConverter.getProperties().setSelectedIdentificationFiles(null);
        PRIDEConverter.getProperties().setSpectrumTableModel(null);
        PRIDEConverter.getProperties().setSpectraSelectionCriteria(null);
        PRIDEConverter.getProperties().setProjectIds(new ArrayList<Long>());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JEditorPane descriptionJEditorPane;
    private javax.swing.JPanel descriptionJPanel;
    private javax.swing.JRadioButton dtaSelectJRadioButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton mascotDatFileJRadioButton;
    private javax.swing.JRadioButton mgfJRadioButton;
    private javax.swing.JRadioButton ms2JRadioButton;
    private javax.swing.JRadioButton ms_limsJRadioButton;
    private javax.swing.JRadioButton mzDataJRadioButton;
    private javax.swing.JRadioButton mzXMLJRadioButton;
    private javax.swing.JButton nextJButton;
    private javax.swing.JRadioButton omssaJRadioButton;
    private javax.swing.JRadioButton pklFilesJRadioButton;
    private javax.swing.JRadioButton sequestDTAFilesJRadioButton;
    private javax.swing.JRadioButton sequestResultsJRadioButton;
    private javax.swing.JRadioButton spectrumMillJRadioButton;
    private javax.swing.JRadioButton tppJRadioButton;
    private javax.swing.JRadioButton vemsJRadioButton;
    private javax.swing.JRadioButton xTandemJRadioButton;
    // End of variables declaration//GEN-END:variables
}
