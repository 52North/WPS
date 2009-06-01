/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wps.webAdmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.n52.wps.commons.WPSConfig;
import org.apache.log4j.Logger;

/**
 *
 * @author fvk
 */
public class ConfigUploadBean {
    private static transient Logger LOGGER = Logger.getLogger(ConfigUploadBean.class);

    private String savePath,  filepath,  filename;
    private final String filenamePrefix = "userConf_";

    public String getFilenamePrefix(){
        return filenamePrefix;
    }

    private void setFilename(String s) {
        if (s == null) {
            return;
        }

        int pos = s.indexOf("filename=\"");
        if (pos != -1) {
            filepath = s.substring(pos + 10, s.length() - 1);
            // Windows browsers include the full path on the client
            // But Linux/Unix and Mac browsers only send the filename
            // test if this is from a Windows browser
            pos = filepath.lastIndexOf("\\");
            if (pos != -1) {
                filename = filepath.substring(pos + 1);
            } else {
                filename = filepath;
            }
        }
    }


    public void doUpload(HttpServletRequest request) throws IOException {
        savePath = WPSConfig.getConfigPath();
        // get rid of the filename
        // How is the path on a windows machine? may be better using:
        // savePath = savePath.substring(0, savePath.length() - "wps_config.xml".length());
        savePath = savePath.substring(0,savePath.lastIndexOf("/")+1);
        ServletInputStream in = request.getInputStream();

        byte[] line = new byte[128];
        int i = in.readLine(line, 0, 128);
        if (i < 3) {
            return;
        }
        int boundaryLength = i - 2;

        String boundary = new String(line, 0, boundaryLength); //-2 discards the newline character

        while (i != -1) {
            String newLine = new String(line, 0, i);
            if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
                if (newLine.indexOf("filename=\"") != -1) {
                    setFilename(new String(line, 0, i - 2));
                    if (filename == null) {
                        return;
                    }
                    //this is the file content
                    i = in.readLine(line, 0, 128);
                    //next line
                    i = in.readLine(line, 0, 128);
                    // blank line
                    i = in.readLine(line, 0, 128);
                    newLine = new String(line, 0, i);
                    // add the prefix to the filename
                    filename = filenamePrefix + filename;
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter((savePath == null ? "" : savePath) + filename)));
                    while (i != -1 && !newLine.startsWith(boundary)) {
                        // the problem is the last line of the file content
                        // contains the new line character.
                        // So, we need to check if the current line is
                        // the last line.
                        i = in.readLine(line, 0, 128);
                        if ((i == boundaryLength + 2 || i == boundaryLength + 4) // + 4 is eof
                                && (new String(line, 0, i).startsWith(boundary))) {
                            pw.print(newLine.substring(0, newLine.length() - 2));
                        } else {
                            pw.print(newLine);
                        }
                        newLine = new String(line, 0, i);
                    }
                    pw.close();
                }
            }
            i = in.readLine(line, 0, 128);
        } // end while
        LOGGER.info("User Configuration file received and saved at: " + savePath + filename);
    }
}
