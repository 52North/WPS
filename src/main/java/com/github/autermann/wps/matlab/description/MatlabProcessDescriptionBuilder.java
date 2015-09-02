package com.github.autermann.wps.matlab.description;

import org.n52.matlab.connector.client.MatlabClient;

import com.github.autermann.wps.commons.description.ProcessDescriptionBuilder;
import com.google.common.base.Supplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface MatlabProcessDescriptionBuilder<T extends MatlabProcessDescription, B extends MatlabProcessDescriptionBuilder<T, B>>
        extends ProcessDescriptionBuilder<T, B> {

    B withClientProvider(Supplier<MatlabClient> clientProvider);

    B withFunction(String function);

}
