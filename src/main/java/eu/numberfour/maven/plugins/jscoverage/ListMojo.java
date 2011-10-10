package eu.numberfour.maven.plugins.jscoverage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * Creates a JSON formatted list of files form a base directory, and optional includes and excludes.
 * </p>
 * 
 * @author <a href="mailto:leonard.ehrenfried@web.de">Leonard Ehrenfried</a>
 * 
 * @goal list
 * @requiresDependencyResolution compile
 * @description Creates a JSON-formatted list of files
 */
public class ListMojo extends AbstractMojo {

    public static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * The Maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * File into which to save the output of the transformation.
     * 
     * @parameter default-value="${basedir}/target/file-list.json"
     */
    private String outputFile;
    /**
     * Base directory of the scanning process
     * 
     * @parameter default-value="${basedir}/target/"
     */
    public String baseDir;
    /**
     * Ant-style include pattern.
     * 
     * For example **.* is all files
     * 
     * @parameter
     */
    public String[] includes;
    /**
     * Ant-style exclude pattern.
     * 
     * For example **.* is all files
     * 
     * @parameter
     */
    public String[] excludes;

    /**
     * @parameter default-value="json"
     */
    public String type;

    /**
     * Whether to ignore case
     * 
     * @parameter
     */
    public boolean caseSensitive;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        FileWriter fileWriter = null;
        try {

            Log log = getLog();
            log.info("");
            log.info("Creating file list ");
            log.info("Basedir:  " + this.baseDir);
            log.info("Output:   " + this.outputFile);
            log.info("Includes: " + Arrays.toString(this.includes));
            log.info("Exludes:  " + Arrays.toString(this.excludes));
            log.info("Type:  " + this.type);

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(this.baseDir);
            scanner.setIncludes(this.includes);
            scanner.setExcludes(this.excludes);
            scanner.setCaseSensitive(this.caseSensitive);
            scanner.scan();

            String[] includedFiles = scanner.getIncludedFiles();

            log.info("File list contains " + includedFiles.length + " files");

            fileWriter = new FileWriter(this.outputFile);

            if ("json".equals(this.type)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(includedFiles);
                fileWriter.write(json);
            } else if ("junit".equals(this.type)) {
                StringBuilder suiteContent = new StringBuilder();
                suiteContent.append("package n4.quat.selenium.acceptancetest.suites;");
                suiteContent.append(NEW_LINE);
                suiteContent.append(NEW_LINE);
                suiteContent.append("import org.junit.runner.RunWith;");
                suiteContent.append(NEW_LINE);
                suiteContent.append("import org.junit.runners.Suite.SuiteClasses;");
                suiteContent.append(NEW_LINE);
                suiteContent.append(NEW_LINE);
                suiteContent.append("@RunWith(org.junit.runners.Suite.class)");
                suiteContent.append(NEW_LINE);
                suiteContent.append("@SuiteClasses( {");
                suiteContent.append(NEW_LINE);
                boolean firstTime = true;
                for (String string : includedFiles) {
                    // suiteContent.append("\t");
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        suiteContent.append(",");
                    }
                    suiteContent.append(string.replaceAll("/", ".").replaceAll("java", "class"));
                    suiteContent.append(NEW_LINE);
                }
                suiteContent.append("} )");
                suiteContent.append(NEW_LINE);
                suiteContent.append("public class AllTestsSuite { }");
                suiteContent.append(NEW_LINE);
                fileWriter.write(suiteContent.toString());
            }

        } catch (IOException ex) {
            throw new MojoFailureException("Could not write output file.");
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ListMojo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
