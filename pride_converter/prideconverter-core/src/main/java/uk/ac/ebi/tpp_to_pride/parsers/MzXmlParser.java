/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 10-Oct-2006
 * Time: 14:19:11
 */
package uk.ac.ebi.tpp_to_pride.parsers;

import org.apache.log4j.Logger;
import org.systemsbiology.jrap.MSXMLParser;
import org.systemsbiology.jrap.MZXMLFileInfo;
import org.systemsbiology.jrap.Scan;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;

import java.io.File;
import java.util.*;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */
import javax.swing.JOptionPane;
import no.uib.prideconverter.gui.ProgressDialog;
import no.uib.prideconverter.util.Util;

/**
 * This class uses the JRAP library to parse mzXML into mzData files.
 *
 * @author martlenn
 * @version $Id: MzXmlParser.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 * Modified by Harald Barsnes (November 2008)
 */
public class MzXmlParser {

    /**
     * Define a static logger variable.
     */
    private static Logger logger = Logger.getLogger(PeptideProphetXMLParser.class);
    /**
     * The filename for the inputfile.
     */
    private String filename = null;
    /**
     * The JRAP MSXMLParser that this class wraps.
     */
    private MSXMLParser mxp = null;
    /**
     * The information present on the mzXML file.
     */
    private MZXMLFileInfo info = null;
    /**
     * A HashMap that infers precursorscans based on the order in which
     * spectra are present. If an MS1 spectrum is found, all directly following
     * MS2 spectra are assumed to have that MS1 spectrum as parent.
     */
    private HashMap scanNumberToParentScanNumber = null;
    /**
     * HashMap which contains the mzXML scan to mzData spectrum ID mapping
     * for precursor spectra who have already been used before.
     */
    private HashMap usedPrecursorSpectraScanToID = new HashMap();

    /**
     * This constructor initializes the mzXML file.
     *
     * @param inputFile File with the pointer to the actual mzXML file to be parsed.
     */
    public MzXmlParser(File inputFile, boolean aInferParentScans) {
        filename = inputFile.getName();
        mxp = new MSXMLParser(inputFile.getAbsolutePath());
        info = mxp.getHeaderInfo();
        logger.info("Read and parsed header of mzXML file '" + filename + "'.");
        if (aInferParentScans) {
            inferParentScans();
        }
    }

    /**
     * This method will retrieve all spectra from the mzXML file, providing mappings
     * in the HashMap for all these spectra as well as their parents, convert these
     * to mzData spectra and add them to the specified List. Note that the indices
     * used for these spectra will start from 1 + the index of the last spectrum in the List
     * (or '1' if the collection is empty).
     *
     * @param aSpectra  Collection to which the parsed mzData spectra will appended.
     * @param aFileAndScanToID  HashMap in which a lookup pair will be aided in the form:
     *                          ('runname scanNumber',mzData spectrum ID as Long) where
     *                          runname is the mzXML filename without the extension.
     */
    public void addAllSpectra(List aSpectra, HashMap aFileAndScanToID) {

        // new version that makes sure the scan numbers are correct
        for (int i = 1; i <= mxp.getScanCount(); i++) {
            addSpectrum(mxp.rap(i), aSpectra, aFileAndScanToID);
        }

        // old version
//        int scanNumber = 1;
//        int totalScans = mxp.getScanCount();
//
//        for(int i=scanNumber; i <= totalScans;i++) {
//            addSpectrum(i, aSpectra,  aFileAndScanToID);
//        }
    }

    /**
     * This method will retrieve all spectra from the mzXML file, providing mappings
     * in the HashMap for all these spectra as well as their parents, convert these
     * to mzData spectra and add them to the specified List. Note that the indices
     * used for these spectra will start from 1 + the index of the last spectrum in the List
     * (or '1' if the collection is empty).
     *
     * @param aSpectra  Collection to which the parsed mzData spectra will appended.
     * @param aFileAndScanToID  HashMap in which a lookup pair will be aided in the form:
     *                          ('runname scanNumber',mzData spectrum ID as Long) where
     *                          runname is the mzXML filename without the extension.
     * @param progressDialog A progressdialog that presents the progress for the given task.
     */
    public void addAllSpectra(List aSpectra, HashMap aFileAndScanToID, ProgressDialog progressDialog) {

        int totalScans = mxp.getScanCount();

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(totalScans);

        // new version that handles the spectra numbers correctly
        for (int i = 1; i <= totalScans; i++) {
            progressDialog.setValue(i);
            addSpectrum(mxp.rap(i), aSpectra, aFileAndScanToID);
        }

        // old version
//        int scanNumber = 1;
//        int totalScans = mxp.getScanCount();
//
//        for(int i=scanNumber; i <= totalScans;i++) {
//            addSpectrum(i, aSpectra,  aFileAndScanToID);
//        }
    }

    /**
     * This method will read the provided Collection of scan numbers, retrieve those
     * spectra as well as their parents, convert these to mzData spectra and add them
     * to the specified List. Note that the indices used for these spectra will
     * start from 1 + the index of the last spectrum in the List
     * (or '1' if the collection is empty).
     * Finally, the mzXML file (without extension!) and scan number will be used as
     * a String key (mzXml name + " " + scan number) in the provided lookup map with the
     * correct spectrum ID as value (of Long type). This lookup will only be possible for
     * the scans that are listed in the original Collection of scans, not for their
     * derived parents.
     *
     * @param aScans List of Integers with the scan numbers to process.
     * @param aSpectra  Collection to which the parsed mzData spectra will appended.
     * @param aFileAndScanToID  HashMap in which a lookup pair will be aided in the form:
     *                          ('runname scanNumber',mzData spectrum ID as Long) where
     *                          runname is the mzXML filename without the extension.
     */
    public void addSpectra(Collection aScans, List aSpectra, HashMap aFileAndScanToID) {
        for (Iterator lIterator = aScans.iterator(); lIterator.hasNext();) {
            int scanNbr = ((Integer) lIterator.next()).intValue();
            addSpectrum(scanNbr, aSpectra, aFileAndScanToID);
        }
    }

    /**
     * This method returns the info section of the mzXML file.
     *
     * @return  MZXMLFileInfo instance with the file info.
     */
    public MZXMLFileInfo getInfo() {
        return info;
    }

    /**
     * 
     * @param aMsScan
     * @param aSpectra
     * @param aFileAndScanToID
     * @return the id of the processed scan
     */
    public int addSpectrum(Scan aMsScan, List aSpectra, HashMap aFileAndScanToID) {
        Scan msScan = aMsScan;

        int scanNumber = msScan.getNum();

        // Process the scan (and all parents).
        int id = processScan(scanNumber, msScan, aSpectra);

        if (id > 0) {
            // We now have the ID we need to enter into the lookup table.
            //String key = filename.substring(0, filename.lastIndexOf(".")) + " " + msScan.getNum();

            // new faster(?) version to keep from having to use substring
            String[] tempString = filename.split("[.]");
            String key = "";

            for (int i = 0; i < tempString.length - 2; i++) {
                key += tempString[i] + ".";
            }

            key += tempString[tempString.length - 2] + " " + scanNumber;

            Object temp = aFileAndScanToID.put(key, new Integer(id));

            if (temp != null) {
                logger.error("Duplicate lookup for scan '" + key + "'!");

                // we already stored a result for this ID!!!
                JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                        "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + key + "'." );
            }
        }
        return id;
    }

    /**
     * 
     * @param aScanNumber
     * @param aSpectra
     * @param aFileAndScanToID
     * @return the id of the processed scan
     */
    public int addSpectrum(int aScanNumber, List aSpectra, HashMap aFileAndScanToID) {
        Scan msScan = mxp.rap(aScanNumber);
        // Process the scan (and all parents).
        int id = processScan(aScanNumber, msScan, aSpectra);
        if (id > 0) {
            // We now have the ID we need to enter into the lookup table.
            String key = filename.substring(0, filename.lastIndexOf(".")) + " " + aScanNumber;

            Object temp = aFileAndScanToID.put(key, new Integer(id));

            if (temp != null) {
                logger.error("Duplicate lookup for scan '" + key + "'!");

                // we already stored a result for this ID!!!
                JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                        "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + key + "'." );
            }
        }
        return id;
    }

    /**
     * This method adds the spectrum at the specified scan to the specified List
     * and assigns it a unique ID within this list (essentially, it's position
     * in the List + 1).
     *
     * @param aScanNumber   int with the sannumber to look up the spectrum for.
     * @param aSpectra  List to add the mzData Spectrum instance to.
     * @return  int with a unique spectrum ID in the specified List
     *              (essentially, the position of the Spectrum in the List + 1).
     */
    public int addSpectrum(int aScanNumber, List aSpectra) {

        Scan msScan = mxp.rap(aScanNumber);
        // Process the scan (and all parents).
        return processScan(aScanNumber, msScan, aSpectra);
    }

    /**
     * This method will process a scan and all its parents recursively.
     *
     * @param aScanNumber int with the scan number of the scan to process.
     * @param aScan Scan to process into an mzData spectrum.
     *              Note that all parent scans will also be processed.
     * @param aSpectra  List to which the processed spectra will be added.
     * @return  int with the ID assigned to the processed spectrum.
     */
    private int processScan(int aScanNumber, Scan aScan, List aSpectra) {
        
        // See if there is a parent scan. If so, do this one first.
        int parentID = -1;

        int precursorScanNum = aScan.getPrecursorScanNum();

        if (precursorScanNum > 0) {
            parentID = processScan(precursorScanNum, mxp.rap(precursorScanNum), aSpectra);
        } else if (aScan.getMsLevel() == 2 && scanNumberToParentScanNumber != null) {
            // try our own inferred parent scan...
            Integer parentScan = (Integer) scanNumberToParentScanNumber.get(aScanNumber);
            // See if this precursor spectrum has been used before.
            Integer previousParentID = (Integer) usedPrecursorSpectraScanToID.get(parentScan);
            if (previousParentID != null) {
                parentID = previousParentID;
            } else if (parentScan != null) {
                // new one! Process...
                parentID = processScan(parentScan, mxp.rap(parentScan), aSpectra);
            }
        }
 
        // Find the starting spectrum ID.
        // We take the last Spectrum's ID + 1,
        // or 1 if the collection is currently empty.
        int id = 1;
        if (aSpectra.size() > 0) {
            Spectrum spectrum = (Spectrum) aSpectra.get(aSpectra.size() - 1);
            id = ((int) spectrum.getSpectrumId()) + 1;
        }

        Spectrum mzDataSpectrum = null;
        if (aScan.getMsLevel() == 1) {
            // If this is a top-level parent spectrum, it will be an MS scan.
            mzDataSpectrum = parseMsSpectrum(aScanNumber, id, aScan);
            usedPrecursorSpectraScanToID.put(aScanNumber, id);
        } else {
            // If this is not a top-level parent spectrum, it will be an MS/MS scan.
            mzDataSpectrum = parseMsMsSpectrum(aScanNumber, id, parentID, aScan);
        }
        
        if (mzDataSpectrum.getMzArrayBinary().getDataLength() > 0) {
            aSpectra.add(mzDataSpectrum);
        } else {
            logger.warn("Skipped empty spectrum in '" + this.filename + "', scan nbr. '" + aScanNumber + "'!");
        }
        
        return id;
    }

    /**
     * This method accepts an mzXML ms scan and transforms it into an mzData Spectrum element.
     *
     * @param aScanNumber int with the scan number of the scan to process.
     * @param aSpectrumID  int with the Spectrum ID for this ms scan.
     * @param aScan Scan with the scan information.
     * @return  Spectrum instance with the mzData spectrum.
     */
    private Spectrum parseMsSpectrum(int aScanNumber, int aSpectrumID, Scan aScan) {
        // CV parameters for the spectruminstrument.
        // Note that this information is hardcoded here!
        Collection spectrumInstrumentCvParameters = new ArrayList(2);
        spectrumInstrumentCvParameters.add(new CvParamImpl("PSI:1000036", "psi", "ScanMode", 0, aScan.getScanType()));
        spectrumInstrumentCvParameters.add(new CvParamImpl("PSI:1000037", "psi", "Polarity", 1, aScan.getPolarity()));
        
        // Spectrum description comment containing the original mzXML scan number.
        //SpectrumDescComment comment = new SpectrumDescCommentImpl("Original mzXML scan number: " + aScanNumber);
        ArrayList spectrumDescriptionComments = null;//new ArrayList(1);
        //spectrumDescriptionComments.add(comment);
        
        // Create new mzData spectrum for the fragmentation spectrum.
        // Notice that certain collections and annotations are 'null' here.
        // Feel free to add any kind of annotation yourself, however.
        return new SpectrumImpl(new BinaryArrayImpl(aScan.getMassIntensityList()[1], BinaryArrayImpl.BIG_ENDIAN_LABEL),
                new Double(aScan.getLowMz()), new BinaryArrayImpl(aScan.getMassIntensityList()[0],
                BinaryArrayImpl.BIG_ENDIAN_LABEL), aScan.getMsLevel(), null, new Double(aScan.getHighMz()),
                null, aSpectrumID, null, spectrumDescriptionComments, spectrumInstrumentCvParameters, null, null, null);
    }

    /**
     * This method accepts an mzXML ms/ms scan and transforms it into an mzData Spectrum element.
     *
     * @param aScanNumber int with the scan number of the scan to process.
     * @param aSpectrumID  int with the Spectrum ID for this ms scan.
     * @param aPrecursorSpectrumID  int with the Spectrum ID of the ms scan holding the precursor.
     * @param aScan Scan with the scan information.
     * @return  Spectrum instance with the mzData spectrum.
     */
    private Spectrum parseMsMsSpectrum(int aScanNumber, int aSpectrumID, int aPrecursorSpectrumID, Scan aScan) {

        // CV parameters for the spectruminstrument.
        // Note that this information is hardcoded here!
        Collection spectrumInstrumentCvParameters = new ArrayList(2);

        if (aScan.getScanType() != null) {
            spectrumInstrumentCvParameters.add(new CvParamImpl("PSI:1000036", "psi", "ScanMode", 0, aScan.getScanType()));
        }

        spectrumInstrumentCvParameters.add(new CvParamImpl("PSI:1000037", "psi", "Polarity", 1, aScan.getPolarity()));

        // Precursor annotation collection.
        Collection precursors = new ArrayList(1);

        // Ion selection annotation parameters.
        Collection ionSelection = new ArrayList(3);

        // See if we know the precursor charge, and if so, include it.
        int charge = aScan.getPrecursorCharge();
        if (charge > 0) {
            ionSelection.add(new CvParamImpl("PSI:1000041", "psi", "ChargeState", 0, Integer.toString(charge)));
        }
        ionSelection.add(new CvParamImpl("PSI:1000040", "psi", "MassToChargeRatio", 1, Double.toString(aScan.getPrecursorMz())));
        ionSelection.add(new CvParamImpl("PSI:RETENTION TIME", "PSI", "Retention time", 2, aScan.getRetentionTime()));

        // Add the precursor.
        precursors.add(new PrecursorImpl(null, null, ionSelection, null, aScan.getMsLevel() - 1, aPrecursorSpectrumID, 0));

        // Spectrum description comment containing the original mzXML scan number.
        //SpectrumDescComment comment = new SpectrumDescCommentImpl("Original mzXML scan number: " + aScanNumber);
        ArrayList spectrumDescriptionComments = null;//new ArrayList(1);
        //spectrumDescriptionComments.add(comment);

        // Create new mzData spectrum for the fragmentation spectrum.
        // Notice that certain collections and annotations are 'null' here.
        // Feel free to add any kind of annotation yourself, however.
        return new SpectrumImpl(new BinaryArrayImpl(aScan.getMassIntensityList()[1], 
                BinaryArrayImpl.BIG_ENDIAN_LABEL), new Double(aScan.getLowMz()),
                new BinaryArrayImpl(aScan.getMassIntensityList()[0], BinaryArrayImpl.BIG_ENDIAN_LABEL),
                2, null, new Double(aScan.getHighMz()), null, aSpectrumID, precursors,
                spectrumDescriptionComments, spectrumInstrumentCvParameters, null, null, null);
    }

    /**
     * 
     */
    private void inferParentScans() {
        logger.debug("Started inferring parent scans.");
        scanNumberToParentScanNumber = new HashMap();
        
        // new version that makes sure the scan numbers are correct
        
        int totalScans = mxp.getScanCount();
        int parentScanNumber = -1;
        int currentScanNumber = -1;
        
        for (int i = 1; i <= totalScans; i++) {
            Scan scan = mxp.rap(i);

            if (scan != null) {
                
                currentScanNumber = scan.getNum();
                
                //System.out.println("currentScanNumber: " + currentScanNumber);
                
                if (scan.getMsLevel() == 1) {
                    parentScanNumber = currentScanNumber;
                } else if (scan.getMsLevel() == 2) {
                    if (parentScanNumber != -1) {
                        scanNumberToParentScanNumber.put(currentScanNumber, parentScanNumber);
                        
                        //System.out.println("currentScanNumber: " + currentScanNumber + " parentScanNumber: " + parentScanNumber);
                    }
                } else {
                    logger.warn("Unable to handle scan of msLevel '" + scan.getMsLevel() + "'. I can only deal with MS levels '1' and '2'!");
                }
            }
        }
        logger.info("Finished inferring parent scans. Inferred parents for " + scanNumberToParentScanNumber.size() + " MS2 spectra.");
        
        
        // old version
//        int scanNumber = 1;
//        int totalScans = mxp.getScanCount();
//        int parentScanNumber = -1;
//        for (int i = scanNumber; i <= totalScans; i++) {
//            Scan scan = mxp.rap(i);
//            if (scan != null) {
//                if (scan.getMsLevel() == 1) {
//                    parentScanNumber = i;
//                } else if (scan.getMsLevel() == 2) {
//                    if (parentScanNumber != -1) {
//                        scanNumberToParentScanNumber.put(i, parentScanNumber);
//                    }
//                } else {
//                    logger.warn("Unable to handle scan of msLevel '" + scan.getMsLevel() + "'. I can only deal with MS levels '1' and '2'!");
//                }
//            }
//        }
//        logger.info("Finished inferring parent scans. Inferred parents for " + scanNumberToParentScanNumber.size() + " MS2 spectra.");
    }
}
