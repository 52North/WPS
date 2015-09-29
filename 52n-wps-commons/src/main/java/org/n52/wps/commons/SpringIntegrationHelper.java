/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * A hacky utility class which provides mechanism to autowire fields of non-spring-managed beans.
 *
 * @author Henning Bredel <h.bredel@52north.org>
 *
 * @since 4.0.0
 */
public final class SpringIntegrationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringIntegrationHelper.class);

    private static AutowireCapableBeanFactory factory;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public static void autowireBean(Object bean) {
        if (factory == null) {
            LOGGER.warn("could not autowire bean as AutowireCapableBeanFactory has not been set.");
        } else {
            factory.autowireBean(bean);
        }
    }

    public void init() {
        LOGGER.info("Init {} with {}", getClass().toString(), beanFactory);
        factory = beanFactory;
    }

}
