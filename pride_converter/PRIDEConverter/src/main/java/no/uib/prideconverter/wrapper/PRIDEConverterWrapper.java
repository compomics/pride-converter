package no.uib.prideconverter.wrapper;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A wrapper class used to start the jar file with parameters. The parameters 
 * are read from the JavaOptions file in the Properties folder.
 * 
 * @author  Harald Barsnes
 * 
 * Created October 2005
 * Revised March 2008
 */
public class PRIDEConverterWrapper {

    private boolean debug = false;
    private String jarFileName;
    private String prideConverterSourceJarFileName;

    /**
     * Starts the launcher by calling the launch method. Use this as the 
     * main class in the jar file.
     */
    public PRIDEConverterWrapper() {
        String versionEnding = getVersion() + ".jar";
        jarFileName = "PRIDEConverter-" + versionEnding;
        prideConverterSourceJarFileName = "prideconverter-core-" + versionEnding;

        try {
            PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // ignore exception, i.e. use default look and feel
        }

        try {
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getVersion() {

        Properties p = new Properties();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("prideconverter.properties");
            p.load( is );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("converter.version");
    }

    /**
     * Launches the jar file with parameters to the jvm.
     * 
     * @throws java.lang.Exception
     */
    private void launch() throws Exception {
                                                          
        boolean error = false;

        String temp = "", cmdLine, path;

        //test for java version. works for 1.4 and newer
        String javaVersion = System.getProperty("java.version");
        StringTokenizer tok = new StringTokenizer(javaVersion, ".");

        if (new Integer(tok.nextToken()).intValue() >= 1 &&
                new Integer(tok.nextToken()).intValue() >= 5) {
            //ok, do nothing
            if (debug) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Java " + javaVersion + " detected.",
                        "Java Version",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } else {

            int option = javax.swing.JOptionPane.showConfirmDialog(null,
                    "You are using Java " + javaVersion + ".\n\n" +
                    "PRIDE Converter requires Java 1.5 or newer.\n\n" +
                    "Do you want to upgrade your Java version?",
                    "PRIDE Converter - Java Version Test",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                BrowserControl.displayURL("http://java.sun.com/javase/downloads/");
                System.exit(0);
            } else {
                System.exit(0);
            }

            error = true;
        }

        if (!error) {

            path = this.getClass().getResource("PRIDEConverterWrapper.class").getPath();
            path = path.substring(5, path.indexOf(jarFileName));
            path = path.replace("%20", " ");

            File javaOptions = new File(path + "Properties/JavaOptions.txt");

            String options = "", currentOption;

            ArrayList javaOptionsAsList = new ArrayList();

            if (javaOptions.exists()) {

                try {
                    FileReader f = new FileReader(javaOptions);
                    BufferedReader b = new BufferedReader(f);

                    currentOption = b.readLine();

                    while (currentOption != null) {
                        if (!currentOption.startsWith("#")) {
                            options += currentOption + " ";
                            javaOptionsAsList.add(currentOption.trim());
                        }

                        currentOption = b.readLine();
                    }

                    b.close();
                    f.close();

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                // options find not found. using default memory settings
                options = "-Xms128M -Xmx768M";
                javaOptionsAsList.add("-Xms128M");
                javaOptionsAsList.add("-Xmx768M");
            }

            path = path + "lib" + File.separator;

            File tempFile = new File(path);

            String javaHome = System.getProperty("java.home") + File.separator +
                    "bin" + File.separator;

            String quote = "";

            if(System.getProperty("os.name").lastIndexOf("Windows") != -1){
                quote = "\"";
            }

            cmdLine = javaHome + "java " + options + " -jar " 
                    + quote + new File(tempFile, prideConverterSourceJarFileName).getAbsolutePath() + quote;

            if (debug) {
                System.out.println(cmdLine);
            }

            try {
                // create the command line
                ArrayList process_name_array = new ArrayList();
                process_name_array.add(javaHome + "java");

                // add the options
                for (int i=0; i < javaOptionsAsList.size(); i++) {
                    process_name_array.add(javaOptionsAsList.get(i));
                }

                process_name_array.add("-jar");
                process_name_array.add(new File(tempFile, prideConverterSourceJarFileName).getAbsolutePath());
                ProcessBuilder pb = new ProcessBuilder(process_name_array);

                // run the command line
                Process p = pb.start();

                InputStream stderr = p.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                temp += "<ERROR>\n\n";

                if (debug) {
                    System.out.println("<ERROR>");
                }

                line = br.readLine();

                error = false;

                while (line != null) {

                    if (debug) {
                        System.out.println(line);
                    }

                    temp += line + "\n";
                    line = br.readLine();
                    error = true;
                }

                if (debug) {
                    System.out.println("</ERROR>");
                }

                temp += "\nThe command line executed:\n";
                temp += cmdLine + "\n";
                temp += "\n</ERROR>\n";
                int exitVal = p.waitFor();

                if (debug) {
                    System.out.println("Process exitValue: " + exitVal);
                }

                if (error) {

                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Failed to start PRIDE Converter.\n\n" +
                            "Make sure that PRIDE Converter is installed in a path not containing\n" +
                            "special characters. On Linux it has to be run from a path without spaces.\n\n" +
                            "The upper memory limit used may be too high for your computer to handle.\n" +
                            "Try reducing it and see if this helps.\n\n" +
                            "For more details see:\n" +
                            System.getProperty("user.home") +
                            File.separator + "pride_converter.log\n\n" +
                            "Or see \'Troubleshooting\' at http://pride-converter.googlecode.com",
                            "PRIDE Converter - Startup Failed", JOptionPane.OK_OPTION);

                    File logFile = new File(System.getProperty("user.home") +
                            File.separator + "pride_converter.log");

                    FileWriter f = new FileWriter(logFile);
                    f.write(temp);
                    f.close();

                    System.exit(0);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the 
     * main class in the jar file.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        new PRIDEConverterWrapper();
    }
}
