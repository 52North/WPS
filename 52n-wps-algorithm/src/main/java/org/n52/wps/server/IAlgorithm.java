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
package org.n52.wps.server;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

/**
 * @author Bastian Schaeffer, University of Muenster,    Theodor Foerster, ITC
 *
 */
public interface IAlgorithm  {

    Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport;

    List<String> getErrors();

    ProcessDescription getDescription();

    /** Returns some well-known name for the process.
     *
     *  @return Returns some well-known name for the process or algorithm
     *  if that exists, else returns an empty String, never null.
     *  <b>Note:</b> The fully-qualified class name is gotten via getName();
     */
    String getWellKnownName();

    /**
     *
     * Checks if the processDescription complies to the process itself and fits any schema or other dependencies.
     *
     * @param version the desired version of the process description to be validated
     * @return true if the processdescription is valid
     */
    boolean processDescriptionIsValid(String version);

    Class< ? > getInputDataType(String id);

    Class< ? > getOutputDataType(String id);

}
