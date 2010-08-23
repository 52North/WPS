/***************************************************************
This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework
is extensible in terms of processes and data handlers. It is compliant
to the WPS version 0.4.0 (OGC 05-007r4).

Copyright (C) 2007 by con terra GmbH

Authors:
Florian van Keulen, ITC Student, ITC Enschede, the Netherlands


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program (see gnu-gpl v2.txt); if not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA or visit the web page of the Free
Software Foundation, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.webadmin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.n52.wps.commons.WPSConfig;

/**
 * This Bean handles the fileupload of the xml configuration file
 * @author Florian van Keulen
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
        String realSavePath = "";
        while (i != -1) {
            String newLine = new String(line, 0, i);
            System.out.println(newLine);
            if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
            	if(newLine.contains("uploadProcess")){
            		//we upload files not config docuemnts. Therefore store is somewhere else.
            		realSavePath = new File(savePath).getParentFile().getAbsolutePath()+"/WEB-INF/classes/";
            	}
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
                  
                    PrintWriter pw = null;
                    if(realSavePath.length()>0){
                    	pw = new PrintWriter(new BufferedWriter(new FileWriter((realSavePath == null ? "" : realSavePath) + filename)));
                    }else{
                    	filename = filenamePrefix + filename;
                    	pw = new PrintWriter(new BufferedWriter(new FileWriter((savePath == null ? "" : savePath) + filename)));	
                    }
                    
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
        if(realSavePath.length()>0){
        	LOGGER.info("User Configuration file received and saved at: " + realSavePath + filename);
        }else{
        	LOGGER.info("User Configuration file received and saved at: " + savePath + filename);
        }
    }
    
  
}
