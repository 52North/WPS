package com.github.autermann.wps.matlab;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.matlab.MatlabException;
import com.github.autermann.matlab.MatlabRequest;
import com.github.autermann.matlab.MatlabResult;
import com.github.autermann.matlab.client.MatlabClient;
import com.github.autermann.matlab.value.MatlabScalar;
import com.github.autermann.wps.matlab.description.MatlabInputDescripton;
import com.github.autermann.wps.matlab.description.MatlabOutputDescription;
import com.github.autermann.wps.matlab.description.MatlabProcessDescription;
import com.github.autermann.wps.matlab.transform.MatlabValueTransformer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class MatlabAlgorithm implements IAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(MatlabAlgorithm.class);
    private final MatlabProcessDescription description;

    public MatlabAlgorithm(MatlabProcessDescription description) {
        this.description = Preconditions.checkNotNull(description);
    }

    @Override
    public List<String> getErrors() {
        return Collections.emptyList();
    }

    @Override
    public synchronized ProcessDescriptionType getDescription() {
        return description.getProcessDescription();
    }

    @Override
    public String getWellKnownName() {
        return description.getId();
    }

    @Override
    public Class<? extends IData> getInputDataType(String id) {
        return description.getInput(id).getBindingClass();
    }

    @Override
    public Class<? extends IData> getOutputDataType(String id) {
        return description.getOutput(id).getBindingClass();
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
            MatlabClient client = MatlabClient.create(description.getClientConfig());
            MatlabResult result = client.exec(request);
            LOG.info("Matlab result: {}", result);
            return toOutputData(result);
        } catch (IOException ex) {
            throw new ExceptionReport(ex.getMessage(),
                                      ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                      ex);
        } catch (MatlabException ex) {
            throw new ExceptionReport(ex.getMessage(),
                                      ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                      ex);
        }

    }

    private MatlabRequest fromInputData(Map<String, List<IData>> inputs)
            throws ExceptionReport {

        MatlabRequest req = new MatlabRequest(description.getFunction());

        MatlabValueTransformer t = new MatlabValueTransformer();

        for (MatlabInputDescripton in : description.getInputs()) {
            if (inputs.containsKey(in.getId())) {
                req.addParameter(t.transform(in, inputs.get(in.getId())));
            } else if (in.getMinOccurs() > 0) {
                throw new ExceptionReport("missing input " + in.getId(),
                                          ExceptionReport.MISSING_PARAMETER_VALUE);
            } else {
                req.addParameter(new MatlabScalar(Double.NaN));
            }
        }

        for (MatlabOutputDescription out : description.getOutputs()) {
            req.addResult(out.getId(), out.getMatlabType());
        }
        return req;
    }

    private Map<String, IData> toOutputData(MatlabResult result) throws ExceptionReport {
        Map<String,IData> map = Maps.newHashMap();
        MatlabValueTransformer t = new MatlabValueTransformer();
        for (String id : result.getResults().keySet()) {
            map.put(id, t.transform(description.getOutput(id), result.getResult(id)));
        }
        return map;
    }
}
