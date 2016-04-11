package unidue.rc.io;


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
