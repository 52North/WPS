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

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AnnotatedAlgorithmIntrospectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    public AnnotatedAlgorithmIntrospector instance;

    @Test
    public void testClassWithNoExecuteAnnotation() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(CoreMatchers.containsString("No execute method binding"));
        instance = new AnnotatedAlgorithmIntrospector(ClassWithNoExecuteAnnotation.class);
    }
}
