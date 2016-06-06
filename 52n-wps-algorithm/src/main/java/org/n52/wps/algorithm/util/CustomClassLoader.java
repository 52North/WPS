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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.n52.wps.server.UploadedAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This custom ClassLoader loads classes from specified paths.
 * 
 * @author Benjamin Pross
 *
 */
public class CustomClassLoader extends ClassLoader {

    /**
     * The HashMap where the classes will be cached
     */
    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	private String baseDir;

	private static Logger LOGGER = LoggerFactory
			.getLogger(CustomClassLoader.class);

	public CustomClassLoader(String baseDir){
	    this.baseDir = baseDir;	
	}
	
    @Override
    public String toString() {
        return CustomClassLoader.class.getName();
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {	    	
    	try {
			return UploadedAlgorithmRepository.class.getClassLoader().loadClass(name);				
		} catch (ClassNotFoundException e) {
			LOGGER.info("Class not found: " + name + ". Trying custom class loader.");
			return findClass(name);
		}
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        if (classes.containsKey(name)) {
            return classes.get(name);
        }

        byte[] classData;

        try {
            classData = loadClassData(name);
        } catch (IOException e) {
            throw new ClassNotFoundException("Class [" + name
                    + "] could not be found", e);
        }

        Class<?> c = defineClass(null, classData, 0, classData.length);
        resolveClass(c);
        classes.put(name, c);

        return c;
    }

    /**
     * Load the class file into byte array
     * 
     * @param name
     *            The name of the process class, it will be loaded from basedDir
     * @return The class file as byte array
     * @throws IOException
     */
    private byte[] loadClassData(String name) throws IOException {
    	
        String pathToClassFile = (baseDir.endsWith(File.separator) ? baseDir : baseDir + File.separator)+ name.replace(".", "/") + ".class"; 
        
        InputStream classBytesStream = null;
        
        File classFile = new File(pathToClassFile);
        
        if(classFile.isAbsolute()){
            //absolute file path was passed, so try to load file
            classBytesStream = new FileInputStream(classFile);
        }else{
            //relative path was passed, so try to get resource as stream 
            classBytesStream =  UploadedAlgorithmRepository.class.getClassLoader().getResourceAsStream(pathToClassFile);
        }
    	
        BufferedInputStream in = new BufferedInputStream(classBytesStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i;

        while ((i = in.read()) != -1) {
            out.write(i);
        }

        in.close();
        byte[] classData = out.toByteArray();
        out.close();

        return classData;
    }

    public String getBaseDir() {
        return baseDir;
    }
}
