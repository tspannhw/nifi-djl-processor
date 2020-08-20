package com.dataflowdeveloper.djl;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// scalastyle:off
// scalastyle:on

/**
 * see https://github.com/awslabs/djl/
 * https://github.com/tspannhw/nifi-mxnetinference-processor
 * upgraded from 0.2 to 0.6
 */
public class DeepLearningService {

    /**
     * logging
     **/
    private final static Logger logger = LoggerFactory.getLogger( DeepLearningService.class );

    /**
     * predict
     *
     * @return
     * @throws IOException
     * @throws ModelException
     * @throws TranslateException
     */
    public List<Result> predict(ai.djl.modality.cv.Image img, String size, String backbone, String flavor, String dataset)
            throws IOException, ModelException, TranslateException {

        List results = new ArrayList<Result>();

        if (img == null) {
            return results;
        }

        Map<String, Object> options = null;

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication( Application.CV.OBJECT_DETECTION )
                        .setTypes( Image.class, DetectedObjects.class )
                        .optFilter( "backbone", backbone )
                        .optOptions( options )
                        .optProgress( new ProgressBar() )
                        .build();
        try (ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel( criteria )) {
            try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
                DetectedObjects detection = predictor.predict( img );

                int rank = 1;

                if (detection != null && detection.topK( 5 ) != null && detection.getNumberOfObjects() > 0) {
                    for (Classifications.Classification classification : detection.items()) {
                        try {
                            Result result = new Result();

                            if (classification != null) {
                                result.setDetectedClass( classification.getClassName() );
                                result.setProbability( classification.getProbability() );
                            }

                            if (classification != null && classification.getClass() != null && classification.getClass().getName() != null &&
                                    classification.getClass().getName().contains( "DetectedObject" )) {
                                DetectedObjects.DetectedObject detectObject = (DetectedObjects.DetectedObject) classification;

                                if (detectObject != null && detectObject.getBoundingBox() != null && detectObject.getBoundingBox().getBounds() != null) {
                                    result.setBoundingBoxX( detectObject.getBoundingBox().getBounds().getX() );
                                    result.setBoundBoxWidth( detectObject.getBoundingBox().getBounds().getWidth() );
                                    result.setBoundingBoxY( detectObject.getBoundingBox().getBounds().getY() );
                                    result.setBoundingBoxHeight( detectObject.getBoundingBox().getBounds().getHeight() );
                                }
                                result.setImageHeight( img.getHeight() );
                                result.setImageWidth( img.getWidth() );
                            }

                            result.setDetection( detection );
                            result.setRank( rank );
                            results.add( result );
                            rank++;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return results;
            }
        }
    }
}