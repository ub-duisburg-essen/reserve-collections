package unidue.rc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.system.BookUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nils on 18.09.15.
 */
public class TestBookUtils extends Assert {

    private static final Logger LOG = LoggerFactory.getLogger(TestBookUtils.class);

    private List<ValidSignatureCombination> testData;

    @BeforeClass
    public void setup() {
        testData = new ArrayList<>();
        testData.add(new ValidSignatureCombination("D00 HQLW23333(10)-CS,941,2+100", "HQLW23333(10)-CS,941,2", "10", "CS,941,2"));
        testData.add(new ValidSignatureCombination("HQLW23333(10)-C,9,2+100", "HQLW23333(10)-C,9,2", "10", "C,9,2"));
        testData.add(new ValidSignatureCombination("HQLW23333+100", "HQLW23333", null, null));
        testData.add(new ValidSignatureCombination("HQLW23333", "HQLW23333", null, null));
        testData.add(new ValidSignatureCombination("E10 HQLW23333", "HQLW23333", null, null));
        testData.add(new ValidSignatureCombination("E10 HQL23333_d", "HQL23333", null, null));
        testData.add(new ValidSignatureCombination("E10 HQL233(10)_d", "HQL233(10)", "10", null));
        testData.add(new ValidSignatureCombination("E10 HQL233(54)+30", "HQL233(54)", "54", null));
        testData.add(new ValidSignatureCombination("E10 HQL233(6)-1_d", "HQL233(6)-1", "6", "1"));
        testData.add(new ValidSignatureCombination("E10 HQL233(10)", "HQL233(10)", "10", null));
        testData.add(new ValidSignatureCombination("E10 HQLW23333-DE,200_d", "HQLW23333-DE,200", null, "DE,200"));
        testData.add(new ValidSignatureCombination("E10 HQLW2333-DE,200,40", "HQLW2333-DE,200,40", null, "DE,200,40"));
        testData.add(new ValidSignatureCombination("E10 HQLW2333-DE,200,40,1956,3", "HQLW2333-DE,200,40,1956,3", null, "DE,200,40,1956,3"));
        testData.add(new ValidSignatureCombination("E10 HQLW23335-DE,200,40,1956,3", "HQLW23335-DE,200,40,1956,3", null, "DE,200,40,1956,3"));
    }

    @Test
    public void testValidSignature() {
        testData.forEach(data -> assertTrue(BookUtils.isMonographySignatureValid(data.sig)));
    }

    @Test
    public void testValidNotation() {
        testData.forEach(data -> testValidNotation(data.sig, data.notation));
    }

    @Test
    public void testValidEdition() {
        testData.forEach(data -> testValidEdition(data.sig, data.edition));
    }

    @Test
    public void testValidVolume() {
        testData.forEach(data -> testValidVolume(data.sig, data.volume));
    }

    private void testValidEdition(String signature, String expected) {

        String actual = BookUtils.getEdition(signature);
        try {
            assertEquals(actual, expected);
        } catch (AssertionError e) {
            LOG.error("actual " + actual + " expected " + expected + " for sig " + signature);
            throw e;
        }
    }

    private void testValidVolume(String signature, String expected) {

        String actual = BookUtils.getVolume(signature);
        try {
            assertEquals(actual, expected);
        } catch (AssertionError e) {
            LOG.error("actual " + actual + " expected " + expected + " for sig " + signature);
            throw e;
        }
    }

    private void testValidNotation(String signature, String notation) {
        String normalized = BookUtils.getNormalized(signature);
        try {
            assertEquals(normalized, notation);
        } catch (AssertionError e) {
            LOG.error("actual " + normalized + " expected " + notation + " for sig " + signature);
            throw e;
        }
    }


    private static class ValidSignatureCombination {
        String sig;
        String notation;
        String edition;
        String volume;

        public ValidSignatureCombination(String sig, String notation, String edition, String volume) {
            this.sig = sig;
            this.notation = notation;
            this.edition = edition;
            this.volume = volume;
        }
    }
}
