/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
