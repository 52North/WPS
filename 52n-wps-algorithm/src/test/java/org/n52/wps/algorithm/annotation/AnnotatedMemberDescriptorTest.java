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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.n52.test.mock.MockEnum;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputFieldBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.InputMethodBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputFieldBinding;
import org.n52.wps.algorithm.annotation.AnnotationBinding.OutputMethodBinding;

/**
 *
 * @author tkunicki
 */
public class AnnotatedMemberDescriptorTest extends TestCase {
    
    // START - TEST DATA AS CLASS FIELDS AND METHODS
    
    // represent almost all cases: literal or complex data, input or output. with expections noted below
    public String stringField;
    public List<String> stringListField;
    public List<? extends String> stringExtendListField;
    public List<? super String> stringSuperListField;
    public List unboundListField;  // effectively List<? extends Object>
    
    // no polymorphism in test methods, will break use of methodMap
    public void setString(String stringParameter) {
        this.stringField = stringParameter;
    }
    public String getString() {
        return stringField;
    }
    public void setStringList(List<String> stringListParameter) {
        this.stringListField = stringListParameter;
    }
    public List<String> getStringList() {
        return this.stringListField;
    }
    public void setStringExtendList(List<? extends String> stringExtendListParameter) {
        this.stringExtendListField = stringExtendListParameter;
    }
    public List<? extends String> getStringExtendList() {
        return this.stringExtendListField;
    }
    public void setStringSuperList(List<? super String> stringSuperListParameter) {
        this.stringSuperListField = stringSuperListParameter;
    }
    public List<? super String> getStringSuperList() {
        return this.stringSuperListField;
    }
    public void setUnboundList(List unboundListParameter) {
        this.unboundListField = unboundListParameter;
    }
    public List getUnboundList() {
        return this.unboundListField;
    }
    
    // special case: enumerations for *inputs* have data type of String *unless* List for outputs
    public MockEnum enumField;
    public List<MockEnum> enumListField;
    /* NOT CURRENTLY SUPPORTED, need to be able to infer concrete type by reflection
    public List<? extends MockEnum> enumExtendsListField;
    public List<? super MockEnum> enumSuperListField;
    */
    
    // no polymorphism in test methods, will break use of methodMap
    public MockEnum getEnum() {
        return enumField; 
    }
    public void setEnum(MockEnum enumParameter) {
        this.enumField = enumParameter; 
    }
    public List<MockEnum> getEnumList() {
        return enumListField; 
    }
    public void setEnumList(List<MockEnum> enumListParameter) {
        this.enumListField = enumListParameter; 
    }
    /* NOT CURRENTLY SUPPORTED, need to be able to infer concrete type by reflection
    public List<? extends MockEnum> getEnumExtendList() {
        return enumExtendsListField; 
    }
    public void setEnumExtendList(List<? extends MockEnum> enumExtendsListParameter) {
        this.enumExtendsListField = enumExtendsListParameter; 
    }
    public List<? super MockEnum> getEnumSuperList() {
        return enumSuperListField; 
    }
    public void setEnumSuperList(List<? super MockEnum> enumSuperListParameter) {
        this.enumSuperListField = enumSuperListParameter; 
    }
    */
    
    // END - TEST DATA AS CLASS FIELDS AND METHODS
    
    Map<String, Method> methodMap;
    
    public AnnotatedMemberDescriptorTest(String testName) {
        super(testName);
        methodMap = new HashMap<String, Method>();
        for (Method method : AnnotationMemberDescriptorSample.class.getDeclaredMethods()) {
            methodMap.put(method.getName(), method);
        }
        methodMap = Collections.unmodifiableMap(methodMap);
    }
    
    private Method getSampleMethod(String name) throws NoSuchMethodException {
        Method method = methodMap.get(name);
        if (method == null) {
            throw new NoSuchMethodException(name);
        }
        return method;
    }
    
    private Field getSampleField(String name) throws NoSuchFieldException {
        return AnnotationMemberDescriptorSample.class.getDeclaredField(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testStringFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("stringField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateInputMember(memberDescriptor);
    }
    
    public void testStringFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("stringField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateOutputMember(memberDescriptor);
    }
    
    public void testStringSetter() throws NoSuchMethodException {
        Method method = getSampleMethod("setString");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateInputMember(memberDescriptor);
    }
    
    public void testStringGetter() throws NoSuchMethodException {
        Method method = getSampleMethod("getString");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateOutputMember(memberDescriptor);
    }
    
    private void validateInputMember(InputBinding memberDescriptor) {
        // data type matches member for inputs type *unless* member type is List
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // payload type matches data type, special handling reserved for enumerations
        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isMemberTypeList());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    private void validateOutputMember(OutputBinding memberDescriptor) {
        // data type matches member type for outputs
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // data type matches payload type, special handling reserved for enumerations
        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    public void testStringListFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("stringListField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateInputListMember(memberDescriptor);
    }
    
    public void testStringListFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("stringListField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateOutputListMember(memberDescriptor);
    }
    
    public void testStringListSetter() throws NoSuchMethodException {
        Method method = getSampleMethod("setStringList");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateInputListMember(memberDescriptor);
    }
    
    public void testStringListGetter() throws NoSuchMethodException {
        Method method = getSampleMethod("getStringList");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateOutputListMember(memberDescriptor);
    }
    
    private void validateInputListMember(InputBinding memberDescriptor) {
        // we extract the parameterized type of the list for inputs.  since member
        //  type is List<String> we expect String
        assertEquals(String.class, memberDescriptor.getType());
        // payload type matches data type, special handling reserved for enumerations
        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
        assertEquals(true, memberDescriptor.isMemberTypeList());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    private void validateOutputListMember(OutputBinding memberDescriptor) {
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    public void testStringExtendListFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("stringExtendListField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateExtendListInputMember(memberDescriptor);
    }
    
    public void testStringExtendListFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("stringExtendListField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateExtendListOutputMember(memberDescriptor);
    }
    
    public void testStringExtendListSetter() throws NoSuchMethodException {
        Method method = getSampleMethod("setStringExtendList");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateExtendListInputMember(memberDescriptor);
    }
    
    public void testStringExtendListGetter() throws NoSuchMethodException {
        Method method = getSampleMethod("getStringExtendList");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateExtendListOutputMember(memberDescriptor);
    }
    
    private void validateExtendListInputMember(InputBinding memberDescriptor) {
        // we extract the parameterized type of the list for inputs.  since member 
        //  type is List<? extends String> we expect a WildcardType of <? extends String>
        //  we need this information later to make sure we can safely assign an
        //  instance to the list with type safety (fail early behavior)
        Type type = memberDescriptor.getType();
        assertTrue(type instanceof WildcardType);
        WildcardType typeWildcard = (WildcardType)type;
        assertEquals(0, typeWildcard.getLowerBounds().length);
        assertEquals(1, typeWildcard.getUpperBounds().length);
        assertEquals(String.class, typeWildcard.getUpperBounds()[0]);
        // we extract the parameterized type of the list for inputs
        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
        assertEquals(true, memberDescriptor.isMemberTypeList());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    private void validateExtendListOutputMember(OutputBinding memberDescriptor) {
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    public void testStringSuperListFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("stringSuperListField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateSuperListInputMember(memberDescriptor);
    }
    
    public void testStringSuperListFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("stringSuperListField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateSuperListOutputMember(memberDescriptor);
    }
    
    public void testStringSuperListSetter() throws NoSuchMethodException {
        Method method = getSampleMethod("setStringSuperList");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateSuperListInputMember(memberDescriptor);
    }
    
    public void testStringSuperListGetter() throws NoSuchMethodException {
        Method method = getSampleMethod("getStringSuperList");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateSuperListOutputMember(memberDescriptor);
    }
    
    private void validateSuperListInputMember(InputBinding memberDescriptor) {
        // we extract the parameterized type of the list for inputs.  since member 
        //  type is List<? super String> we expect a WildcardType of <? super String>
        //  we need this information later to make sure we can safely assign an
        //  instance to the list with type safety (fail early behavior)
        Type type = memberDescriptor.getType();
        assertTrue(type instanceof WildcardType);
        WildcardType typeWildcard = (WildcardType)type;
        assertEquals(1, typeWildcard.getLowerBounds().length);
        assertEquals(String.class, typeWildcard.getLowerBounds()[0]);
        assertEquals(1, typeWildcard.getUpperBounds().length);
        assertEquals(Object.class, typeWildcard.getUpperBounds()[0]);
        // we extract the parameterized type of the list for inputs
        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
        assertEquals(true, memberDescriptor.isMemberTypeList());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    private void validateSuperListOutputMember(OutputBinding memberDescriptor) {
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    public void testUnboundListFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("unboundListField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateUnboundListInputMember(memberDescriptor);
    }
    
    public void testUnboundListFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("unboundListField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateUnboundListOutputMember(memberDescriptor);
    }
    
    public void testUnboundListSetter() throws NoSuchMethodException {
        Method method = getSampleMethod("setUnboundList");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateUnboundListInputMember(memberDescriptor);
    }
    
    public void testUnboundListGetter() throws NoSuchMethodException {
        Method method = getSampleMethod("getUnboundList");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateUnboundListOutputMember(memberDescriptor);
    }
    
    private void validateUnboundListInputMember(InputBinding memberDescriptor) {
//        // we extract the parameterized type of the list for inputs.  since member 
//        //  type is List<? super String> we expect a WildcardType of <? super String>
//        //  we need this information later to make sure we can safely assign an
//        //  instance to the list with type safety (fail early behavior)
//        Type type = memberDescriptor.getType();
//        assertTrue(type instanceof WildcardType);
//        WildcardType typeWildcard = (WildcardType)type;
//        assertEquals(0, typeWildcard.getLowerBounds().length);
//        assertEquals(1, typeWildcard.getUpperBounds().length);
//        assertEquals(Object.class, typeWildcard.getUpperBounds()[0]);
//        // we extract the parameterized type of the list for inputs
//        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
//        assertEquals(true, memberDescriptor.isMemberTypeList());
//        assertEquals(false, memberDescriptor.isTypeEnum());
         // we extract the parameterized type of the list for inputs.  since member 
        //  type is List<? extends Object> we expect a WildcardType of <? extends Object>
        //  we need this information later to make sure we can safely assign an
        //  instance to the list with type safety (fail early behavior)
        Type type = memberDescriptor.getType();
        assertTrue(type instanceof WildcardType);
        WildcardType typeWildcard = (WildcardType)type;
        assertEquals(0, typeWildcard.getLowerBounds().length);
        assertEquals(1, typeWildcard.getUpperBounds().length);
        assertEquals(Object.class, typeWildcard.getUpperBounds()[0]);
        // we extract the parameterized type of the list for inputs
        assertEquals(memberDescriptor.getType(), memberDescriptor.getPayloadType());
        assertEquals(true, memberDescriptor.isMemberTypeList());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    private void validateUnboundListOutputMember(OutputBinding memberDescriptor) {
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
    
    public void testEnumFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("enumField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateEnumInputMember(memberDescriptor);
    }
    
    public void testEnumFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("enumField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateEnumOutputMember(memberDescriptor);
    }
    
    public void testEnumSetter() throws NoSuchMethodException {
        Method method = getSampleMethod("setEnum");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateEnumInputMember(memberDescriptor);
    }
    
    public void testEnumGetter() throws NoSuchMethodException {
        Method method = getSampleMethod("getEnum");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateEnumOutputMember(memberDescriptor);
    }
    
    private void validateEnumInputMember(InputBinding memberDescriptor) {
        assertEquals(MockEnum.class, memberDescriptor.getType());
        // for all instances of Class<? extends Enum> the payload type is Class<String>
        //   as these will be bound with LiteralStringBinding
        assertEquals(String.class, memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isMemberTypeList());
        assertEquals(true, memberDescriptor.isTypeEnum());
    }
    
    private void validateEnumOutputMember(OutputBinding memberDescriptor) {
        assertEquals(MockEnum.class, memberDescriptor.getType());
        // for all instances of Class<? extends Enum> the payload type is Class<String>
        //   as these will be bound with LiteralStringBinding
        assertEquals(String.class, memberDescriptor.getPayloadType());
        assertEquals(true, memberDescriptor.isTypeEnum());
    }
    
    public void testEnumListFieldAsInput() throws NoSuchFieldException {
        Field field = getSampleField("enumListField");
        InputFieldBinding memberDescriptor = new InputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateEnumListInputMember(memberDescriptor);
    }
    
    public void testEnumListFieldAsOutput() throws NoSuchFieldException {
        Field field = getSampleField("enumListField");
        OutputFieldBinding memberDescriptor = new OutputFieldBinding(field);
        
        assertEquals(field, memberDescriptor.getMember());
        assertEquals(field.getGenericType(), memberDescriptor.getMemberType());
        
        validateEnumListOutputMember(memberDescriptor);
    }
    
    public void testEnumListSetter() throws NoSuchFieldException, NoSuchMethodException {
        Method method = getSampleMethod("setEnumList");
        InputMethodBinding memberDescriptor = new InputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericParameterTypes()[0], memberDescriptor.getMemberType());
        
        validateEnumListInputMember(memberDescriptor);
    }
    
    public void testEnumListGetter() throws NoSuchFieldException, NoSuchMethodException {
        Method method = getSampleMethod("getEnumList");
        OutputMethodBinding memberDescriptor = new OutputMethodBinding(method);
        
        assertEquals(method, memberDescriptor.getMember());
        assertEquals(method.getGenericReturnType(), memberDescriptor.getMemberType());
        
        validateEnumListOutputMember(memberDescriptor);
    }
    
    
    private void validateEnumListInputMember(InputBinding memberDescriptor) {
        assertEquals(MockEnum.class, memberDescriptor.getType());
        assertEquals(String.class, memberDescriptor.getPayloadType());
        assertEquals(true, memberDescriptor.isMemberTypeList());
        assertEquals(true, memberDescriptor.isTypeEnum());
    }
    
    private void validateEnumListOutputMember(OutputBinding memberDescriptor) {
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getType());
        // no special handling for outputs of member type List, member type matches data type
        assertEquals(memberDescriptor.getMemberType(), memberDescriptor.getPayloadType());
        assertEquals(false, memberDescriptor.isTypeEnum());
    }
 
}
