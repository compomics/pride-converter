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

    private PRIDEConverter prideConverter;

    /** 
     * Opens a new DataSourceSelection frame, and inserts stored information.
     * 
     * @param prideConverter
     * @param location where to position the frame
     */
    public DataSourceSelection(PRIDEConverter prideConverter, Point location) {

        this.prideConverter = prideConverter;

        // sets the default wizard frame size
        this.setPreferredSize(new Dimension(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT));
        this.setSize(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT);
        this.setMaximumSize(new Dimension(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT));
        this.setMinimumSize(new Dimension(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT));

        initComponents();

        //descriptionJEditorPane.setFont(new java.awt.Font("Tahoma", 0, 11));

        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)descriptionJEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");

        // insert the information about the selected data source
        if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("ms_lims")) {
            ms_limsJRadioButton.setSelected(true);
            ms_limsJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Dat File")) {
            mascotDatFileJRadioButton.setSelected(true);
            mascotDatFileJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Generic File")) {
            mgfJRadioButton.setSelected(true);
            mgfJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Sequest DTA File")) {
            sequestDTAFilesJRadioButton.setSelected(true);
            sequestDTAFilesJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
            xTandemJRadioButton.setSelected(true);
            xTandemJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Micromass PKL File")) {
            pklFilesJRadioButton.setSelected(true);
            pklFilesJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
            spectrumMillJRadioButton.setSelected(true);
            spectrumMillJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Sequest Result File")) {
            sequestResultsJRadioButton.setSelected(true);
            sequestResultsJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {
            mzXMLJRadioButton.setSelected(true);
            mzXMLJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("OMSSA")) {
            omssaJRadioButton.setSelected(true);
            omssaJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzML")) {
            mzMLJRadioButton.setSelected(true);
            mzMLJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzData")) {
            mzDataJRadioButton.setSelected(true);
            mzDataJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("VEMS")) {
            vemsJRadioButton.setSelected(true);
            vemsJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("MS2")) {
            ms2JRadioButton.setSelected(true);
            ms2JRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            tppJRadioButton.setSelected(true);
            tppJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("DTASelect")) {
            dtaSelectJRadioButton.setSelected(true);
            dtaSelectJRadioButtonActionPerformed(null);
            nextJButton.setEnabled(true);
        } else {
            ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Welcome Message");
            descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br><b>PRIDE Converter</b><br><br>" +
                    "<b>PRIDE Converter</b> converts mass spectrometry data sets into valid PRIDE XML<br>" +
                    "for submission to the online <a href=\"http://www.ebi.ac.uk/pride/\">PRIDE repository</a>.<br><br>" +
                    "To get started select one of the supported data sources above and click Next.<br><br>" +
                    "For additional help click the help icons in the lower left corner of each frame.<br><br>" +
                    "More information about <b>PRIDE Converter</b> can be found here:<br>" +
                    "<a href=\"http://code.google.com/p/pride-converter\">" +
                    "http://code.google.com/p/pride-converter</a>");
            descriptionJEditorPane.setCaretPosition(0);
        }

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        //helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("../resources/icons/help.GIF")));

        setTitle(prideConverter.getWizardName() + " " +
                prideConverter.getPrideConverterVersionNumber() + " - " + getTitle());

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
        mascotDatFileJRadioButton = new javax.swing.JRadioButton();
        mgfJRadioButton = new javax.swing.JRadioButton();
        xTandemJRadioButton = new javax.swing.JRadioButton();
        sequestResultsJRadioButton = new javax.swing.JRadioButton();
        spectrumMillJRadioButton = new javax.swing.JRadioButton();
        sequestDTAFilesJRadioButton = new javax.swing.JRadioButton();
        pklFilesJRadioButton = new javax.swing.JRadioButton();
        mzXMLJRadioButton = new javax.swing.JRadioButton();
        tppJRadioButton = new javax.swing.JRadioButton();
        omssaJRadioButton = new javax.swing.JRadioButton();
        mzMLJRadioButton = new javax.swing.JRadioButton();
        phenyxJRadioButton = new javax.swing.JRadioButton();
        ms_limsJRadioButton = new javax.swing.JRadioButton();
        vemsJRadioButton = new javax.swing.JRadioButton();
        ms2JRadioButton = new javax.swing.JRadioButton();
        mzDataJRadioButton = new javax.swing.JRadioButton();
        dtaSelectJRadioButton = new javax.swing.JRadioButton();
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

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Source", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        buttonGroup1.add(mascotDatFileJRadioButton);
        mascotDatFileJRadioButton.setFont(mascotDatFileJRadioButton.getFont());
        mascotDatFileJRadioButton.setText("Mascot Dat Files");
        mascotDatFileJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mascotDatFileJRadioButton.setIconTextGap(20);
        mascotDatFileJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mascotDatFileJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mascotDatFileJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mgfJRadioButton);
        mgfJRadioButton.setText("Mascot Generic Files");
        mgfJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mgfJRadioButton.setIconTextGap(20);
        mgfJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mgfJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mgfJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(xTandemJRadioButton);
        xTandemJRadioButton.setText("X!Tandem");
        xTandemJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        xTandemJRadioButton.setIconTextGap(20);
        xTandemJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        xTandemJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xTandemJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(sequestResultsJRadioButton);
        sequestResultsJRadioButton.setText("Sequest Result Files");
        sequestResultsJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sequestResultsJRadioButton.setIconTextGap(20);
        sequestResultsJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sequestResultsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequestResultsJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(spectrumMillJRadioButton);
        spectrumMillJRadioButton.setText("Spectrum Mill");
        spectrumMillJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        spectrumMillJRadioButton.setIconTextGap(20);
        spectrumMillJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        spectrumMillJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumMillJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(sequestDTAFilesJRadioButton);
        sequestDTAFilesJRadioButton.setText("Sequest DTA Files");
        sequestDTAFilesJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sequestDTAFilesJRadioButton.setIconTextGap(20);
        sequestDTAFilesJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sequestDTAFilesJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequestDTAFilesJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(pklFilesJRadioButton);
        pklFilesJRadioButton.setText("Micromass PKL Files");
        pklFilesJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pklFilesJRadioButton.setIconTextGap(20);
        pklFilesJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pklFilesJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pklFilesJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mzXMLJRadioButton);
        mzXMLJRadioButton.setText("mzXML");
        mzXMLJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mzXMLJRadioButton.setIconTextGap(20);
        mzXMLJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mzXMLJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mzXMLJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(tppJRadioButton);
        tppJRadioButton.setText("Peptide- and ProteinProphet");
        tppJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tppJRadioButton.setIconTextGap(20);
        tppJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tppJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tppJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(omssaJRadioButton);
        omssaJRadioButton.setText("OMSSA");
        omssaJRadioButton.setToolTipText("Open Mass Spectrometry Search Algorithm ");
        omssaJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        omssaJRadioButton.setIconTextGap(20);
        omssaJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        omssaJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mzMLJRadioButton);
        mzMLJRadioButton.setText("mzML");
        mzMLJRadioButton.setToolTipText("In Development");
        mzMLJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mzMLJRadioButton.setEnabled(false);
        mzMLJRadioButton.setIconTextGap(20);
        mzMLJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mzMLJRadioButton.setPreferredSize(new java.awt.Dimension(0, 0));
        mzMLJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mzMLJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(phenyxJRadioButton);
        phenyxJRadioButton.setText("Phenyx");
        phenyxJRadioButton.setToolTipText("In Development");
        phenyxJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        phenyxJRadioButton.setEnabled(false);
        phenyxJRadioButton.setIconTextGap(20);
        phenyxJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        phenyxJRadioButton.setPreferredSize(new java.awt.Dimension(0, 0));
        phenyxJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phenyxJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(ms_limsJRadioButton);
        ms_limsJRadioButton.setText("ms_lims 6");
        ms_limsJRadioButton.setToolTipText("Mass Spectrometry Laboratory Information Managment System");
        ms_limsJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ms_limsJRadioButton.setIconTextGap(20);
        ms_limsJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ms_limsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ms_limsJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(vemsJRadioButton);
        vemsJRadioButton.setText("VEMS");
        vemsJRadioButton.setToolTipText("Virtual Expert Mass Spectrometrist ");
        vemsJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        vemsJRadioButton.setIconTextGap(20);
        vemsJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vemsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vemsJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(ms2JRadioButton);
        ms2JRadioButton.setText("MS2");
        ms2JRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ms2JRadioButton.setIconTextGap(20);
        ms2JRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ms2JRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ms2JRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mzDataJRadioButton);
        mzDataJRadioButton.setText("mzData");
        mzDataJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mzDataJRadioButton.setIconTextGap(20);
        mzDataJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mzDataJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mzDataJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(dtaSelectJRadioButton);
        dtaSelectJRadioButton.setText("DTASelect");
        dtaSelectJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dtaSelectJRadioButton.setIconTextGap(20);
        dtaSelectJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dtaSelectJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dtaSelectJRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mgfJRadioButton)
                    .add(xTandemJRadioButton)
                    .add(mascotDatFileJRadioButton)
                    .add(spectrumMillJRadioButton)
                    .add(pklFilesJRadioButton))
                .add(75, 75, 75)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sequestResultsJRadioButton)
                    .add(sequestDTAFilesJRadioButton)
                    .add(tppJRadioButton)
                    .add(omssaJRadioButton)
                    .add(ms_limsJRadioButton))
                .add(57, 57, 57)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(71, 71, 71)
                        .add(phenyxJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(vemsJRadioButton)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(ms2JRadioButton)
                        .add(18, 18, 18)
                        .add(mzMLJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mzXMLJRadioButton)
                    .add(mzDataJRadioButton)
                    .add(dtaSelectJRadioButton))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phenyxJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(mascotDatFileJRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mgfJRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(xTandemJRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(spectrumMillJRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pklFilesJRadioButton))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sequestResultsJRadioButton)
                            .add(vemsJRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sequestDTAFilesJRadioButton)
                            .add(ms2JRadioButton)
                            .add(mzMLJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(omssaJRadioButton)
                            .add(mzDataJRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(tppJRadioButton)
                            .add(mzXMLJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(ms_limsJRadioButton)
                            .add(dtaSelectJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 2, 11));
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

        descriptionJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Source Description", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        descriptionJEditorPane.setContentType("text/html");
        descriptionJEditorPane.setEditable(false);
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
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionJPanelLayout.setVerticalGroup(
            descriptionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, descriptionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, descriptionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 309, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3))
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
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
        prideConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Closes the frame and opens the next step (DataFileSelection/DataFileSelection_TwoFileTypes).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        if (ms_limsJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("ms_lims");
            new DataBaseDetails(prideConverter, this.getLocation());
        } else if (mascotDatFileJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("Mascot Dat File");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (mgfJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("Mascot Generic File");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (sequestDTAFilesJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("Sequest DTA File");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (xTandemJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("X!Tandem");
            new DataFileSelectionTwoFileTypes(prideConverter, this.getLocation());
        } else if (pklFilesJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("Micromass PKL File");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (spectrumMillJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("Spectrum Mill");
            new DataFileSelectionTwoFileTypes(prideConverter, this.getLocation());
        } else if (sequestResultsJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("Sequest Result File");
            new DataFileSelectionTwoFileTypes(prideConverter, this.getLocation());
        } else if (mzXMLJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("mzXML");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (omssaJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("OMSSA");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (mzMLJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("mzML");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (mzDataJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("mzData");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (vemsJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("VEMS");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (ms2JRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("MS2");
            new DataFileSelection(prideConverter, this.getLocation());
        } else if (tppJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("TPP");
            new DataFileSelectionTPP(prideConverter, this.getLocation());
        } else if (dtaSelectJRadioButton.isSelected()) {
            prideConverter.getProperties().setDataSource("DTASelect");
            new DataFileSelectionDTASelect(prideConverter, this.getLocation());
        }

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
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>ms_lims 6</b><br><br>" +
                "The ms_lims suite of software is mainly designed to " +
                "automate data flow in the high-throughput proteomics lab. Taking in spectrum " +
                "files from a variety of fileformats, it transforms these to the Mascot " +
                "Generic Format and stores them in the database.<br><br>" +
                "These can then be submitted to a search engine, e.g., Mascot from Matrix Science.<br><br>" +
                "PRIDE Converter currently supports ms_lims 6.<br><br>" + 
                "Homepage: <a href=\"http://genesis.ugent.be/ms_lims\">http://genesis.ugent.be/ms_lims</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("ms_lims")) {
            resetDataSourceProperties();
        }
    }//GEN-LAST:event_ms_limsJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void mascotDatFileJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mascotDatFileJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Mascot Dat Files</b><br><br>" +
                "Mascot Dat Files are result files " +
                "from a Mascot search. <br>Both the spectra and the identifications are included.<br><br>" +
                "File Extension: .dat<br><br>" +
                "Homepage: <a href=\"http://www.matrixscience.com\">www.matrixscience.com</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Dat File")) {
            resetDataSourceProperties();
        }
    }//GEN-LAST:event_mascotDatFileJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void mgfJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mgfJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Mascot Generic Files</b><br><br>" +
                "The Mascot Generic File format is a generic format for submitting " +
                "data to Mascot. <br>It only contains information about the spectra.<br><br>" +
                "File Extension: .mgf<br><br>" +
                "Homepage: <a href=\"http://www.matrixscience.com\">www.matrixscience.com</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Generic File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mgfJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void xTandemJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xTandemJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>X!Tandem</b><br><br>" +
                "X!Tandem is open source software that matches " +
                "tandem mass spectra to peptide sequences.<br><br>" +
                "The output format is described here:<br>" +
                "<a href=\"http://www.thegpm.org/docs/X_series_output_form.pdf\">www.thegpm.org/docs/X_series_output_form.pdf</a>" +
                "<br><br>" +
                "File Extension: .xml (the identifications) and dta, mgf, pkl, mzData or mzXML (the spectra)<br><br>" +
                "Homepage: <a href=\"http://www.thegpm.org/TANDEM\">www.thegpm.org/TANDEM</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_xTandemJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void sequestResultsJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequestResultsJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Sequest Result File</b><br><br>" + "Sequest Result File " +
                "are result files from a Sequest search.<br> " +
                "Includes two sets of files: *.dta (the spectrum files) and *.out " +
                "(the identification files).<br><br>" +
                "File Extensions: .dta and .out<br><br>" +
                "Homepage: <a href=\"http://fields.scripps.edu/?q=content/software\">http://fields.scripps.edu/?q=content/software</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Sequest Result File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_sequestResultsJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void omssaJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>OMSSA</b><br><br>" +
                "OMSSA MS/MS result files from the Open Mass Spectrometry Search Algorithm [OMSSA].<br><br>" +
                "File Extension: .omx<br><br>" +
                "Homepage: <a href=\"http://pubchem.ncbi.nlm.nih.gov/omssa/\">" +
                "http://pubchem.ncbi.nlm.nih.gov/omssa</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("OMSSA")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_omssaJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void spectrumMillJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumMillJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Spectrum Mill</b><br><br>" +
                "Spectrum Mill search results consist of two sets of files: *.pkl (the spectrum files) and *.pkl.spo " +
                "(the identification files)." + "<br><br>" +
                "File Extensions: .pkl and .pkl.spo<br><br>" +
                "Homepage: <a href=\"http://www.chem.agilent.com\">" +
                "www.chem.agilent.com</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_spectrumMillJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void sequestDTAFilesJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequestDTAFilesJRadioButtonActionPerformed

        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Sequest DTA Files</b><br><br>" +
                "Sequest DTA File is a simple MS/MS data format without identifications." +
                "<br>The first line contains the singly protonated peptide mass (MH+) and the peptide charge " +
                "state. <br>Subsequent lines contain space separated pairs of fragment ion m/z and intensity values. " +
                "<br><br>NB: Each file contains a single MS/MS data set.<br><br>" +
                "File Extension: .dta<br><br>" +
                "More Information: <a href=\"http://www.matrixscience.com/help/data_file_help.html#DTA\">" +
                "www.matrixscience.com/help/data_file_help.html#DTA</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Sequest DTA File")) {
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
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void pklFilesJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pklFilesJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Micromass PKL Files</b><br><br>" + "Micromass PKL File " +
                "is a simple MS/MS data format without identifications. The first line contains the " +
                "observed m/z, intensity, and the charge state of the precursor peptide. <br>Subsequent lines " +
                "contain space separated pairs of fragment ion m/z and intensity values.<br><br>" +
                "Each file can include multiple MS/MS spectra.<br><br>" +
                "File Extension: .pkl<br><br>" +
                "More Information: <a href=\"http://www.matrixscience.com/help/data_file_help.html#QTOF\">" +
                "www.matrixscience.com/help/data_file_help.html#QTOF</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Micromass PKL File")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_pklFilesJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void mzXMLJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mzXMLJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>mzXML</b><br><br>" +
                "mzXML is a common file format for proteomics mass spectrometric data.<br><br>" +
                "File Extension: .mzXML<br><br>" +
                "More Information: <a href=\"http://tools.proteomecenter.org/wiki/index.php?title=Formats:mzXML\">" +
                "http://tools.proteomecenter.org/wiki/index.php?title=Formats:mzXML</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mzXMLJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void tppJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tppJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
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

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_tppJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void mzMLJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mzMLJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>mzML</b><br><br>" +
                "mzML is a common file format for proteomics mass spectrometric data.<br><br>" +
                "File Extension: .mzML<br><br>" +
                "More Information: <a href=\"http://www.psidev.info/index.php?q=node/257\">" +
                "http://www.psidev.info/index.php?q=node/257</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzML")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mzMLJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void mzDataJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mzDataJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>mzData</b><br><br>" +
                "mzData is a common file format for proteomics mass spectrometric data.<br><br>" +
                "File Extension: .mzData<br><br>" +
                "More Information: <a href=\"http://www.psidev.info/index.php?q=node/80#mzdata\">" +
                "www.psidev.info/index.php?q=node/80#mzdata</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzData")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_mzDataJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void phenyxJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phenyxJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>Phenyx</b><br><br>" + "Phenyx<br><br>" + "File Extension: ?<br><br>" +
                "More Information: <a href=\"http://www.genebio.com/products/phenyx/\">" +
                "http://www.genebio.com/products/phenyx/</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("Phenyx")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_phenyxJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void vemsJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vemsJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>VEMS: Virtual Expert Mass Spectrometrist</b><br><br>" +
                "The *.pkx format is a VEMS format and is very similar to the pkl format, but " +
                "also includes the retention time of the precursor and the charge state of the fragments.<br><br>" +
                "File Extension: .pkx<br><br>" +
                "More Information: <a href=\"http://personal.cicbiogune.es/rmatthiesen/\">" +
                "http://personal.cicbiogune.es/rmatthiesen/</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("VEMS")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_vemsJRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void ms2JRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ms2JRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>MS2</b><br><br>" +
                "MS2 files stores MS/MS data and can replace a folder of thousands of DTA files.<br>" +
                "It contains all the spectral information necessary for database searching algorithms.<br><br>" +
                "File Extension: .ms2<br><br>" +
                "More Information: <a href=\"http://doi.wiley.com/10.1002/rcm.1603\">" +
                "http://doi.wiley.com/10.1002/rcm.1603</a>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("MS2")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_ms2JRadioButtonActionPerformed

    /**
     * See ms_limsJRadioButtonActionPerformed
     * 
     * @param evt
     */
    private void dtaSelectJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dtaSelectJRadioButtonActionPerformed
        ((TitledBorder) descriptionJPanel.getBorder()).setTitle("Data Source Description");
        repaint();

        this.nextJButton.setEnabled(true);
        descriptionJEditorPane.setText("<font size=\"3\" face=\"Tahoma\"><br>" +
                "<b>DTASelect</b><br><br>" +
                "DTASelect reassembles Sequest peptide information into protein information.<br><br>" +
                "File Extensions: .txt for the identifications (the DTASelect file) and .ms2 for the spectra<br><br>" +
                "More Information: <a href=\"http://fields.scripps.edu/DTASelect\">" +
                "http://fields.scripps.edu/DTASelect</a><br><br><br>" +
                "<b>NB: DTASelect support is currently in a beta status. " +
                "<br>If you find any bugs please let us know.</b>");
        descriptionJEditorPane.setCaretPosition(0);

        if (!prideConverter.getProperties().getDataSource().equalsIgnoreCase("DTASelect")) {
            resetDataSourceProperties();
        }
}//GEN-LAST:event_dtaSelectJRadioButtonActionPerformed

    /**
     * Resets all the data source properties. Like the list of selected files, 
     * the spectra table etc.
     */
    private void resetDataSourceProperties() {
        prideConverter.getProperties().setSelectedSourceFiles(null);
        prideConverter.getProperties().setSelectedIdentificationFiles(null);
        prideConverter.getProperties().setSpectrumTableModel(null);
        prideConverter.getProperties().setSpectraSelectionCriteria(null);
        prideConverter.getProperties().setProjectIds(new ArrayList<Long>());
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton mascotDatFileJRadioButton;
    private javax.swing.JRadioButton mgfJRadioButton;
    private javax.swing.JRadioButton ms2JRadioButton;
    private javax.swing.JRadioButton ms_limsJRadioButton;
    private javax.swing.JRadioButton mzDataJRadioButton;
    private javax.swing.JRadioButton mzMLJRadioButton;
    private javax.swing.JRadioButton mzXMLJRadioButton;
    private javax.swing.JButton nextJButton;
    private javax.swing.JRadioButton omssaJRadioButton;
    private javax.swing.JRadioButton phenyxJRadioButton;
    private javax.swing.JRadioButton pklFilesJRadioButton;
    private javax.swing.JRadioButton sequestDTAFilesJRadioButton;
    private javax.swing.JRadioButton sequestResultsJRadioButton;
    private javax.swing.JRadioButton spectrumMillJRadioButton;
    private javax.swing.JRadioButton tppJRadioButton;
    private javax.swing.JRadioButton vemsJRadioButton;
    private javax.swing.JRadioButton xTandemJRadioButton;
    // End of variables declaration//GEN-END:variables
}
