
---

**This project is out-dated and we strongly recommend to use the newer [PRIDE Converter 2](https://github.com/PRIDE-Toolsuite/pride-converter-2).**

Input formats, not supported by PRIDE Converter 2, for instance ms\_lims, SEQUEST Result Files and Spectrum Mill can be still be converted by the old converter, referred to simply as PRIDE Converter at the current page.

You can also try [PeptideShaker](http://peptide-shaker.googlecode.com) which has as built-in PRIDE XML exporter.

---

# PRIDE Converter 
  * [About PRIDE Converter](#about-pride-converter)
  * [Getting PRIDE Converter](#getting-pride-converter)
    * [Requirements](#requirements)
    * [Downloading](#downloading)
    * [Troubleshooting](#troubleshooting)
  * [Upgrading](#upgrading)
  * [How to Submit Data to PRIDE](#how-to-submit-data-to-pride)
  * [How to Reference PRIDE Submissions](#how-to-reference-pride-submissions)
  * [Getting Help](#getting-help)
  * [Source Code](#source-code)
  * [Screenshots](#screenshots)

**PRIDE Converter Publications:**
  * [Barsnes et al: Nat Biotechnol. 2009 Jul;27(7):598-9](http://www.nature.com/nbt/journal/v27/n7/full/nbt0709-598.html).
  * [Barsnes et al: Methods Mol Biol. 2011;694:237-53](http://www.ncbi.nlm.nih.gov/pubmed/21082439).
  * If you publish your data as part of a paper, please include the first reference above.
  * See the [How to Reference PRIDE Submissions](#how-to-reference-pride-submissions) section.

---

## About PRIDE Converter 

**PRIDE Converter** converts mass spectrometry data from most common data formats into valid PRIDE XML for submission to the publicly available [PRIDE database](http://www.ebi.ac.uk/pride). It presents a convenient, wizard-like graphical user interface, and includes efficient access to the Ontology Lookup Service ([OLS](http://www.ebi.ac.uk/ols)).

The currently supported formats are:

<table border='0'>
<blockquote><tr>
<blockquote><td width='200' height='30'><a href='http://www.matrixscience.com/'>Mascot Dat Files</a></td>
<td width='200' height='30'><a href='http://www.matrixscience.com/'>Mascot Generic Files</a></td>
<td width='200' height='30'><a href='http://www.thegpm.org/TANDEM/'>X!Tandem</a></td>
<td width='200' height='30'><a href='http://www.psidev.info/index.php?q=node/80#mzdata'>mzData</a> </td>
</blockquote></tr>
<tr>
<blockquote><td width='200' height='30'><a href='http://genesis.UGent.be/ms_lims'>ms_lims 7</a></td>
<td width='200' height='30'><a href='http://www.chem.agilent.com/Scripts/PDS.asp?lPage=7771'>Spectrum Mill</a></td>
<td width='200' height='30'><a href='http://www.matrixscience.com/help/data_file_help.html#QTOF'>Micromass PKL Files</a></td>
<td width='200' height='30'><a href='http://peptideprophet.sourceforge.net'>PeptideProphet</a>/<a href='http://proteinprophet.sourceforge.net'>ProteinProphet</a></td>
</blockquote></tr>
<tr>
<blockquote><td width='200' height='30'><a href='http://fields.scripps.edu/sequest/'>Sequest Result Files</a></td>
<td width='200' height='30'><a href='http://www.matrixscience.com/help/data_file_help.html#DTA'>Sequest DTA Files</a></td>
<td width='200' height='30'><a href='http://tools.proteomecenter.org/wiki/index.php?title=Formats:mzXML'>mzXML</a></td>
<td width='200' height='30'><a href='http://pubchem.ncbi.nlm.nih.gov/omssa/'>OMSSA</a></td>
</blockquote></tr>
<tr>
<blockquote><td width='200' height='30'><a href='http://personal.cicbiogune.es/rmatthiesen/'>VEMS PKX Files</a></td>
<td width='200' height='30'><a href='http://doi.wiley.com/10.1002/rcm.1603'>MS2 Files</a></td>
<td width='200' height='30'><a href='http://fields.scripps.edu/DTASelect'>DTASelect (beta)</a></td>
</blockquote></tr>
</table></blockquote>

Remarks:
  * Support for additional formats is in development.
  * Requests for additional formats can be sent to the [PRIDE team](#support).
  * Please note that not all of the supported formats are equally well documented. If you come across files that are of the supported formats, but for some reason fails to be converted, or if you detect errors with the conversion, please let us know so that we can try to fix the problem.

**PRIDE Converter** was developed by Harald Barsnes (when a PhD student at  [the University of Bergen](http://iieng.iportal.uib.no/)) under the guidance of  [Dr. Lennart Martens (EBI)](http://www.ebi.ac.uk/~lmartens/). As of July 2009 the development of **PRIDE Converter** was turned over to the PRIDE team at the [EBI](http://www.ebi.ac.uk). To contact the team see the [Support section](#support).

If you use **PRIDE Converter** to publish your data as part of a paper, please include a reference to the **PRIDE Converter** paper: [Barsnes et. al: Nat Biotechnol. 2009 Jul;27(7):598-9](http://www.nature.com/nbt/journal/v27/n7/full/nbt0709-598.html). See the [How to Reference PRIDE Submissions](#how-to-reference-pride-submissions) section.

[Go to top of page](#pride-converter)

---

## Getting PRIDE Converter

### Requirements

  * **PRIDE Converter** requires Java 1.5 (or above), which you can download for free [here](http://java.sun.com/javase/downloads/index.jsp). Most modern computers will have Java installed already, so first try to follow the instructions below to download and run **PRIDE Converter** before downloading Java. You only need the JRE version (and not the JDK version) to run **PRIDE Converter**.

  * Unless your data is stored in, or can be converted to, one of the supported data formats, the software will unfortunately not be of much help. We are however working on extending the list of supported data formats.

  * The current version has been tested on Windows XP, Windows Vista, Linux and Mac OS X, but should also work on other platforms. If you come across problems on your platform please let us know.

[Go to top of page](#pride-converter)

### Downloading

You can download the latest version [here](http://www.ebi.ac.uk/pride/resources/tools/converter/2.5.7/PRIDE_Converter_v2.5.7.zip). Unzipping the file, creates the following directory structure:

```
  PRIDE_Converter_vX.Y
     PRIDEConverter-X.Y.jar
     Properties
          Contacts
          Instruments
          Protocols
          Samples
     lib
```

To start the software, simply double-click the file named `PRIDEConverter-X.Y.jar`.
If this fails, try to download and install Java 1.5 or above, as explained in the previous section. (The program can also be started from the command line using the following command: `java -jar PRIDEConverter-X.Y.jar`.) A **PRIDE Converter** icon that can be used for shortcuts linking to the program is also included in the zip file.

At start up you will be given the choice of importing the settings from any previously installed versions of **PRIDE Converter**. Simply follow the instructions to import your old settings and lists of instruments, contacts etc. Upgrading does not delete your previous installation, but when you have verified that you can run the new version without problems, you can manually remove the old version by deleting the given folders.

[Go to top of page](#pride-converter)

### Troubleshooting

  * **Does Not Start** - If nothing happens when double clicking the **PRIDE Converter** jar file, the most likely cause is that you don't have Java installed. Download the latest version of Java  [here](http://java.sun.com/javase/downloads/index.jsp) and try again. (You only need the JRE version (and not the JDK version) to run **PRIDE Converter**.)

  * **Does Not Start II** - If **PRIDE Converter** fails during start-up and you get the "PRIDE Converter - Startup Failed" message, a file called `pride_converter.log` will be created in your home directory. In this file you will find detailed information about why the program was not able to start.

  * **Does Not Start III** - The most likely reason for the "PRIDE Converter - Startup Failed" message is that you have installed **PRIDE Converter** in a path containing special characters, i.e. `[`, `%`, æ, ø, å, etc. Move the converter to a different folder or rename the folder(s) causing the problem and try again. (On Linux **PRIDE Converter** also has to be run from a path not containing spaces).

  * **Does Not Start IV** - Another reason for the "PRIDE Converter - Startup Failed" message could be that you have set the upper memory limit higher than your computer can handle (see points below).

  * **General Error Diagnosis** - In the `Properties` folder (see section above for the folder structure), there is a file called `ErrorLog.txt`. This file contains transcripts of any errors that the application has encountered, and can be very useful in diagnosing your problem.

  * **Memory Issues I** - Big datasets can require a lot of memory. If the software unexpectedly fails on a big project, and the software mentions that it ran out of memory, you should try to give the program more memory. This can be done by editing the `JavaOptions.txt` file in the `Properties` folder (see section above for the folder structure). In this file, change the `-Xmx768M` option to a higher number (e.g., `-Xmx1500M` for a maximum of appr. 1.5GB of memory). Please note that on a 32-bit operating system you can not increase this value beyond 2000M. If increasing the memory settings does not solve your problem, or you don't enough memory available, e-mail a support request to the PRIDE team at the EBI: `pride-support at ebi.ac.uk` (replace `at` with `@`).

  * **Memory Issues II** - Another memory related issue that can occur in rare cases is that the default Java stack size memory is not set high enough for a given dataset. If this happens an exception will be thrown and an error (java.lang.StackOverflowError) can be found in the `ErrorLog.txt` file. The solution is to increase the default stack size, which is done in the same manner as the point above, but this time by increasing the `-Xss1M` and `-Xoss1M` parameters. Try doubling both of the values until the problem disappears.

  * **Memory Issues III** - If you get an out of memory error after using the converter to create multiple PRIDE XML files, it might help restarting the converter and try converting the last file again. If the problem persists try increasing the provided memory size as explained above.

  * **Memory Issues IV** - In some rare cases increasing the maximum memory size may result in the converter not starting, even though you have more than enough memory available. (Please note that on a 32-bit operating system you cannot increase the maximum memory beyond 2000M.) If you are using Windows it may help to increase the available virtual memory, but this is not recommended unless all other options have been tried. For Windows XP: Control Panel -> System -> Advanced Tab -> Performance Panel, Settings -> Advanced Tab -> Virtual memory, Change.

  * **Internet Connection** - If you have problems connecting to the Ontology Lookup Service, first make sure that you are connected to the internet. Then check your firewall (and proxy) settings to see if they prevent **PRIDE Converter** from accessing the internet. If you are using a proxy server you need to add the proxy settings to the `JavaOptions.txt` file in the `Properties` folder (see section above for the folder structure). Add: "-Dhttp.proxyHost=my.proxy.domain.com" and "-Dhttp.proxyPort=3128" to the end of this file (on two separate lines). Replace the name of the proxy host (and the proxy port if necessary), save the `JavaOptions.txt` file and start the converter again. If this does not solve your problem, or you are not using a proxy server, you (or your IT department) has to allow HTTP POST connections to the following URL: http://www.ebi.ac.uk/ontology-lookup/services/OntologyQuery.

  * **Miscellaneous** - A rare issue/bug is that the converter sometimes (for unknown reasons and very rarely) halts after clicking the `Convert!` button in the last step. If this happens simply cancel the conversion by closing the progress dialog and restart the conversion. If the problem persists, try going back one step in the converter and then forward again before restarting the conversion.

  * **Problem Not Solved? Or Problem Not In List Above?** - See [Support](#support).

[Go to top of page](#pride-converter)

---

## Upgrading 

When a new version of **PRIDE Converter** becomes available, existing users will be notified of this the next time they start the converter. Upgrading is done by downloading the latest version from the **PRIDE Converter** web pages, unzipping the file in a (preferably) empty folder, and then double clicking on the `PRIDE_Converter_vX.Y.jar` file. You will then get the chance to import your settings (instruments, contacts, etc) from the previously installed version of the converter. Simply follow the instructions to import all your old settings. Upgrading does not delete the older version of **PRIDE Converter**. But after making sure that the new version is working, you can safely delete the folder containing the old version.

Installing the new version in the same folder as the previously installed version can result in problems and is not recommended.

A detailed overview of the changes between each **PRIDE Converter** version can be found in the [Release Notes](http://code.google.com/p/pride-converter/wiki/ReleaseNotes).

[Go to top of page](#pride-converter)

---

## How to Submit Data to PRIDE 

Once the PRIDE XML file is created, there are currently two ways of submitting it to PRIDE:

  * Direct submission via the [PRIDE web page](http://www.ebi.ac.uk/pride): this submission path is only suitable for small submissions (files of up to 15 MB unzipped, corresponding to about 5 MB for zipped files). In order to make the submission, users will have to be [registered in the PRIDE system](http://www.ebi.ac.uk/pride/startRegistration.do) and log-in, at which point a “Submit Data” option is made available in the left menu on the [PRIDE website](http://www.ebi.ac.uk/pride).
  * For larger files, users are advised to first contact the PRIDE team at `pride-support at ebi.ac.uk` (replace `at` with `@`). A curator will then create a private directory for the user in the EBI FTP server and the user will be able to upload their files there confidentially.

The restrictions on using the web interface are solely the result of potential problems in the HTTP protocol when uploading large files, as file size limitations are essentially non-existent for submission purposes.

[Go to top of page](#pride-converter)

---

## How to Reference PRIDE Submissions

Referencing PRIDE submissions in manuscripts:

  * If still under some form of review:
    * `The data is available in the PRIDE database [ref Vizcaíno et al 2010 (PMID: 19906717)] (www.ebi.ac.uk/pride) under accession numbers X and Y (username: reviewXYZ, password: ABC). The data was converted using PRIDE Converter [ref Barsnes et al 2009 (PMID: 19587657)] (http://pride-converter.googlecode.com).`
  * If at galley proof stage:
    * `The data is available in the PRIDE database [ref Vizcaíno et al 2010 (PMID: 19906717)] (www.ebi.ac.uk/pride) under accession numbers X and Y. The data was converted using PRIDE Converter [ref Barsnes et al 2009 (PMID: 19587657)] (http://pride-converter.googlecode.com).`

Remember to notify the PRIDE team when your paper has been accepted, so that private data (if any) can be made public.

[Go to top of page](#pride-converter)

---

## Getting Help

For questions or additional help, please contact the authors or e-mail a support request to the PRIDE team at the EBI: `pride-support at ebi.ac.uk` (replace `at` with `@`). If appropriate please include a (preferably zipped) copy of the `ErrorLog.txt` file from the `Properties` folder (see section above for the folder structure).

[Go to top of page](#pride-converter)

---

## Source Code

PRIDE Converter has mainly been developed using [NetBeans](http://www.netbeans.org), and is built and deployed using [Maven](http://maven.apache.org/).

**PRIDE Converter** uses code from 10 other projects:
  * [PRIDE - PRoteomics IDEntifications database](http://www.ebi.ac.uk/pride)
  * [ms\_lims - mass spectrometry laboratory information management system](http://genesis.UGent.be/ms_lims)
  * [OLS - the Ontology Lookup Service](http://www.ebi.ac.uk/ols)
  * [OLS Dialog](http://code.google.com/p/ols-dialog)
  * [MascotDatfile Library](http://genesis.ugent.be/MascotDatfile/)
  * [i-Tracker](http://www.cranfield.ac.uk/health/researchareas/bioinformatics/page6801.jsp) (translated from PERL to Java)
  * [JGoodies - Looks](http://www.jgoodies.com/)
  * [JRAP](http://tools.proteomecenter.org/wiki/index.php?title=Software:JRAP)
  * [OMSSA Parser](http://code.google.com/p/omssa-parser/)
  * [SwingLabs SwingX](https://swingx.dev.java.net/)

[Go to top of page](#PRIDE_Converter.md)

---

## Screenshots 

(Click on a screenshot to see the full size version)

[http://pride-converter.googlecode.com/svn/wiki/images/screenshots/dataSourceSelection\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/dataSourceSelection.PNG) [http://pride-converter.googlecode.com/svn/wiki/images/screenshots/dataFileSelection\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/dataFileSelection.PNG) [http://pride-converter.googlecode.com/svn/wiki/images/screenshots/spectraSelection\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/spectraSelection.PNG)
[http://pride-converter.googlecode.com/svn/wiki/images/screenshots/experimentProperties\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/experimentProperties.PNG)
[http://pride-converter.googlecode.com/svn/wiki/images/screenshots/sampleDetails\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/sampleDetails.PNG)
[http://pride-converter.googlecode.com/svn/wiki/images/screenshots/protocolDetails\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/protocolDetails.PNG)
[http://pride-converter.googlecode.com/svn/wiki/images/screenshots/instrumentAndProcessing\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/instrumentAndProcessing.PNG) [http://pride-converter.googlecode.com/svn/wiki/images/screenshots/userParameters\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/userParameters.PNG) [http://pride-converter.googlecode.com/svn/wiki/images/screenshots/outputDetails\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/outputDetails.PNG) [http://pride-converter.googlecode.com/svn/wiki/images/screenshots/olsDialog\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/olsDialog.PNG) [http://pride-converter.googlecode.com/svn/wiki/images/screenshots/modificationMapping\_small.PNG](http://pride-converter.googlecode.com/svn/wiki/images/screenshots/modificationMapping.PNG)

[Go to top of page](#pride-converter)
