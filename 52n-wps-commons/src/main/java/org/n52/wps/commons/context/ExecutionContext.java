/**
 * ﻿Copyright (C) 2006 - 2019 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.commons.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.opengis.wps.x100.OutputDefinitionType;

public class ExecutionContext {

    private String tempFolderName;
    private List<OutputDefinitionType> outputDefinitionTypes;

    public ExecutionContext() {
        this(Arrays.asList(new OutputDefinitionType[0]));
    }

    public ExecutionContext(OutputDefinitionType output) {
        this(Arrays.asList(output != null ? new OutputDefinitionType[] {output} : new OutputDefinitionType[0]));
    }

    public ExecutionContext(List< ? extends OutputDefinitionType> outputs) {
        this.tempFolderName = UUID.randomUUID().toString();
        this.outputDefinitionTypes = Collections.unmodifiableList(outputs != null ? outputs
                                                                                 : Arrays.asList(new OutputDefinitionType[0]));
    }

    public String getTempDirectoryPath() {

        return System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + this.tempFolderName;
    }

    public List<OutputDefinitionType> getOutputs() {
        return this.outputDefinitionTypes;
    }
}
