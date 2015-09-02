/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.algorithm.annotation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.ExecuteMethodBinding;
import org.n52.wps.algorithm.annotation.AnnotationParser.ComplexDataInputFieldAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.ComplexDataInputMethodAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.ComplexDataOutputFieldAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.ComplexDataOutputMethodAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.InputAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.LiteralDataInputFieldAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.LiteralDataInputMethodAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.LiteralDataOutputFieldAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.LiteralDataOutputMethodAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.OutputAnnotationParser;
import org.n52.wps.algorithm.annotation.AnnotationParser.ExecuteAnnotationParser;
import org.n52.wps.algorithm.descriptor.AlgorithmDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class AnnotatedAlgorithmIntrospector {
   
    private final static Logger LOGGER = LoggerFactory.getLogger(AnnotatedAlgorithmIntrospector.class);
    
    private final static List<InputAnnotationParser<?,Field,?>> INPUT_FIELD_PARSERS;
    private final static List<InputAnnotationParser<?,Method,?>> INPUT_METHOD_PARSERS;
    private final static List<OutputAnnotationParser<?,Field,?>> OUTPUT_FIELD_PARSERS;
    private final static List<OutputAnnotationParser<?,Method,?>> OUTPUT_METHOD_PARSERS;
    private final static ExecuteAnnotationParser PROCESS_PARSER;
    
    static {
        List<InputAnnotationParser<?,Field,?>> inputFieldParsers =
                new ArrayList<InputAnnotationParser<?,Field,?>>();
        inputFieldParsers.add(new LiteralDataInputFieldAnnotationParser());
        inputFieldParsers.add(new ComplexDataInputFieldAnnotationParser());
        INPUT_FIELD_PARSERS = Collections.unmodifiableList(inputFieldParsers);
        
        List<InputAnnotationParser<?,Method,?>> inputMethodParsers = 
                new ArrayList<InputAnnotationParser<?,Method,?>>();
        inputMethodParsers.add(new LiteralDataInputMethodAnnotationParser());
        inputMethodParsers.add(new ComplexDataInputMethodAnnotationParser());
        INPUT_METHOD_PARSERS = Collections.unmodifiableList(inputMethodParsers);
        
        List<OutputAnnotationParser<?,Field,?>> outputFieldParsers = 
                new ArrayList<OutputAnnotationParser<?,Field,?>>();
        outputFieldParsers.add(new LiteralDataOutputFieldAnnotationParser());
        outputFieldParsers.add(new ComplexDataOutputFieldAnnotationParser());
        OUTPUT_FIELD_PARSERS = Collections.unmodifiableList(outputFieldParsers);
        
        List<OutputAnnotationParser<?,Method,?>> outputMethodParsers =
                new ArrayList<OutputAnnotationParser<?,Method,?>>();
        outputMethodParsers.add(new LiteralDataOutputMethodAnnotationParser());
        outputMethodParsers.add(new ComplexDataOutputMethodAnnotationParser());
        OUTPUT_METHOD_PARSERS = Collections.unmodifiableList(outputMethodParsers);
        
        PROCESS_PARSER = new ExecuteAnnotationParser();
    }
    
    private final static Map<Class<?>, AnnotatedAlgorithmIntrospector> INTROSPECTOR_MAP =
            new HashMap<Class<?>, AnnotatedAlgorithmIntrospector>();
    public static synchronized AnnotatedAlgorithmIntrospector getInstrospector(Class<?> algorithmClass) {
        AnnotatedAlgorithmIntrospector introspector = INTROSPECTOR_MAP.get(algorithmClass);
        if (introspector == null) {
            introspector = new AnnotatedAlgorithmIntrospector(algorithmClass);
            INTROSPECTOR_MAP.put(algorithmClass, introspector);
        }
        return introspector;
    }
    
    private Class<?> algorithmClass;
    
    private AlgorithmDescriptor algorithmDescriptor;
    
    private ExecuteMethodBinding executeMethodBinding;
    private Map<String, AnnotationBinding.InputBinding<?, ?>> inputBindingMap;
    private Map<String, AnnotationBinding.OutputBinding<?, ?>> outputBindingMap;
    

    public AnnotatedAlgorithmIntrospector(Class<?> algorithmClass) {
        
        this.algorithmClass = algorithmClass;
        
        inputBindingMap = new LinkedHashMap<String, InputBinding<?, ?>>();
        outputBindingMap = new LinkedHashMap<String, OutputBinding<?, ?>>();
        
        parseClass();
        
        inputBindingMap = Collections.unmodifiableMap(inputBindingMap);
        outputBindingMap = Collections.unmodifiableMap(outputBindingMap);
    }

    private void parseClass() {
        
        if (!algorithmClass.isAnnotationPresent(Algorithm.class)) {
            throw new RuntimeException("Class isn't annotated with an Algorithm annotation");
        }
        
        boolean validContructor = false;
        try {
            Constructor defaultConstructor = algorithmClass.getConstructor(new Class[0]);
            validContructor = (defaultConstructor.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC;
        } catch (NoSuchMethodException ex) {
            // inherit error message on fall through...
        } catch (SecurityException ex) {
            throw new RuntimeException("Current security policy limits use of reflection, error introspecting " + algorithmClass.getName());
        }
        if (!validContructor) {
             throw new RuntimeException("Classes with Algorithm annotation require public no-arg constructor, error introspecting " + algorithmClass.getName());
        }
        
        
        AlgorithmDescriptor.Builder<?> algorithmBuilder = null;
        
        Algorithm algorithm = algorithmClass.getAnnotation(Algorithm.class);
        
        algorithmBuilder = AlgorithmDescriptor.builder(
                algorithm.identifier().length() > 0 ?
                    algorithm.identifier() :
                    algorithmClass.getCanonicalName());
        
        algorithmBuilder.
                title(algorithm.title()).
                abstrakt(algorithm.abstrakt()).
                version(algorithm.version()).
                storeSupported(algorithm.storeSupported()).
                statusSupported(algorithm.statusSupported());
        
        parseElements(algorithmClass.getDeclaredMethods(),
                INPUT_METHOD_PARSERS,
                OUTPUT_METHOD_PARSERS);
        parseElements(algorithmClass.getDeclaredFields(),
                INPUT_FIELD_PARSERS,
                OUTPUT_FIELD_PARSERS);

        
        for (Method method : algorithmClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PROCESS_PARSER.getSupportedAnnotation())) {
                ExecuteMethodBinding executeMethodBinding = PROCESS_PARSER.parse(method);
                if (executeMethodBinding != null) {
                    if (this.executeMethodBinding != null) {
                        // we need to error out here because ordering of getDeclaredMethods() or
                        // getMethods() is not guarenteed to be consistent, if it were consistent
                        // maybe we could ignore this state,  but having an algorithm behave
                        // differently betweeen runtimes would be bad...
                        throw new RuntimeException("Multiple execute method bindings encountered for class " + getClass().getCanonicalName());
                    }
                    this.executeMethodBinding = executeMethodBinding;
                }
            }
        }

        if(this.executeMethodBinding == null) {
            throw new RuntimeException("No execute method binding for class " + this.algorithmClass.getCanonicalName());
        }

        for (InputBinding<?,?> inputBinding : inputBindingMap.values()) {
            algorithmBuilder.addInputDescriptor(inputBinding.getDescriptor());
        }
        for (OutputBinding<?,?> outputBinding : outputBindingMap.values()) {
            algorithmBuilder.addOutputDescriptor(outputBinding.getDescriptor());
        }
        algorithmDescriptor = algorithmBuilder.build();
    }

    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return algorithmDescriptor;
    }

    public ExecuteMethodBinding getExecuteMethodBinding() {
        return executeMethodBinding;
    }

    public Map<String, AnnotationBinding.InputBinding<?, ?>> getInputBindingMap() {
        return inputBindingMap;
    }

    public Map<String, AnnotationBinding.OutputBinding<?, ?>> getOutputBindingMap() {
        return outputBindingMap;
    }
    
    public <M extends AccessibleObject & Member> void parseElements(
        M members[],
        List<InputAnnotationParser<?,M,?>> inputParser,
        List<OutputAnnotationParser<?,M,?>> outputParser) {
        for (M member : members) {
            for (OutputAnnotationParser<?,M,?> parser : outputParser) {
                if (member.isAnnotationPresent(parser.getSupportedAnnotation())) {
                    OutputBinding<?,?> binding = parser.parse(member);
                    if (binding != null) {
                        outputBindingMap.put(binding.getDescriptor().getIdentifier(), binding);
                    }
                }
            }
            for (InputAnnotationParser<?,M,?> parser : inputParser) {
                if (member.isAnnotationPresent(parser.getSupportedAnnotation())) {
                    InputBinding<?,?> binding = parser.parse(member);
                    if (binding != null) {
                        inputBindingMap.put(binding.getDescriptor().getIdentifier(), binding);
                    }
                }
            } 
        }
    }
         
}
