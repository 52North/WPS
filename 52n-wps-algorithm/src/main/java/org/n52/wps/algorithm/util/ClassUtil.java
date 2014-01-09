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
package org.n52.wps.algorithm.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tkunicki
 */
public class ClassUtil {

    private final static Map<Class<?>, Class<?>> TO_WRAPPER;
    private final static Map<Class<?>, Class<?>> FROM_WRAPPER;

    static {
        BiMap<Class<?>, Class<?>> map = HashBiMap.create();
        map.put(Float.TYPE, Float.class);
        map.put(Double.TYPE, Double.class);
        map.put(Byte.TYPE, Byte.class);
        map.put(Short.TYPE, Short.class);
        map.put(Integer.TYPE, Integer.class);
        map.put(Long.TYPE, Long.class);
        map.put(Character.TYPE, Character.class);
        map.put(Boolean.TYPE, Boolean.class);
        TO_WRAPPER = Collections.unmodifiableMap(map);
        FROM_WRAPPER = Collections.unmodifiableMap(map.inverse());
    }


    public static <T extends Enum<T>> List<T> convertStringToEnumList(Class<T> enumType, List<String> stringList) {
        List<T> enumList = new ArrayList<T>();
        for (String string : stringList) {
            enumList.add(Enum.valueOf(enumType, string));
        }
        return enumList;
    }

    public static <T extends Enum<T>> List<String> convertEnumToStringList(Class<T> enumType) {
        ArrayList stringList = null;
        T[] constants = enumType.getEnumConstants();
        if (constants != null && constants.length > 0) {
            stringList = new ArrayList(constants.length);
            for (T constant : constants) {
                stringList.add(constant.name());
            }
        }
        return stringList;
    }

    public static <T extends Enum<T>> String[] convertEnumToStringArray(Class<T> enumType) {
        List<String> stringList = convertEnumToStringList(enumType);
        return stringList == null ? null :
            stringList.toArray(new String[stringList.size()]);
    }

    public static boolean isWrapper(Class<?> clazz) {
        return FROM_WRAPPER.containsKey(clazz);
    }

    public static Class<?> unwrap(Class<?> clazz) {
        Class<?> unwrapped = FROM_WRAPPER.get(clazz);
        if (unwrapped == null)
            return clazz;
        return unwrapped;

    }

    public static Class<?> wrap(Class<?> clazz) {
        Class<?> wrapped = TO_WRAPPER.get(clazz);
        if (wrapped == null)
            return clazz;
        return wrapped;
    }

}
