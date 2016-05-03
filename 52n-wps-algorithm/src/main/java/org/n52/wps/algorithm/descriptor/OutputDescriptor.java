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
package org.n52.wps.algorithm.descriptor;

import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class OutputDescriptor<T extends Class<? extends IData>> extends BoundDescriptor<T> {

	OutputDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IData>> extends BoundDescriptor.Builder<B,T>{

        protected Builder(String identifier, T binding) {
            super(identifier, binding);
        }

        public abstract OutputDescriptor<T> build();
    }
}
