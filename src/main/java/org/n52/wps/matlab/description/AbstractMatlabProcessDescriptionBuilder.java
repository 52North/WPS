package org.n52.wps.matlab.description;

import org.n52.matlab.connector.client.MatlabClient;

import com.github.autermann.wps.commons.description.ProcessInputDescription;
import com.github.autermann.wps.commons.description.ProcessOutputDescription;
import com.github.autermann.wps.commons.description.impl.AbstractProcessDescriptionBuilder;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class AbstractMatlabProcessDescriptionBuilder<T extends MatlabProcessDescription, B extends MatlabProcessDescriptionBuilder<T, B>>
        extends AbstractProcessDescriptionBuilder<T, B>
        implements MatlabProcessDescriptionBuilder<T, B> {

    private String function;
    private Supplier<MatlabClient> clientProvider;

    @Override
    @SuppressWarnings("unchecked")
    public B withClientProvider(Supplier<MatlabClient> clientProvider) {
        this.clientProvider = clientProvider;
        return (B) this;
    }

    @Override
    public B withInput(ProcessInputDescription input) {
        if (!(input instanceof MatlabProcessInputDescription)) {
            throw new IllegalArgumentException();
        }
        return super.withInput(input);
    }

    @Override
    public B withOutput(ProcessOutputDescription output) {
        if (!(output instanceof MatlabProcessOutputDescription)) {
            throw new IllegalArgumentException();
        }
        return super.withOutput(output);
    }

    @SuppressWarnings("unchecked")
    @Override
    public B withFunction(String function) {
        this.function = Preconditions.checkNotNull(Strings.emptyToNull(function));
        return (B) this;
    }

    String getFunction() {
        return function;
    }

    Supplier<MatlabClient> getClientProvider() {
        return clientProvider;
    }

}
