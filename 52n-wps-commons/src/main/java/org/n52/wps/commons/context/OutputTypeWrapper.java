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

import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.OutputDefinitionType;

public class OutputTypeWrapper {

    private List<? extends OutputDefinitionType> wps100OutputDefinitionTypes = new ArrayList<>();

    private List<? extends net.opengis.wps.x20.OutputDefinitionType> wps200OutputDefinitionTypes = new ArrayList<>();

    private boolean isWPS100Execution = false;

    private boolean isWPS200Execution = false;

    public List<? extends OutputDefinitionType> getWps100OutputDefinitionTypes() {
        return wps100OutputDefinitionTypes;
    }

    public void setWps100OutputDefinitionTypes(List<? extends OutputDefinitionType> wps100OutputDefinitionTypes) {
        this.wps100OutputDefinitionTypes = wps100OutputDefinitionTypes;
        setWPS100Execution(true);
    }

    public List<? extends net.opengis.wps.x20.OutputDefinitionType> getWps200OutputDefinitionTypes() {
        return wps200OutputDefinitionTypes;
    }

    public void setWps200OutputDefinitionTypes(
            List<? extends net.opengis.wps.x20.OutputDefinitionType> wps200OutputDefinitionTypes) {
        this.wps200OutputDefinitionTypes = wps200OutputDefinitionTypes;
        setWPS200Execution(true);
    }

    public boolean isWPS100Execution() {
        return isWPS100Execution;
    }

    public void setWPS100Execution(boolean isWPS100Execution) {
        this.isWPS100Execution = isWPS100Execution;
    }

    public boolean isWPS200Execution() {
        return isWPS200Execution;
    }

    public void setWPS200Execution(boolean isWPS200Execution) {
        this.isWPS200Execution = isWPS200Execution;
    }

}
