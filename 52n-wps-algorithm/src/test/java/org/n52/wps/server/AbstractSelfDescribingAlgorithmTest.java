package org.n52.wps.server;

import java.util.HashMap;
import org.n52.test.mock.MockUtil;
import junit.framework.TestCase;
import net.opengis.wps.x100.ProcessDescriptionType;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 * @author tkunicki
 */
public class AbstractSelfDescribingAlgorithmTest extends TestCase {
    
    public AbstractSelfDescribingAlgorithmTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.getMockConfig();
    }

    public void testComplexSelfDescribingAlgorithmUsingDescriptor() {
        IAlgorithm algorithm = new ComplexSelfDescribingAlgorithmUsingDescriptor();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testComplexAnnotatedAlgorithm() {
        IAlgorithm algorithm = new ComplexAnnotatedAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringReverseSelfDescribingAlgorithm() {
        IAlgorithm algorithm = new StringReverseSelfDescribingAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringReverseAnnotatedAlgorithm() {
        IAlgorithm algorithm = new StringReverseAnnotatedAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringJoinSelfDescribingAlgorithm() {
        IAlgorithm algorithm = new StringJoinSelfDescribingAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringJoinAnnotatedAlgorithm() {
        IAlgorithm algorithm = new StringJoinAnnotatedAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    private void printAlgorithmProcessDescription(IAlgorithm algorithm) {
        System.out.println();
        System.out.println(" ### DescribeProcess for " + algorithm.getClass().getName() + " ###");
        System.out.println(getXMLAsStringFromDescription(algorithm.getDescription()));
        System.out.println();
    }

    private String getXMLAsStringFromDescription(ProcessDescriptionType decription) {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSaveOuter();
        HashMap ns = new HashMap();
        ns.put("http://www.opengis.net/wps/1.0.0", "wps");
        ns.put("http://www.opengis.net/ows/1.1", "ows");
        options.setSaveNamespacesFirst().
                setSaveSuggestedPrefixes(ns).
                setSaveAggressiveNamespaces();
        return decription.xmlText(options);
    }

}
