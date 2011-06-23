package eu.numberfour.maven.plugins.jscoverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * <p>
 * Creates a JSON formatted list of files form a base directory, and optional 
 * includes and excludes.
 * </p>
 * 
 * @author <a href="mailto:leonard.ehrenfried@web.de">Leonard Ehrenfried</a>
 * 
 * @goal list
 * @requiresDependencyResolution compile
 * @description Creates a JSON-formatted list of files
 */
public class ListMojo extends AbstractMojo {

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        FileWriter fileWriter = null;
        try {

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(baseDir);
            scanner.setIncludes(includes);
            scanner.setExcludes(excludes);
            scanner.scan();

            String[] includedFiles = scanner.getIncludedFiles();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(includedFiles);
            System.out.println(json);

            fileWriter = new FileWriter(outputFile);
            fileWriter.write(json);

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
