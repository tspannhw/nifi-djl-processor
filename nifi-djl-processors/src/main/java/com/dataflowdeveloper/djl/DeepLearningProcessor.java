package com.dataflowdeveloper.djl;

import ai.djl.ModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.translate.TranslateException;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@EventDriven
@SupportsBatching
@SideEffectFree
@Tags({ "djl", "inference", "models", "classification", "computer vision", "image", "ssd", "object detection", "Java Deep Learning", "deep learning" })
@CapabilityDescription("Run Deep Learning Models with DJL")
@SeeAlso({})
@WritesAttributes({ @WritesAttribute(attribute = "className", description = "The image x, y, bounding box, probabilities and class name") })
/**
 *
 * @author tspann  Timothy Spann
 *
 */
public class DeepLearningProcessor extends AbstractProcessor {

    public static final String DATASET_NAME = "Dataset";
    public static final String SIZE_NAME = "Size";
    public static final String BACKBONE_NAME = "Backbone";
    public static final String FLAVOR_NAME = "Flavor";
    
    // properties
    public static final PropertyDescriptor DATASET = new PropertyDescriptor.Builder().name(DATASET_NAME)
            .description("Dataset name").required(true).defaultValue("voc")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();

    public static final PropertyDescriptor SIZE = new PropertyDescriptor.Builder().name(SIZE_NAME)
            .description("Size").required(true).defaultValue("512")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();

    public static final PropertyDescriptor BACKBONE = new PropertyDescriptor.Builder().name(BACKBONE_NAME)
            .description("Backbone").required(true).defaultValue("resnet50")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();

    public static final PropertyDescriptor FLAVOR = new PropertyDescriptor.Builder().name(FLAVOR_NAME)
            .description("Flavor").required(true).defaultValue("v1")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();

    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("Successfully determined image.").build();
    public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("Failed to determine image.").build();
    public static final String FILENAME = "filename";

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    private DeepLearningService service;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(DATASET);
        descriptors.add(FLAVOR);
        descriptors.add(SIZE);
        descriptors.add(BACKBONE);

        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        service = new DeepLearningService();
        return;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            flowFile = session.create();
        }
        try {
            flowFile.getAttributes();

            // read all bytes of the flowfile (tensor requires whole image)
            // read all parameters

            String dataset = flowFile.getAttribute(DATASET_NAME);
            if (dataset == null) {
                dataset = context.getProperty(DATASET_NAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            final String finalDataset = dataset;

            String size = flowFile.getAttribute(SIZE_NAME);
            if (size == null) {
                size = context.getProperty(SIZE_NAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            final String finalSize = size;

            String backbone = flowFile.getAttribute(BACKBONE_NAME);
            if (backbone == null) {
                backbone = context.getProperty(BACKBONE_NAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            final String finalBackbone = backbone;

            String flavor = flowFile.getAttribute(FLAVOR_NAME);
            if (flavor == null) {
                flavor = context.getProperty(FLAVOR_NAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            final String finalFlavor = flavor;

            String filename = flowFile.getAttribute(FILENAME);
            if (filename == null) {
                filename = context.getProperty(FILENAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            final String finalFilename = filename;

            try {
                final HashMap<String, String> attributes = new HashMap<String, String>();

                flowFile = session.write(flowFile, new StreamCallback() {
                    @Override
                    public void process(final InputStream input, final java.io.OutputStream out) throws IOException {

                        if ( input == null) {
                            return;
                        }
                        byte[] byteArray = IOUtils.toByteArray(input);

                        List<Result> results = null;
                        InputStream in = null;
//                        BufferedImage img = null;
                        Image img = null;
                        String deepLearningLabel = null;

                        try {
                            in = new ByteArrayInputStream(byteArray);
                            //img = ImageIO.read(in);
                            img = ImageFactory.getInstance().fromInputStream(in);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            if ( in != null && img != null) {
                                results = service.predict(img, finalSize, finalBackbone, finalFlavor, finalDataset);
                            }
                        } catch (ModelException e) {
                            e.printStackTrace();
                        } catch (TranslateException e) {
                            e.printStackTrace();
                        }

                        if (results != null) {
                            getLogger().debug(String.format("Found %d results", new Object[] { results.size() }));

                            int i = 1;

                            for (Result result : results) {
                                attributes.put(String.format("class_%d", i), result.getDetectedClass() );
                                deepLearningLabel  =  result.getDetectedClass();

                                try {
                                    attributes.put(String.format("probability_%d",i), String.format("%.2f", result.getProbability()));
                                    attributes.put(String.format("boundingbox_width_%d",i), String.format("%.2f",result.getBoundBoxWidth()));
                                    attributes.put(String.format("boundingbox_height_%d",i), String.format("%.2f",result.getBoundingBoxHeight()));
                                    attributes.put(String.format("boundingbox_x_%d",i), String.format("%.2f",result.getBoundingBoxX()));
                                    attributes.put(String.format("boundingbox_y_%d",i), String.format("%.2f",result.getBoundingBoxY()));

                                    attributes.put(String.format("image_height_%d",i), String.format("%d",result.getImageHeight()));
                                    attributes.put(String.format("image_min_x_%d",i), String.format("%d",result.getImageMinX()));
                                    attributes.put(String.format("image_min_y_%d",i), String.format("%d",result.getImageMinY()));
                                    attributes.put(String.format("image_width_%d",i), String.format("%d",result.getImageWidth()));
                                    attributes.put(String.format("rank_%d",i), String.format("%d",result.getRank()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Image newImage = img.duplicate(Image.Type.TYPE_INT_ARGB);
                                newImage.drawBoundingBoxes(result.getDetection());
                                newImage.save(out, "png");
                                attributes.put("filename", finalFilename + "." + deepLearningLabel + ".png");
                                i++;
                            }
                        }
                    }
                });

                if (attributes.size() == 0) {
                    session.transfer(flowFile, REL_FAILURE);
                } else {
                    flowFile = session.putAllAttributes(flowFile, attributes);

                    /// Add a new changed image with boxes
                    session.transfer(flowFile, REL_SUCCESS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ProcessException(e);
            }

            session.commit();
        } catch (final Throwable t) {
            getLogger().error("Unable to process DJL Processor file " + t.getLocalizedMessage());
            throw new ProcessException(t);
        }
    }
}