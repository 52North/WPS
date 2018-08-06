/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;


/**
 * Extending subclasses of AbstractGenerator shall provide functionality to
 * generate serviceable output data for the processes offered by the 52N WPS framework.
 *
 * @author Matthias Mueller
 *
 */

public abstract class AbstractIOHandler implements IOHandler {
    protected Set<String> supportedFormats;
    protected Set<String> supportedSchemas;
    protected Set<String> supportedEncodings;
    protected Set<Class<?>> supportedIDataTypes;
    protected List<? extends ConfigurationEntry<?>> properties;
    protected List<FormatEntry> formats;

    public AbstractIOHandler(){
        this.supportedFormats = new HashSet<String>();
        this.supportedSchemas = new HashSet<String>();
        this.supportedEncodings = new HashSet<String>();
        this.supportedIDataTypes = new HashSet<Class<?>>();
    }

    /**
     * Returns true if the given format is supported, else false.
     */
    public boolean isSupportedFormat(String format) {
        String[] sf = getSupportedFormats();
        for(String f : sf) {
            if (f.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an array having the supported formats (mimeTypes).
     */
    public String[] getSupportedFormats() {
        return supportedFormats.toArray(new String[supportedFormats.size()]);
    }


    /**
     * Returns an array having the supported schemas.
     */
    public String[] getSupportedSchemas() {
        return supportedSchemas.toArray(new String[supportedSchemas.size()]);
    }


    /**
     * Returns true if the given schema is supported, else false.
     * Binary data has no schema in WPS 1.0.0: If the request does not contain a schema and
     * the Generator has no schemas configured it is assumed to be a "binary case".
     * The method will return TRUE in this case.
     * Might lead to unexpected behaviour in malformed requests.
     */
    public boolean isSupportedSchema(String schema) {
        //no schema given. assuming no schema required. therefore accept all schemas
        if(supportedSchemas.size()==0 && (schema == null || schema.isEmpty())){ // test whether schema is empty, because in ArcToolbox process descriptions, there is empty elements for schemas
            return true;
        }
        for(String supportedSchema : supportedSchemas) {
            if(supportedSchema.equalsIgnoreCase(schema)){
                return true;
            }
        }
        return false;
    }

    public Class<?>[] getSupportedDataBindings() {
        return supportedIDataTypes.toArray(new Class<?>[supportedIDataTypes.size()]);
    }


    public boolean isSupportedDataBinding(Class<?> binding) {
        for (Class<?> currentBinding : supportedIDataTypes){
            if (binding.equals(currentBinding)){
                return true;
            }
        }
        return false;
    }

    public String[] getSupportedEncodings(){
        String[] resultArray = supportedEncodings.toArray(new String[supportedEncodings.size()]);
        return resultArray;
        //return IOHandler.SUPPORTED_ENCODINGS;
    }

    public List<FormatEntry> getSupportedFullFormats(){
        return formats;
    }

    public boolean isSupportedEncoding(String encoding){
        for (String currentEncoding : this.getSupportedEncodings()){
            if (currentEncoding.equalsIgnoreCase(encoding)){
                return true;
            }
        }
        return false;
    }

    protected boolean isSupportedGenerate (Class<?> binding, String mimeType, String schema){

        if (!(this.isSupportedFormat(mimeType))){
            return false;
        }

        if (!(this.isSupportedSchema(schema))){
            return false;
        }

        if(!(this.isSupportedDataBinding(binding))){
            return false;
        }

        return true;
    }

}

