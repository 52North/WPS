/**
 * Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server.r;

import java.io.File;
import java.io.FileNotFoundException;
import jdk.Exported;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.beans.factory.annotation.Autowired;
/**
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class RRepositoryTest extends AbstractITClass {

    @Autowired
    private RAlgorithmRepository rRepository;

    @Mock
    private ITransactionalAlgorithmRepository tRepo;

    @Mock
    private IAlgorithmRepository repo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RepositoryManager repositoryManager = new RepositoryManager();
        repositoryManager.setApplicationContext(wac);
        repositoryManager.init();
    }

    @Test
    public void falseWhenAddingNonExistingScript() {
        Assert.assertFalse(rRepository.addAlgorithm(new File("does-not-exist.R")));
    }

    @Test
    public void falseWhenAddingInvalidScriptContent() {
        Assert.assertFalse(rRepository.addAlgorithm(new File("just-two-kittens.R")));
    }


}
