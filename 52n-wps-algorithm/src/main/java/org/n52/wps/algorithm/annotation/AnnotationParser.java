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
package org.n52.wps.algorithm.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputFieldBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputMethodBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputFieldBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputMethodBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.ExecuteMethodBinding;
import org.n52.wps.algorithm.descriptor.BoundDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.InputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.OutputDescriptor;
import org.n52.wps.algorithm.util.ClassUtil;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.data.ILiteralData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class AnnotationParser<A extends Annotation, M extends AccessibleObject & Member, B extends AnnotationBinding<M>> {
    
    public final static Logger LOGGER = LoggerFactory.getLogger(AnnotationParser.class);
     
    public B parse(M member) {
        A annotation = member.getAnnotation(getSupportedAnnotation());
        return annotation == null ? null : parse(annotation, member);
    }
    
    public abstract B parse(A annotation, M member);
    
    public abstract Class<? extends A> getSupportedAnnotation();
    
    public static class ExecuteAnnotationParser extends AnnotationParser<Execute, Method, ExecuteMethodBinding> {

        @Override
        public ExecuteMethodBinding parse(Execute annotation, Method member) {
            ExecuteMethodBinding annotationBinding = new ExecuteMethodBinding(member);
            return annotationBinding.validate() ? annotationBinding : null;
        }

        @Override
        public Class<? extends Execute> getSupportedAnnotation() {
            return Execute.class;
        }
        
    }
    
    public abstract static class DataAnnotationParser<A extends Annotation, M extends AccessibleObject & Member, B extends AnnotationBinding.DataBinding<M, ? extends BoundDescriptor>>
        extends AnnotationParser<A,M,B> {
        protected abstract B createBinding(M member);
    }
    
    public abstract static class InputAnnotationParser<A extends Annotation, M extends AccessibleObject & Member, B extends AnnotationBinding.InputBinding<M, ? extends InputDescriptor>>
        extends DataAnnotationParser<A,M,B> {}
    
    public abstract static class OutputAnnotationParser<A extends Annotation, M extends AccessibleObject & Member, B extends AnnotationBinding.OutputBinding<M, ? extends OutputDescriptor>>
        extends DataAnnotationParser<A,M,B> {}
    
    public abstract static class LiteralDataInputAnnotationParser<M extends AccessibleObject & Member, B extends AnnotationBinding.InputBinding<M, LiteralDataInputDescriptor>>
        extends InputAnnotationParser<LiteralDataInput, M, B> {
        
        @Override
        public B parse(LiteralDataInput annotation, M member) {
        
            B annotatedBinding = createBinding(member); 
            // auto generate binding if it's not explicitly declared
            Type payloadType = annotatedBinding.getPayloadType();
            Class<? extends ILiteralData> binding = annotation.binding();
            if (binding == null || ILiteralData.class.equals(binding)) {
                if (payloadType instanceof Class<?>) {
                    binding = BasicXMLTypeFactory.getBindingForPayloadType((Class<?>) payloadType);
                    if (binding == null) {
                        LOGGER.error("Unable to locate binding class for {}; binding not found.", payloadType);
                    }
                } else {
                    if (annotatedBinding.isMemberTypeList()) {
                        LOGGER.error("Unable to determine binding class for {}; List must be parameterized with a type matching a known binding payload to use auto-binding.", payloadType);
                    } else {
                        LOGGER.error("Unable to determine binding class for {}; type must fully resolved to use auto-binding", payloadType);
                    }
                }
            }
            String[] allowedValues = annotation.allowedValues();
            String defaultValue = annotation.defaultValue();
            int maxOccurs = annotation.maxOccurs();
            // If InputType is enum
            //  1) generate allowedValues if not explicitly declared
            //  2) validate allowedValues if explicitly declared
            //  3) validate defaultValue if declared
            //  4) check for special ENUM_COUNT maxOccurs flag
            Type inputType = annotatedBinding.getType();
            if (annotatedBinding.isTypeEnum()) {
                Class<? extends Enum> inputEnumClass = (Class<? extends Enum>) inputType;
                // validate contents of allowed values maps to enum
                if (allowedValues.length > 0) {
                    List<String> invalidValues = new ArrayList<String>();
                    for (String value : allowedValues) {
                        try {
                            Enum.valueOf(inputEnumClass, value);
                        } catch (IllegalArgumentException e) {
                            invalidValues.add(value);
                            LOGGER.warn("Invalid allowed value \"{}\" specified for for enumerated input type {}", value, inputType);
                        }
                    }
                    if (invalidValues.size() > 0) {
                        List<String> updatedValues = new ArrayList<String>(Arrays.asList(allowedValues));
                        updatedValues.removeAll(invalidValues);
                        allowedValues = updatedValues.toArray(new String[0]);
                    }
                }
                // if list is empty, populated with values from enum
                if (allowedValues.length == 0) {
                    allowedValues = ClassUtil.convertEnumToStringArray(inputEnumClass);
                }
                if (defaultValue.length() > 0) {
                    try {
                        Enum.valueOf(inputEnumClass, defaultValue);
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Invalid default value \"{}\" specified for for enumerated input type {}, ignoring.", defaultValue, inputType);
                        defaultValue = "";
                    }
                }
                if (maxOccurs == LiteralDataInput.ENUM_COUNT) {
                    maxOccurs = inputEnumClass.getEnumConstants().length;
                }
            } else {
                if (maxOccurs == LiteralDataInput.ENUM_COUNT) {
                    maxOccurs = 1;
                    LOGGER.warn("Invalid maxOccurs \"ENUM_COUNT\" specified for for input type {}, setting maxOccurs to {}", inputType, maxOccurs);
                }
            }
            if (binding != null) {
                LiteralDataInputDescriptor descriptor =
                        LiteralDataInputDescriptor.builder(annotation.identifier(), binding).
                        title(annotation.title()).
                        abstrakt(annotation.abstrakt()).
                        minOccurs(annotation.minOccurs()).
                        maxOccurs(maxOccurs).
                        defaultValue(defaultValue).
                        allowedValues(allowedValues).
                        build();
                annotatedBinding.setDescriptor(descriptor);
            } else {
                LOGGER.error("Unable to generate binding for input identifier \"{}\"", annotation.identifier());
            }
            return annotatedBinding.validate() ? annotatedBinding : null;
        }

        @Override
        public Class<? extends LiteralDataInput> getSupportedAnnotation() {
            return LiteralDataInput.class;
        }
    }
    
    public abstract static class LiteralDataOutputAnnotationParser<M extends AccessibleObject & Member, B extends AnnotationBinding.OutputBinding<M, LiteralDataOutputDescriptor>>
        extends OutputAnnotationParser<LiteralDataOutput, M, B> {
        
        @Override
        public B parse(LiteralDataOutput annotation, M member) {
            B annotatedBinding = createBinding(member);
            // auto generate binding if it's not explicitly declared
            Type payloadType = annotatedBinding.getPayloadType();
            Class<? extends ILiteralData> binding = annotation.binding();
            if (binding == null || ILiteralData.class.equals(binding)) {
                if (payloadType instanceof Class<?>) {
                    binding = BasicXMLTypeFactory.getBindingForPayloadType((Class<?>) payloadType);
                    if (binding == null) {
                        LOGGER.error("Unable to locate binding class for {}; binding not found.", payloadType);
                    }
                } else {
                    LOGGER.error("Unable to determine binding class for {}; type must fully resolved to use auto-binding", payloadType);
                }
            }
            if (binding != null) {
                LiteralDataOutputDescriptor descriptor =
                        LiteralDataOutputDescriptor.builder(annotation.identifier(), binding).
                        title(annotation.title()).
                        abstrakt(annotation.abstrakt()).
                        build();
                annotatedBinding.setDescriptor(descriptor);
            } else {
                LOGGER.error("Unable to generate binding for output identifier \"{}\"", annotation.identifier());
            }
            return annotatedBinding.validate() ? annotatedBinding : null;
        }

        @Override
        public Class<? extends LiteralDataOutput> getSupportedAnnotation() {
            return LiteralDataOutput.class;
        }
    }
    
    public static class ComplexDataInputAnnotationParser<M extends AccessibleObject & Member, B extends AnnotationBinding.InputBinding<M, ComplexDataInputDescriptor>>
        extends InputAnnotationParser<ComplexDataInput, M, B> {

        @Override
        public B parse(ComplexDataInput annotation, M member) {
            B annotatedBinding = createBinding(member);
            ComplexDataInputDescriptor descriptor =
                    ComplexDataInputDescriptor.builder(annotation.identifier(), annotation.binding()).
                    title(annotation.title()).
                    abstrakt(annotation.abstrakt()).
                    minOccurs(annotation.minOccurs()).
                    maxOccurs(annotation.maxOccurs()).
                    maximumMegaBytes(annotation.maximumMegaBytes()).
                    build();
            annotatedBinding.setDescriptor(descriptor);
            return  annotatedBinding.validate() ? annotatedBinding : null;
        }

        @Override
        public Class<? extends ComplexDataInput> getSupportedAnnotation() {
            return ComplexDataInput.class;
        }

        @Override
        protected B createBinding(M member) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public abstract static class ComplexDataOutputAnnotationParser<M extends AccessibleObject & Member, B extends AnnotationBinding.OutputBinding<M, ComplexDataOutputDescriptor>>
        extends OutputAnnotationParser<ComplexDataOutput, M, B> {
        
        @Override
        public B parse (ComplexDataOutput annotation, M member) {
            B annotatedBinding = createBinding(member);
            ComplexDataOutputDescriptor descriptor = 
                    ComplexDataOutputDescriptor.builder(annotation.identifier(), annotation.binding()).
                    title(annotation.title()).
                    abstrakt(annotation.abstrakt()).
                    build();
            annotatedBinding.setDescriptor(descriptor);
            return annotatedBinding.validate() ? annotatedBinding : null;
        }

        @Override
        public Class<? extends ComplexDataOutput> getSupportedAnnotation() {
            return ComplexDataOutput.class;
        }
    }
    
    public static class LiteralDataInputFieldAnnotationParser extends LiteralDataInputAnnotationParser<Field, AnnotationBinding.InputBinding<Field, LiteralDataInputDescriptor>> {
        @Override
        protected InputBinding<Field, LiteralDataInputDescriptor> createBinding(Field member) {
            return new InputFieldBinding<LiteralDataInputDescriptor>(member);
        }
    }
    public static class LiteralDataOutputFieldAnnotationParser extends LiteralDataOutputAnnotationParser<Field, AnnotationBinding.OutputBinding<Field, LiteralDataOutputDescriptor>> {
        @Override
        protected OutputBinding<Field, LiteralDataOutputDescriptor> createBinding(Field member) {
            return new OutputFieldBinding<LiteralDataOutputDescriptor>(member);
        }
    }
    public static class ComplexDataInputFieldAnnotationParser extends ComplexDataInputAnnotationParser<Field, AnnotationBinding.InputBinding<Field, ComplexDataInputDescriptor>> {
        @Override
        protected InputBinding<Field, ComplexDataInputDescriptor> createBinding(Field member) {
            return new InputFieldBinding<ComplexDataInputDescriptor>(member);
        }
    }
    public static class ComplexDataOutputFieldAnnotationParser extends ComplexDataOutputAnnotationParser<Field, AnnotationBinding.OutputBinding<Field, ComplexDataOutputDescriptor>> {
        @Override
        protected OutputBinding<Field, ComplexDataOutputDescriptor> createBinding(Field member) {
            return new OutputFieldBinding<ComplexDataOutputDescriptor>(member);
        }
    }
    
    public static class LiteralDataInputMethodAnnotationParser extends LiteralDataInputAnnotationParser<Method, AnnotationBinding.InputBinding<Method, LiteralDataInputDescriptor>> {
        @Override
        protected InputBinding<Method, LiteralDataInputDescriptor> createBinding(Method member) {
            return new InputMethodBinding<LiteralDataInputDescriptor>(member);
        }
    }
    public static class LiteralDataOutputMethodAnnotationParser extends LiteralDataOutputAnnotationParser<Method, AnnotationBinding.OutputBinding<Method, LiteralDataOutputDescriptor>> {
        @Override
        protected OutputBinding<Method, LiteralDataOutputDescriptor> createBinding(Method member) {
            return new OutputMethodBinding<LiteralDataOutputDescriptor>(member);
        }
    }
    public static class ComplexDataInputMethodAnnotationParser extends ComplexDataInputAnnotationParser<Method, AnnotationBinding.InputBinding<Method, ComplexDataInputDescriptor>> {
        @Override
        protected InputBinding<Method, ComplexDataInputDescriptor> createBinding(Method member) {
            return new InputMethodBinding<ComplexDataInputDescriptor>(member);
        }
    }
    public static class ComplexDataOutputMethodAnnotationParser extends ComplexDataOutputAnnotationParser<Method, AnnotationBinding.OutputBinding<Method, ComplexDataOutputDescriptor>> {
        @Override
        protected OutputBinding<Method, ComplexDataOutputDescriptor> createBinding(Method member) {
            return new OutputMethodBinding<ComplexDataOutputDescriptor>(member);
        }
    }
}
