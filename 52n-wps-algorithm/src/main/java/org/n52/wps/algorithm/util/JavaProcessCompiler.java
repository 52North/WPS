/**
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.algorithm.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for compiling Java processes.
 * 
 * @author Benjamin Pross
 *
 */
public class JavaProcessCompiler {

	private static Logger LOGGER = LoggerFactory
			.getLogger(JavaProcessCompiler.class);

    /**
     * Static method for compiling source files
     * TODO: return possible compile errors
     * 
     * @param fileName source file name
     */
    public static void compile(String fileName)
    {
        ClassLoader cl = JavaProcessCompiler.class.getClassLoader();
        List<URL> classpath = new ArrayList<URL>();
        if (cl instanceof URLClassLoader) {
            for (URL jar : ((URLClassLoader)cl).getURLs()) {
                classpath.add(jar);
                LOGGER.debug("Using " + jar.toString());
            }
        }
        String classPath = System.getProperty("java.class.path");
        for (String path : classPath.split(File.pathSeparator)) {
            try {
                classpath.add(new URL("file:" + path));
            } catch (MalformedURLException e) {
            	LOGGER.error("Wrong url: " + e.getMessage(), e);
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
                ((ArrayList<String>) opsIter).add(URLDecoder.decode(s, Charset.forName("UTF-8").toString()));
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
        	LOGGER.error(e.getMessage(), e);
        }

    }
	
}
