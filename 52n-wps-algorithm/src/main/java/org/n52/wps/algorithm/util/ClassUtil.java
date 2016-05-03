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
