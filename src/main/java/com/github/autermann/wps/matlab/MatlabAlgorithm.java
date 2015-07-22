/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.autermann.wps.matlab;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;

import com.github.autermann.matlab.MatlabException;
import com.github.autermann.matlab.MatlabRequest;
import com.github.autermann.matlab.MatlabResult;
import com.github.autermann.matlab.client.MatlabClient;
import com.github.autermann.matlab.value.MatlabScalar;
import com.github.autermann.matlab.value.MatlabValue;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.matlab.description.MatlabProcessDescription;
import com.github.autermann.wps.matlab.description.MatlabProcessInputDescription;
import com.github.autermann.wps.matlab.transform.MatlabValueTransformer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class MatlabAlgorithm implements IAlgorithm {
    private static final Logger LOG = LoggerFactory
            .getLogger(MatlabAlgorithm.class);
    private final MatlabProcessDescription description;

    public MatlabAlgorithm(MatlabProcessDescription description) {
        this.description = Preconditions.checkNotNull(description);
    }

    @Override
    public List<String> getErrors() {
        return Collections.emptyList();
    }

    @Override
    public ProcessDescriptionType getDescription() {
        return this.description.getProcessDescription();
    }

    @Override
    public String getWellKnownName() {
        return this.description.getId().getValue();
    }

    @Override
    public Class<? extends IData> getInputDataType(String id) {
        return this.description.getInput(new OwsCodeType(id)).getBindingClass();
    }

    @Override
    public Class<? extends IData> getOutputDataType(String id) {
        return this.description.getOutput(new OwsCodeType(id)).getBindingClass();
    }

    @Override
    public boolean processDescriptionIsValid() {
        return getDescription().validate();
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData)
            throws ExceptionReport {
        try {
            MatlabRequest request = fromInputData(inputData);
            LOG.info("Executing Matlab request: {}", request);
            MatlabClient client = this.description.getClientProvider().get();
            MatlabResult result = client.execSync(request);
            LOG.info("Matlab result: {}", result);
            return toOutputData(result);
        } catch (IOException | MatlabException ex) {
            throw new ExceptionReport(ex.getMessage(),
                    ExceptionReport.REMOTE_COMPUTATION_ERROR, ex);
        }
    }

    private MatlabRequest fromInputData(Map<String, List<IData>> inputs)
            throws ExceptionReport {

        MatlabRequest req = new MatlabRequest(description.getFunction());

        MatlabValueTransformer t = new MatlabValueTransformer();

        for (MatlabProcessInputDescription in : description.getInputDescriptions()) {
            String id = in.getId().getValue();
            if (inputs.containsKey(id)) {
                req.addParameter(t.transform(in, inputs.get(id)));
            } else if (in.getOccurence().isRequired()) {
                throw new ExceptionReport("missing input " + in.getId(),
                                          ExceptionReport.MISSING_PARAMETER_VALUE);
            } else {
                req.addParameter(new MatlabScalar(Double.NaN));
            }
        }
        description.getOutputDescriptions().stream().forEach(out ->
            req.addResult(out.getId().getValue(), out.getMatlabType())
        );
        return req;
    }

    private Map<String, IData> toOutputData(MatlabResult result)
            throws ExceptionReport {
        Map<String, IData> map = Maps.newHashMap();
        MatlabValueTransformer t = new MatlabValueTransformer();
        for (String id : result.getResults().keySet()) {
            MatlabValue value = result.getResult(id);
            OwsCodeType codeType = new OwsCodeType(id);
            IData data = t.transform(description.getOutput(codeType), value);
            map.put(id, data);
        }
        return map;
    }
}
