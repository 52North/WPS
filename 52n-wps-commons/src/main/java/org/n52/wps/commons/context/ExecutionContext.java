/*
 * Copyright (C) 2006-2017 52°North Initiative for Geospatial Open Source
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

import java.io.File;

public class ExecutionContext {

    private String tempFolderName;
    private OutputTypeWrapper outputDefinitionTypes;

    public ExecutionContext() {
        this(new OutputTypeWrapper());
    }

    public ExecutionContext(OutputTypeWrapper output) {
        outputDefinitionTypes = output == null ? new OutputTypeWrapper() : output;
    }

    public String getTempDirectoryPath() {

        return System.getProperty("java.io.tmpdir") + File.separatorChar + this.tempFolderName;
    }

    public OutputTypeWrapper getOutputs() {
        return this.outputDefinitionTypes;
    }
}
