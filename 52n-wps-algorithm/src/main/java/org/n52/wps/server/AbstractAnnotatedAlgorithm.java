/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.algorithm.annotation.AnnotatedAlgorithmIntrospector;
import static org.n52.wps.algorithm.annotation.AnnotatedAlgorithmIntrospector.getInstrospector;
import org.n52.wps.algorithm.annotation.AnnotationBinding;
import org.n52.wps.algorithm.descriptor.AlgorithmDescriptor;
import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author tkunicki
 */
public abstract class AbstractAnnotatedAlgorithm extends AbstractDescriptorAlgorithm {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractAnnotatedAlgorithm.class);

    @Override
    protected AlgorithmDescriptor createAlgorithmDescriptor() {
        return getInstrospector(getAlgorithmClass()).getAlgorithmDescriptor();
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputMap) {
        Object annotatedInstance = getAlgorithmInstance();
        
        AnnotatedAlgorithmIntrospector introspector = getInstrospector(annotatedInstance.getClass());
        
        for (Map.Entry<String, AnnotationBinding.InputBinding<?, ?>> iEntry : introspector.getInputBindingMap().entrySet()) {
            iEntry.getValue().set(annotatedInstance, inputMap.get(iEntry.getKey()));
        }
        
        getInstrospector(annotatedInstance.getClass()).getExecuteMethodBinding().execute(annotatedInstance);
        
        Map<String, IData> oMap = new HashMap<String, IData>();
        for (Map.Entry<String, AnnotationBinding.OutputBinding<?, ?>> oEntry : introspector.getOutputBindingMap().entrySet()) {
            oMap.put(oEntry.getKey(), oEntry.getValue().get(annotatedInstance));
        }
        return oMap;
    }
    
    public Object getAlgorithmInstance() {
        return this;
    }
    
    public Class<?> getAlgorithmClass() {
        return getClass();
    }
    
    public static class Proxy extends AbstractAnnotatedAlgorithm {
        
        final private Class<?> proxiedClass;
        final private Object proxiedInstance;
        
        public Proxy(Class<?> proxiedClass) {
            this.proxiedClass = proxiedClass;
            try {
                this.proxiedInstance = proxiedClass.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("unable to instantiate proxied algorithm instance");
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("unable to instantiate proxied algorithm instance");
            }
        }

        @Override
        public Class<?> getAlgorithmClass() {
            return proxiedClass;
        }

        @Override
        public Object getAlgorithmInstance() {
            return proxiedInstance;
        }
    }

}
