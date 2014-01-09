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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import org.n52.wps.algorithm.descriptor.BoundDescriptor;
import org.n52.wps.algorithm.descriptor.InputDescriptor;
import org.n52.wps.algorithm.descriptor.OutputDescriptor;
import org.n52.wps.algorithm.util.ClassUtil;
import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class AnnotationBinding<M extends AccessibleObject & Member> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AnnotationBinding.class);
    
    private M member;

    public AnnotationBinding(M member) {
        this.member = member;
    }

    public M getMember() {
        return member;
    }
    
    protected boolean checkModifier() {
        return (getMember().getModifiers() & Modifier.PUBLIC) != 0;
    }
    
    public abstract boolean validate();
    
    public static class ExecuteMethodBinding extends AnnotationBinding<Method> {

        public ExecuteMethodBinding(Method method) {
            super(method);
        }

        @Override
        public boolean validate() {
            if (!checkModifier()) {
                LOGGER.error("Method {} with Execute annotation can't be used, not public.", getMember());
                return false;
            }
            // eh, do we really need to care about this?
            if (!getMember().getReturnType().equals(void.class)) {
                LOGGER.error("Method {} with Execute annotation can't be used, return type not void", getMember());
                return false;
            }
            if (getMember().getParameterTypes().length != 0) {
                LOGGER.error("Method {} with Execute annotation can't be used, method parameter count is > 0.", getMember());
                return false;
            }
            return true;
        }
        
        public void execute(Object annotatedInstance) {
            try {
                getMember().invoke(annotatedInstance);
            }catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error executing process", ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error executing process", ex);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                throw new RuntimeException(cause.getMessage(), cause);
            }
        }
    }
    
    public static abstract class DataBinding<M extends AccessibleObject & Member, D extends BoundDescriptor> extends AnnotationBinding<M> {
        
        private D descriptor;
        
        public DataBinding(M member) {
            super(member);
        }
        
        public void setDescriptor(D descriptor) {
            this.descriptor = descriptor;
        }
        
        public D getDescriptor() {
            return descriptor;
        }
        
        public abstract Type getMemberType();

        public Type getType() {
            return getMemberType();
        }

        public Type getPayloadType() {
            Type type = getType();
            if (isTypeEnum()) {
                return String.class;
            }
            if (type instanceof Class<?>) {
                Class<?> inputClass = (Class<?>) type;
                if (inputClass.isPrimitive()) {
                    return ClassUtil.wrap(inputClass);
                }
            }
            return type;
        }

        public boolean isTypeEnum() {
            Type inputType = getType();
            return (inputType instanceof Class<?>) && ((Class<?>) inputType).isEnum();
        }
    }

    public static abstract class InputBinding<M extends AccessibleObject & Member, D extends InputDescriptor> extends DataBinding<M,D> {
        
        public InputBinding(M member) {
            super(member);
        }

        @Override
        public Type getType() {
            Type memberType = getMemberType();
            Type inputType = memberType;
            if (memberType instanceof Class<?>) {
                Class<?> memberClass = (Class<?>) memberType;
                if (List.class.isAssignableFrom(memberClass)) {
                    // We treat List as List<? extends Object>
                    inputType = NOT_PARAMETERIZED_TYPE;
                }
            } else if (memberType instanceof ParameterizedType) {
                ParameterizedType parameterizedMemberType = (ParameterizedType) memberType;
                Class<?> rawClass = (Class<?>) parameterizedMemberType.getRawType();
                if (List.class.isAssignableFrom(rawClass)) {
                    inputType = parameterizedMemberType.getActualTypeArguments()[0];
                }
            } else {
                LOGGER.error("Unable to infer concrete type information for " + getMember());
            }
            return inputType;
        }

        public boolean isMemberTypeList() {
            Type memberType = getMemberType();
            if (memberType instanceof Class<?>) {
                return List.class.isAssignableFrom((Class<?>) memberType);
            } else if (memberType instanceof ParameterizedType) {
                Class<?> rawClass = (Class<?>) ((ParameterizedType) memberType).getRawType();
                return List.class.isAssignableFrom(rawClass);
            } else {
                LOGGER.error("Unable to infer concrete type information for " + getMember());
            }
            return false;
        }
        
        protected boolean checkType() {
            Type inputPayloadType = getPayloadType();
            Class<? extends IData> bindingClass = getDescriptor().getBinding();
            try {
                Class<?> bindingPayloadClass = bindingClass.getMethod("getPayload", (Class<?>[]) null).getReturnType();
                if (inputPayloadType instanceof Class<?>) {
                    return ((Class<?>) inputPayloadType).isAssignableFrom(bindingPayloadClass);
                } else if (inputPayloadType instanceof ParameterizedType) {
                    // i.e. List<FeatureCollection<SimpleFeatureType,SimpleFeature>>
                    return ((Class<?>) ((ParameterizedType) inputPayloadType).getRawType()).isAssignableFrom(bindingPayloadClass);
                } else if (inputPayloadType instanceof WildcardType) {
                    // i.e. List<? extends String> or List<? super String>
                    WildcardType inputTypeWildcardType = (WildcardType) inputPayloadType;
                    Type[] lowerBounds = inputTypeWildcardType.getLowerBounds();
                    Type[] upperBounds = inputTypeWildcardType.getUpperBounds();
                    Class<?> lowerBoundClass = null;
                    Class<?> upperBoundClass = null;
                    if (lowerBounds != null && lowerBounds.length > 0) {
                        if (lowerBounds[0] instanceof Class<?>) {
                            lowerBoundClass = (Class<?>) lowerBounds[0];
                        } else if (lowerBounds[0] instanceof ParameterizedType) {
                            lowerBoundClass = (Class<?>) ((ParameterizedType) lowerBounds[0]).getRawType();
                        }
                    }
                    if (upperBounds != null && upperBounds.length > 0) {
                        if (upperBounds[0] instanceof Class<?>) {
                            upperBoundClass = (Class<?>) upperBounds[0];
                        } else if (upperBounds[0] instanceof ParameterizedType) {
                            upperBoundClass = (Class<?>) ((ParameterizedType) upperBounds[0]).getRawType();
                        }
                    }
                    return (upperBoundClass == null || upperBoundClass.isAssignableFrom(bindingPayloadClass)) && (lowerBounds == null || bindingPayloadClass.isAssignableFrom(lowerBoundClass));
                } else {
                    LOGGER.error("Unable to infer assignability from type for " + getMember());
                }
            } catch (NoSuchMethodException e) {
                return false;
            }
            return false;
        }
        
        public Object unbindInput(List<IData> boundValueList) {
            Object value = null;
            if (boundValueList != null && boundValueList.size() > 0) {
                if (isMemberTypeList()) {
                    List valueList = new ArrayList(boundValueList.size());
                    for (IData bound : boundValueList) {
                        value = bound.getPayload();
                        if (isTypeEnum()) {
                            value = Enum.valueOf((Class<? extends Enum>)getType(), (String)value);
                        }
                        valueList.add(value);
                    }
                    value = valueList;
                } else if (boundValueList.size() == 1) {
                    value = boundValueList.get(0).getPayload();
                    if (isTypeEnum()) {
                        value = Enum.valueOf((Class<? extends Enum>)getType(), (String)value);
                    }
                }
            }
            return value;
        }
        
        public abstract void set(Object annotatedObject, List<IData> boundInputList);
    }

    public static abstract class OutputBinding<M extends AccessibleObject & Member,  D extends OutputDescriptor> extends DataBinding<M,D> {
        
        private Constructor<? extends IData> bindingConstructor;
        
        public OutputBinding(M member) {
            super(member);
        }
        
        protected boolean checkType( ) {
            return getConstructor() != null;
        }
        
        public IData bindOutputValue(Object outputValue) {
            try {
                if (isTypeEnum()) {
                    outputValue = ((Enum<?>)outputValue).name();
                }
                return getConstructor().newInstance(outputValue);
            } catch (InstantiationException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            } catch (SecurityException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                throw new RuntimeException(cause.getMessage(), cause);
            }
        }
        
        public abstract IData get(Object annotatedInstance);
        
        private synchronized Constructor<? extends IData> getConstructor() {
            if (bindingConstructor == null ){
                try {
                    Class<? extends IData> bindingClass = getDescriptor().getBinding();
                    Class<?> outputPayloadClass = bindingClass.getMethod("getPayload", (Class<?>[]) null).getReturnType();
                    Type bindingPayloadType = getPayloadType();
                    if (bindingPayloadType instanceof Class<?>) {
                        Class<?> bindingPayloadClass = (Class<?>) bindingPayloadType;
                        if (bindingPayloadClass.isAssignableFrom(outputPayloadClass)) {
                            bindingConstructor = bindingClass.getConstructor(bindingPayloadClass);
                        }
                    }
                }  catch (NoSuchMethodException e) {
                    // error handling on fall-through
                }
            }
            return bindingConstructor;
        }
    }

    public static class InputFieldBinding<D extends InputDescriptor> extends InputBinding<Field, D> {

        public InputFieldBinding(Field field) {
            super(field);
        }

        @Override
        public Type getMemberType() {
            return getMember().getGenericType();
        }
        
        @Override
        public boolean validate() {
            if (!checkModifier()) {
                LOGGER.error("Field {} with input annotation can't be used, not public.", getMember());
                return false;
            }
            if (!(getDescriptor().getMaxOccurs().intValue() < 2 || isMemberTypeList())) {
                LOGGER.error("Field {} with input annotation can't be used, maxOccurs > 1 and field is not of type List", getMember());
                return false;
            }
            if (!checkType()) {
                LOGGER.error("Field {} with input annotation can't be used, unable to safely assign field using binding payload type", getMember());
                return false;
            }
            return true;
        }
        
        @Override
        public void set(Object annotatedObject, List<IData> boundInputList) {
            try {
                getMember().set(annotatedObject, unbindInput(boundInputList));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            }
        }
    }

    public static class InputMethodBinding<D extends InputDescriptor> extends InputBinding<Method, D> {

        public InputMethodBinding(Method method) {
            super(method);
        }

        @Override
        public Type getMemberType() {
            Type[] genericParameterTypes = getMember().getGenericParameterTypes();
            return (genericParameterTypes.length == 0) ? Void.class : genericParameterTypes[0];
        }
        
        @Override
        public boolean validate() {
            if (!checkModifier()) {
                LOGGER.error("Field {} with input annotation can't be used, not public.", getMember());
                return false;
            }
            if (!(getDescriptor().getMaxOccurs().intValue() < 2 || isMemberTypeList())) {
                LOGGER.error("Field {} with input annotation can't be used, maxOccurs > 1 and field is not of type List", getMember());
                return false;
            }
            if (!checkType()) {
                LOGGER.error("Field {} with input annotation can't be used, unable to safely assign field using binding payload type", getMember());
                return false;
            }
            return true;
        }
        
        @Override
        public void set(Object annotatedObject, List<IData> boundInputList) {
            try {
                getMember().invoke(annotatedObject, unbindInput(boundInputList));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                throw new RuntimeException(cause.getMessage(), cause);
            }
        }
    }

    public static class OutputFieldBinding<D extends OutputDescriptor> extends OutputBinding<Field, D> {

        public OutputFieldBinding(Field field) {
            super(field);
        }

        @Override
        public Type getMemberType() {
            return getMember().getGenericType();
        }
        
        @Override
        public boolean validate() {
            if (!checkModifier()) {
                LOGGER.error("Field {} with output annotation can't be used, not public.", getMember());
                return false;
            }
            if (!checkType()) {
                LOGGER.error("Field {} with output annotation can't be used, unable to safely construct binding using field type", getMember());
                return false;
            }
            return true;
        }
        
        @Override
        public IData get(Object annotatedInstance) {
            Object value;
            try {
                value = getMember().get(annotatedInstance);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            }
            return value == null ? null : bindOutputValue(value);
        }
    }

    public static class OutputMethodBinding<D extends OutputDescriptor> extends OutputBinding<Method, D> {

        public OutputMethodBinding(Method method) {
            super(method);
        }

        @Override
        public Type getMemberType() {
            return getMember().getGenericReturnType();
        }
        
        @Override
        public boolean validate() {
            Method method = getMember();
            if (method.getParameterTypes().length != 0) {
                LOGGER.error("Method {} with output annotation can't be used, parameter count != 0", getMember());
                return false;
            }
            if (!checkModifier()) {
                LOGGER.error("Method {} with output annotation can't be used, not public", getMember());
                return false;
            }
            if (!checkType()) {
                LOGGER.error("Method {} with output annotation can't be used, unable to safely construct binding using method return type", getMember());
                return false;
            }
            return true;
        }
        
        @Override
        public IData get(Object annotatedInstance) {
            Object value;
            try {
                value = getMember().invoke(annotatedInstance);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                throw new RuntimeException(cause.getMessage(), cause);
            }
            return value == null ? null : bindOutputValue(value);
        }
    }
    
    // for example, a type reprecenting the <? extends Object> for types of List<? extends Object> or List
    public final Type NOT_PARAMETERIZED_TYPE = new WildcardType() {
        @Override public Type[] getUpperBounds() { return new Type[]{Object.class}; }
        @Override public Type[] getLowerBounds() { return new Type[0]; }  
    };
}
