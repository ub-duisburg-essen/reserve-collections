package unidue.rc.io;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.LegalEntity;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;

/**
 * @author Nils Verheyen
 */
public class TestLegalEntityIO extends Assert {

    private static final Logger LOG = LoggerFactory.getLogger(TestLegalEntityIO.class);

    @BeforeClass
    public void setup() {
        LOG.info("running " + this.getClass().getName() + " tests");
    }

    @AfterClass
    public void shutdown() {
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }

    @Test
    public void parse() {
        Strategy strategy = new AnnotationStrategy();
        Serializer s = new Persister(strategy);
        URL input = TestLegalEntityIO.class.getResource("legalEntity.xml");
        try {
            LOG.debug("parsing legalentity from url " + input);
            boolean isValid = s.validate(LegalEntity.class, input.openStream());
            assertTrue(isValid);
        } catch (Exception e) {
            LOG.error("class XML schema does not fully match", e);
        }
    }
}
