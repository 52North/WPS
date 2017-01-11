/*
 * Copyright (C) 2010-2017 52°North Initiative for Geospatial Open Source
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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.beans.factory.annotation.Autowired;
/**
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class TransactionalRRepositoryTest extends AbstractITClass {

    @Autowired
    private RAlgorithmRepository rRepository;

    @Autowired
    private ScriptFileRepository scriptRepo;

    @Test
    public void falseWhenAddingNonExistingScript() {
        Assert.assertFalse(rRepository.addAlgorithm(new File("does-not-exist.R")));
    }

    @Test
    public void falseWhenAddingInvalidScript() {
        Assert.assertFalse(rRepository.addAlgorithm(new File("just-two-kittens.R")));
    }

    @Test
    public void trueWhenAddingValidScript() {
        final File file = TestUtil.loadFile("/uniform.R");
        Assert.assertTrue(rRepository.addAlgorithm(file));
    }

    @Test
    @Ignore("TODO Not implemented yet")
    public void trueRemovingKnownScript() {
        final File file = TestUtil.loadFile("/uniform.R");
        Assert.assertTrue(rRepository.removeAlgorithm(file));
        Assert.assertFalse(scriptRepo.isRegisteredScriptFile(file));
    }


}
