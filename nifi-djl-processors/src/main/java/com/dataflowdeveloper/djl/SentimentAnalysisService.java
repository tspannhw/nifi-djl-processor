package com.dataflowdeveloper.djl;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentiment
 *
 * <p>See:
 * DistilBERT model trained by HuggingFace using PyTorch
 *
 https://github.com/awslabs/djl/blob/master/examples/src/main/java/ai/djl/examples/inference/SentimentAnalysis.java
 https://github.com/awslabs/djl/blob/master/examples/docs/sentiment_analysis.md
 */
// scalastyle:off
// scalastyle:on

public class SentimentAnalysisService {

    /**
     *
     * @param message
     * @return
     * @throws IOException
     * @throws TranslateException
     * @throws ModelException
     */
    public  List<Result> predict(String message) throws IOException, TranslateException, ModelException {
        List results = new ArrayList<Result>();

        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                        .setTypes(String.class, Classifications.class)
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, Classifications> model = ModelZoo.loadModel(criteria)) {
            try (Predictor<String, Classifications> predictor = model.newPredictor()) {
                Classifications classifications = predictor.predict(message);
                if ( classifications == null) {
                    return results;
                }
                else {
                    System.out.println("Classifications:" + classifications.items().size());
                    System.out.println("Classifications:" + classifications.toString());
                    System.out.println("Classifications:" + classifications.topK(5));
                    if ( classifications.items() != null && classifications.items().size() > 0) {
                        for (Classifications.Classification classification : classifications.items()) {
                            try {
                                Result result = new Result();

                                if (classification != null) {
                                    result.setDetectedClass( classification.getClassName() );
                                    result.setProbability( classification.getProbability() );
                                    result.setProbabilityPercentage( (classification.getProbability()*100) );
                                }

                                results.add( result );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return results;
    }
}
