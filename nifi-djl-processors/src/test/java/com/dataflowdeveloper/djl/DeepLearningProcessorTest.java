package com.dataflowdeveloper.djl;


import ai.djl.modality.Classifications;
import jdk.nashorn.internal.objects.NativeArray;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 *
 */
public class DeepLearningProcessorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(DeepLearningProcessor.class);
    }


    private String pathOfResource(String name) throws URISyntaxException {
        URL r = this.getClass().getClassLoader().getResource(name);
        URI uri = r.toURI();
        return Paths.get(uri).toAbsolutePath().getParent().toString();
    }

    private void runAndAssertHappy() {
        testRunner.setValidateExpressionUsage(false);
        testRunner.run();
        testRunner.assertValid();

        testRunner.assertAllFlowFilesTransferred(DeepLearningProcessor.REL_SUCCESS);
        List<MockFlowFile> successFiles = testRunner.getFlowFilesForRelationship(DeepLearningProcessor.REL_SUCCESS);

        for (MockFlowFile mockFile : successFiles) {
            assertEquals("car", mockFile.getAttribute("class_1"));
            assertEquals("1.00", mockFile.getAttribute("probability_1"));

            System.out.println("Size:" +             mockFile.getSize() ) ;
            Map<String, String> attributes =  mockFile.getAttributes();

            for (String attribute : attributes.keySet()) {
                System.out.println("Attribute:" + attribute + " = " + mockFile.getAttribute(attribute));
            }
        }
    }

    @Test
    public void testQA() throws Exception {
        QAService q = new QAService();
        String answer = q.predict();
        System.out.println("Answer: {}" + answer);
    }

    @Test
    public void testSentiment() throws Exception {
        SentimentAnalysisService s = new SentimentAnalysisService();
        List<Result> results = s.predict( "Cloudera is the best company in the world.  Awesome" );
        if ( results != null && results.size() > 0) {
            for (Result result : results) {
                if ( result != null) {
                    System.out.println("Class:" + result.getDetectedClass() + " p=" + result.getProbability()
                    + " p100=" +  result.getProbabilityPercentage());
                }
            }
        }
    }

    @Test
    public void testProcessor() throws Exception {

        java.io.File resourcesDirectory = new java.io.File("src/test/resources");
        System.out.println(resourcesDirectory.getAbsolutePath());

        testRunner.setProperty(DeepLearningProcessor.BACKBONE, "resnet50");
        testRunner.setProperty(DeepLearningProcessor.DATASET, "voc");
        testRunner.setProperty(DeepLearningProcessor.SIZE, "512");
        testRunner.setProperty(DeepLearningProcessor.FLAVOR, "v1");
        testRunner.enqueue(this.getClass().getClassLoader().getResourceAsStream("2019-12-10_1611.jpg"));

        runAndAssertHappy();
    }
}