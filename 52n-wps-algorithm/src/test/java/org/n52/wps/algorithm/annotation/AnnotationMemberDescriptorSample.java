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

import java.util.List;
import org.n52.test.mock.MockEnum;

/**
 *
 * @author tkunicki
 */
public class AnnotationMemberDescriptorSample {
  
    // TEST IMPLEMENTATION NOTE:  no polymorphism in sample methods, the reflection utilities
    // used for generic method lookup used in the unit tests (polymorphism is ok in
    // actual implementations as the annotations are what drive method lookups)
    
    // Represents almost all cases for fields: literal or complex data, input or output. with expections noted below.
    // We are using String instances, but any literal or complex payload type could be swapped out.
    public String stringField;
    public List<String> stringListField;
    public List<? extends String> stringExtendListField;
    public List<? super String> stringSuperListField;
    public List unboundListField;  // effectively List<? extends Object>
    
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
    
    // Special case: enumerations for *inputs* have payload type of String so that
    // Enumerations can be bound with  LiteralStringBinding instances.  Exception
    // is List of enums for outputs.
    public MockEnum enumField;
    public List<MockEnum> enumListField;
/* NOT CURRENTLY SUPPORTED, need to be able to infer concrete type by reflection
    public List<? extends MockEnum> enumExtendsListField;
    public List<? super MockEnum> enumSuperListField;
*/
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
}
