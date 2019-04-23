/*
 * Copyright (C) 2006-2018 52°North Initiative for Geospatial Open Source
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
import java.util.UUID;

public class ExecutionContext {

    private UUID jobId;
    private final OutputTypeWrapper outputDefinitionTypes;
    private String tempFolderName;

    public ExecutionContext() {
        this(new OutputTypeWrapper(), null);
    }

    public ExecutionContext(UUID jobId) {
        this(new OutputTypeWrapper(), jobId);
    }

    public ExecutionContext(OutputTypeWrapper output, UUID jobId) {
        this.outputDefinitionTypes = output == null ? new OutputTypeWrapper() : output;
        this.jobId = jobId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir") + File.separatorChar + this.tempFolderName;
    }

    public OutputTypeWrapper getOutputs() {
        return this.outputDefinitionTypes;
    }
}
