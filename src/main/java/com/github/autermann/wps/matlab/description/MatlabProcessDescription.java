package com.github.autermann.wps.matlab.description;

import java.util.Collection;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import com.github.autermann.matlab.client.MatlabClient;
import com.github.autermann.yaml.YamlNode;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabProcessDescription extends AbstractMatlabDescription {
    private String version;
    private boolean storeSupported;
    private boolean statusSupported;
    private String function;
    private final Map<String, MatlabInputDescripton> inputs;
    private final Map<String, MatlabOutputDescription> outputs;
    private Supplier<MatlabClient> clientProvider;
    private ProcessDescriptionType processDescription;

    public MatlabProcessDescription() {
        this.inputs = Maps.newLinkedHashMap();
        this.outputs = Maps.newLinkedHashMap();
    }

    public Collection<MatlabInputDescripton> getInputs() {
        return inputs.values();
    }

    public MatlabInputDescripton getInput(String id) {
        return inputs.get(id);
    }

    public Collection<MatlabOutputDescription> getOutputs() {
        return outputs.values();
    }

    public MatlabOutputDescription getOutput(String id) {
        return outputs.get(id);
    }

    public Supplier<MatlabClient> getClientProvider() {
        return clientProvider;
    }

    public void setClientConfiguration(
            Supplier<MatlabClient> clientProvider) {
        this.clientProvider = clientProvider;
    }

    public void addInputDescription(MatlabInputDescripton desc) {
        this.inputs.put(desc.getId(), desc);
    }

    public void addOutputDescription(MatlabOutputDescription desc) {
        this.outputs.put(desc.getId(), desc);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = Strings.emptyToNull(version);
    }

    public boolean isStoreSupported() {
        return storeSupported;
    }

    public void setStoreSupported(boolean storeSupported) {
        this.storeSupported = storeSupported;
    }

    public boolean isStatusSupported() {
        return statusSupported;
    }

    public void setStatusSupported(boolean statusSupported) {
        this.statusSupported = statusSupported;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public ProcessDescriptionType getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(ProcessDescriptionType processDescription) {
        this.processDescription = processDescription;
    }

    public static MatlabProcessDescription load(YamlNode definition) {
        return new MatlabDescriptionGenerator()
                .createProcessDescription(definition);
    }
}
