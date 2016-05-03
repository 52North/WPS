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

import com.google.common.base.Preconditions;

/**
 *
 * @author tkunicki
 */
public abstract class Descriptor {

    private final String identifier;
    private final String title;
    private final String abstrakt; // want 'abstract' but it's a java keyword

	Descriptor(Builder<? extends Builder<?>> builder) {
        this.identifier = builder.identifier;
        this.title = builder.title;
        this.abstrakt = builder.abstrakt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasTitle() {
        return title != null && title.length() > 0;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasAbstract() {
        return abstrakt != null && abstrakt.length() > 0;
    }

    public String getAbstract() {
        return abstrakt;
    }

    public static abstract class Builder<B extends Builder<B>> {

        private final String identifier;
        private String title;
        private String abstrakt; // want 'abstract' but it's a java keyword

        public Builder(String identifier) {
            Preconditions.checkArgument(
                    !(identifier == null || identifier.isEmpty()),
                    "identifier may not be null or an empty String");
            this.identifier = identifier;
        }

        public B title(String title) {
            this.title = title;
            return self();
        }

        // want 'abstract' but it's a java keyword
        public B abstrakt(String abstrakt) {
            this.abstrakt = abstrakt;
            return self();
        }

        protected abstract B self();
    }
}
