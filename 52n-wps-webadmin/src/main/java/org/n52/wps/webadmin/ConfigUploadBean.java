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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;

/**
 * This Bean handles the fileupload of the xml configuration file
 * 
 * @author Florian van Keulen
 */
public class ConfigUploadBean {
    private static transient Logger LOGGER = LoggerFactory.getLogger(ConfigUploadBean.class);

    private String savePath, filepath, filename, xmlFilepath, xmlFilename, realSavePath, boundary;

    private final String filenamePrefix = "userConf_";

    private int i, boundaryLength;

    private byte[] line;

    public String getFilenamePrefix()
    {
        return filenamePrefix;
    }

    private void setFilename(String s)
    {
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

    private void setXMLFilename(String s)
    {
        if (s == null) {
            return;
        }

        int pos = s.indexOf("filename=\"");
        if (pos != -1) {
            xmlFilepath = s.substring(pos + 10, s.length() - 1);
            // Windows browsers include the full path on the client
            // But Linux/Unix and Mac browsers only send the filename
            // test if this is from a Windows browser
            pos = xmlFilepath.lastIndexOf("\\");
            if (pos != -1) {
                xmlFilename = xmlFilepath.substring(pos + 1);
            } else {
                xmlFilename = xmlFilepath;
            }
        }
    }

    public void doUpload(HttpServletRequest request) throws IOException
    {
        savePath = WPSConfig.getConfigPath();
        // get rid of the filename
        // How is the path on a windows machine? may be better using:
        // savePath = savePath.substring(0, savePath.length() -
        // "wps_config.xml".length());
        savePath = savePath.substring(0, savePath.lastIndexOf(File.separator) + 1);
        ServletInputStream in = request.getInputStream();

        line = new byte[128];
        i = in.readLine(line, 0, 128);
        if (i < 3) {
            return;
        }
        boundaryLength = i - 2;

        int classnameupcoming = 4;

        boundary = new String(line, 0, boundaryLength); // -2 discards
        // the newline
        // character

        /*
         * the type of the uploaded file until now only processes (.java/.zip)
         * or R scripts are supported
         */
        String type = "";

        String realSavePath = "";
        while (i != -1) {
            String newLine = new String(line, 0, i);
            System.out.println(newLine);
            if (newLine.contains("processName")) {
                type = "javaProcess";
                // we set a flag to know that the next string is the fully
                // qualified classname
                classnameupcoming--;
            } else if (newLine.contains("rProcessName")) {
                type = "rScript";
                // we set a flag to know that the next string is the fully
                // qualified classname
                classnameupcoming--;

            }
            if (classnameupcoming < 4) {
                classnameupcoming--;
            }
            if (classnameupcoming == 0) {
                // // then parse classname
                // String classname = newLine;
                // String[] classnameArr = classname.replace(".",
                // ",").split(",");
                // String classdir = new String();
                // for (int c = 0; c < classnameArr.length - 1; c++) {
                // if (c == 0) {
                // classdir = classdir + classnameArr[c];
                // } else {
                // classdir = classdir + File.separator + classnameArr[c];
                // }
                //
                // }
                // realSavePath = new File(savePath).getParentFile()
                // + "/WEB-INF/classes/";
                // realSavePath = realSavePath + classdir + File.separator;
                // new File(realSavePath).mkdirs();

                try {
                    handleUpload(in, type, newLine);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
                if (newLine.contains("processDescriptionFile")) {
                    if (newLine.indexOf("filename=\"") != -1) {
                        setXMLFilename(new String(line, 0, i - 2));
                        if (xmlFilename == null) {
                            return;
                        }

                        if (!xmlFilename.equals("")) {

                            // this is the file content
                            i = in.readLine(line, 0, 128);
                            // next line
                            i = in.readLine(line, 0, 128);
                            // blank line
                            i = in.readLine(line, 0, 128);
                            newLine = new String(line, 0, i);
                            // add the prefix to the filename

                            PrintWriter pw = null;
                            if (realSavePath.length() > 0) {
                                pw = new PrintWriter(new BufferedWriter(new FileWriter((realSavePath == null ? "" : realSavePath) + xmlFilename)));
                            } else {
                                xmlFilename = filenamePrefix + xmlFilename;
                                pw = new PrintWriter(new BufferedWriter(new FileWriter((savePath == null ? "" : savePath) + filename)));
                            }

                            while (i != -1 && !newLine.startsWith(boundary)) {
                                // the problem is the last line of the file
                                // content
                                // contains the new line character.
                                // So, we need to check if the current line is
                                // the last line.
                                i = in.readLine(line, 0, 128);
                                if ((i == boundaryLength + 2 || i == boundaryLength + 4) // +
                                        // 4
                                        // is
                                        // eof
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

                }

                if (newLine.contains("processFile")) {
                    // we upload files not config docuemnts. Therefore store is
                    // somewhere else.
                    // realSavePath = new
                    // File(savePath).getParentFile().getAbsolutePath()+"/WEB-INF/classes/";

                    /**
                     * TODO: dir structure according to fully qual classname
                     */

                    if (newLine.indexOf("filename=\"") != -1) {
                        setFilename(new String(line, 0, i - 2));
                        if (filename == null) {
                            return;
                        }

                        // this is the file content
                        i = in.readLine(line, 0, 128);
                        // next line
                        i = in.readLine(line, 0, 128);
                        // blank line
                        i = in.readLine(line, 0, 128);
                        newLine = new String(line, 0, i);
                        // add the prefix to the filename

                        PrintWriter pw = null;
                        if (realSavePath.length() > 0) {
                            pw = new PrintWriter(new BufferedWriter(new FileWriter((realSavePath == null ? "" : realSavePath) + filename)));
                        } else {
                            filename = filenamePrefix + filename;
                            pw = new PrintWriter(new BufferedWriter(new FileWriter((savePath == null ? "" : savePath) + filename)));
                        }

                        while (i != -1 && !newLine.startsWith(boundary)) {
                            // the problem is the last line of the file content
                            // contains the new line character.
                            // So, we need to check if the current line is
                            // the last line.
                            i = in.readLine(line, 0, 128);
                            if ((i == boundaryLength + 2 || i == boundaryLength + 4) // +
                                    // 4
                                    // is
                                    // eof
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

            }

            i = in.readLine(line, 0, 128);
        } // end while
        // if (realSavePath.length() > 0) {
        // LOGGER.info("User Configuration file received and saved at: "
        // + realSavePath + filename);
        // } else {
        // LOGGER.info("User Configuration file received and saved at: "
        // + savePath + filename);
        // }
    }

    private void handleUpload(ServletInputStream in,
            String type,
            String newLine) throws Exception
    {

        if (type.equals("javaProcess")) {

            // then parse classname
            String classname = newLine;
            String[] classnameArr = classname.replace(".", ",").split(",");
            String classdir = new String();
            for (int c = 0; c < classnameArr.length - 1; c++) {
                if (c == 0) {
                    classdir = classdir + classnameArr[c];
                } else {
                    classdir = classdir + File.separator + classnameArr[c];
                }

            }
            realSavePath = new File(savePath).getParentFile() + "/WEB-INF/classes/";
            realSavePath = realSavePath + classdir + File.separator;
            new File(realSavePath).mkdirs();

            while (i != -1) {

                newLine = new String(line, 0, i);
                System.out.println(newLine);

                if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
                    if (newLine.contains("processFile")) {
                        // we upload files not config docuements. Therefore
                        // store is
                        // somewhere else.
                        // realSavePath = new
                        // File(savePath).getParentFile().getAbsolutePath()+"/WEB-INF/classes/";

                        /**
                         * TODO: dir structure according to fully qual classname
                         */

                        if (newLine.indexOf("filename=\"") != -1) {
                            setFilename(new String(line, 0, i - 2));
                            if (filename == null) {
                                return;
                            }

                            // this is the file content
                            i = in.readLine(line, 0, 128);
                            // next line
                            i = in.readLine(line, 0, 128);
                            // blank line
                            i = in.readLine(line, 0, 128);
                            newLine = new String(line, 0, i);
                            // add the prefix to the filename

                            PrintWriter pw = null;
                            if (realSavePath.length() > 0) {
                                pw = new PrintWriter(new BufferedWriter(new FileWriter((realSavePath == null ? "" : realSavePath) + filename)));
                            } else {
                                filename = filenamePrefix + filename;
                                pw = new PrintWriter(new BufferedWriter(new FileWriter((savePath == null ? "" : savePath) + filename)));
                            }

                            while (i != -1 && !newLine.startsWith(boundary)) {
                                // the problem is the last line of the file
                                // content
                                // contains the new line character.
                                // So, we need to check if the current line is
                                // the last line.
                                i = in.readLine(line, 0, 128);
                                if ((i == boundaryLength + 2 || i == boundaryLength + 4) // +
                                        // 4
                                        // is
                                        // eof
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

                }

                i = in.readLine(line, 0, 128);
            } // end while
            if (realSavePath.length() > 0) {
                LOGGER.info("User Configuration file received and saved at: " + realSavePath + filename);
            } else {
                LOGGER.info("User Configuration file received and saved at: " + savePath + filename);
            }
            if (realSavePath.length() > 0) {
                compile(realSavePath + filename);
            }

        } else if (type.equals("rScript")) {
            // if processName the filename is used as ID, otherwise the script will be
            // renamed because r process IDs are derived from the filenames
            String processName = newLine.trim();

            /*
             * FIXME This is very dirty. Changed from staticly referencing the
             * 52n-wps-r module (!!) to generic string property resolving.
             * Nevertheless, this is sort of hard-coded. The webadmin module
             * should be completely separated from others.
             */
            String scriptDir = "";
            Property[] rConfig = WPSConfig.getInstance().getPropertiesForRepositoryClass("org.n52.wps.server.r.LocalRAlgorithmRepository");
            for (Property property : rConfig) {
                if (property.getName().equalsIgnoreCase("Script_Dir")) {
                    scriptDir = property.getStringValue();
                }
            }

            File basedir = new File(savePath).getParentFile();
            realSavePath = new File(basedir, scriptDir).getAbsolutePath()+File.separator;
            new File(realSavePath).mkdirs();

            while (i != -1) {

                newLine = new String(line, 0, i);
                System.out.println(newLine);

                if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
                    if (newLine.contains("rProcessFile")) {

                        if (newLine.indexOf("filename=\"") != -1) {

                            // filename is either the process name or the
                            // original file name
                            if (processName != null && !processName.isEmpty()) {
                                filename = processName + ".R";
                            } else
                                setFilename(new String(line, 0, i - 2));

                            if (filename == null) {
                                return;
                            }

                            // this is the file content
                            i = in.readLine(line, 0, 128);
                            // next line
                            i = in.readLine(line, 0, 128);
                            // blank line
                            i = in.readLine(line, 0, 128);
                            newLine = new String(line, 0, i);
                            // add the prefix to the filename

                            PrintWriter pw = null;
                            File destFile = null;
                            if (realSavePath.length() > 0) {
                            	destFile = new File((realSavePath == null ? "" : realSavePath) + filename);
                                pw = new PrintWriter(new BufferedWriter(new FileWriter(destFile)));
                            } else {
                                filename = filenamePrefix + filename;
                                destFile = new File((savePath == null ? "" : savePath) + filename);
                                pw = new PrintWriter(new BufferedWriter(new FileWriter(destFile)));
                            }

                            while (i != -1 && !newLine.startsWith(boundary)) {
                                // the problem is the last line of the file
                                // content
                                // contains the new line character.
                                // So, we need to check if the current line is
                                // the last line.
                                i = in.readLine(line, 0, 128);
                                if ((i == boundaryLength + 2 || i == boundaryLength + 4) // +
                                        // 4
                                        // is
                                        // eof
                                        && (new String(line, 0, i).startsWith(boundary))) {

                                    pw.print(newLine.substring(0, newLine.length() - 2));
                                } else {
                                    pw.print(newLine);
                                }

                                newLine = new String(line, 0, i);
                            }
                            pw.close();

                        }

                        /*
                         * FIXME This is very dirty. The webadmin module should
                         * be completely separated from others. This one points
                         * out the need for a generic
                         * "reload your configuration" convenience method.
                         * Changed to java Reflections to remove the static
                         * 52n-wps-r module reference (though, very hardcoded).
                         */
                        {
                            LOGGER.info("R script uploaded, call to RPropertyChangeManager");
                            Class<?> clazz = Class.forName("org.n52.wps.server.r.RPropertyChangeManager");
                            Method method = clazz.getMethod("getInstance", null);
                            Object instance = method.invoke(null, null);
                            Method method2 = instance.getClass().getMethod("updateRepositoryConfiguration", null);
                            method2.invoke(instance, null);
                            // RPropertyChangeManager rmanager =
                            // RPropertyChangeManager.getInstance();
                            // LOGGER.info("R script uploaded, call to RPropertyChangeManager");
                            // rmanager.addUnregisteredScripts();
                        }

                    }

                }

                i = in.readLine(line, 0, 128);
            } // end while
            if (realSavePath.length() > 0) {
                LOGGER.info("User Configuration file received and saved at: " + realSavePath + filename);
            } else {
                LOGGER.info("User Configuration file received and saved at: " + savePath + filename);
            }
            if (realSavePath.length() > 0) {
            }
        }

    }

    public void compile(String fileName)
    {
        ClassLoader cl = this.getClass().getClassLoader();
        List<URL> classpath = new ArrayList<URL>();
        if (cl instanceof URLClassLoader) {
            URLClassLoader cl2 = (URLClassLoader) cl;
            for (URL jar : cl2.getURLs()) {
                classpath.add(jar);
            }
        }
        String classPath = System.getProperty("java.class.path");
        for (String path : classPath.split(File.pathSeparator)) {
            try {
                classpath.add(new URL("file:" + path));
            } catch (MalformedURLException e) {
                System.err.println("Wrong url: " + e.getMessage());
                e.printStackTrace();
            }
        }

        StringBuffer sb = new StringBuffer();
        for (URL jar : classpath) {
            if (SystemUtils.IS_OS_WINDOWS == false) {
                sb.append(jar.getPath());
                sb.append(File.pathSeparatorChar);
            } else {
                sb.append(jar.getPath().substring(1));
                sb.append(File.pathSeparatorChar);
            }
        }
        String ops[] = new String[] { "-classpath", sb.toString() };

        List<String> opsIter = new ArrayList<String>();
        try {
            for (String s : ops) {
                // XXX test usage, removed use of deprecated method
                // ((ArrayList) opsIter).add(URLDecoder.decode(s));
                ((ArrayList) opsIter).add(URLDecoder.decode(s, Charset.forName("UTF-8").toString()));
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage(), e);
        }

        File[] files1 = new File[1];
        files1[0] = new File(fileName);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files1));

        compiler.getTask(null, fileManager, null, opsIter, null, compilationUnits1).call();

        try {
            fileManager.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
