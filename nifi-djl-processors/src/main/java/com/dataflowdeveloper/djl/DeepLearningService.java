package com.dataflowdeveloper.djl;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.DetectedObjects;
import ai.djl.modality.cv.ImageVisualization;
import ai.djl.modality.cv.util.BufferedImageUtils;
import ai.djl.mxnet.zoo.MxModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// scalastyle:off
// scalastyle:on

/**
 * see https://github.com/awslabs/djl/
 * https://github.com/tspannhw/nifi-mxnetinference-processor
 */
public class DeepLearningService {

    /**
     * logging
     **/
    private final static Logger logger = LoggerFactory.getLogger(DeepLearningService.class);

    /**
     * predict
     * @return
     * @throws IOException
     * @throws ModelException
     * @throws TranslateException
     */
    public List<Result> predict(BufferedImage img, String size, String backbone, String flavor, String dataset)
            throws IOException, ModelException, TranslateException {

        List results = new ArrayList<Result>();

        if ( img == null ) {
         return results;
        }

        Map<String, String> criteria = new ConcurrentHashMap<>();
        criteria.put("size", size); //"512"
        criteria.put("backbone", backbone); //"resnet50");
        criteria.put("flavor", flavor); //"v1");
        criteria.put("dataset", dataset); //"voc");

        try (ZooModel<BufferedImage, DetectedObjects> model =
                     MxModelZoo.SSD.loadModel(criteria, new ProgressBar())) {
            try (Predictor<BufferedImage, DetectedObjects> predictor = model.newPredictor()) {
                DetectedObjects detection = predictor.predict(img);

                int rank = 1;

                if ( detection != null && detection.topK(5) != null && detection.getNumberOfObjects() > 0) {
                    for (Classifications.Classification classification : detection.items()) {
                        try {
                            Result result = new Result();

                            if ( classification !=null ) {
                                result.setDetectedClass(classification.getClassName());
                                result.setProbability(classification.getProbability());
                            }

                            if ( classification !=null && classification.getClass() != null && classification.getClass().getName() != null &&
                                    classification.getClass().getName().contains("DetectedObject")) {
                                DetectedObjects.DetectedObject detectObject = (DetectedObjects.DetectedObject) classification;

                                if ( detectObject != null && detectObject.getBoundingBox() != null && detectObject.getBoundingBox().getBounds() != null) {
                                    result.setBoundingBoxX(detectObject.getBoundingBox().getBounds().getX());
                                    result.setBoundBoxWidth(detectObject.getBoundingBox().getBounds().getWidth());
                                    result.setBoundingBoxY( detectObject.getBoundingBox().getBounds().getY());
                                    result.setBoundingBoxHeight(detectObject.getBoundingBox().getBounds().getHeight() );
                                }
                                result.setImageHeight(img.getHeight());
                                result.setImageWidth(img.getWidth());
                                result.setImageMinX(img.getMinX());
                                result.setImageMinY(img.getMinY());
                            }

                            result.setDetection(detection);
                            result.setRank(rank);
                            results.add(result);
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