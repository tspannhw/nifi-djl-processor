package com.dataflowdeveloper.djl;

import ai.djl.modality.cv.DetectedObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 * Class: dog
 * Probabilties: 0.82268184
 * Coord:83.82353, 179.13997, 206.63783, 476.78754
 * @author tspann
 */
public class Result implements Serializable {

    private String detectedClass;
    private double probability;
    private int rank;

    private double boundingBoxX;
    private double boundingBoxY;
    private double boundBoxWidth;
    private double boundingBoxHeight;

    private int imageWidth;
    private int imageHeight;
    private int imageMinX;
    private int imageMinY;

    private String extraInformation;

    private DetectedObjects detection;

    public DetectedObjects getDetection() {
        return detection;
    }

    public void setDetection(DetectedObjects detection) {
        this.detection = detection;
    }

    /**
     *
     */
    public Result() {
        super();
    }

    /**
     *
     * @param detectedClass
     * @param probability
     * @param rank
     * @param boundingBoxX
     * @param boundingBoxY
     * @param boundBoxWidth
     * @param boundingBoxHeight
     * @param imageWidth
     * @param imageHeight
     * @param imageMinX
     * @param imageMinY
     * @param extraInformation
     */
    public Result(String detectedClass, double probability, int rank, double boundingBoxX, double boundingBoxY, double boundBoxWidth, double boundingBoxHeight, int imageWidth, int imageHeight, int imageMinX, int imageMinY, String extraInformation) {
        super();
        this.detectedClass = detectedClass;
        this.probability = probability;
        this.rank = rank;
        this.boundingBoxX = boundingBoxX;
        this.boundingBoxY = boundingBoxY;
        this.boundBoxWidth = boundBoxWidth;
        this.boundingBoxHeight = boundingBoxHeight;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageMinX = imageMinX;
        this.imageMinY = imageMinY;
        this.extraInformation = extraInformation;
    }

    public String getDetectedClass() {
        return detectedClass;
    }

    public void setDetectedClass(String detectedClass) {
        this.detectedClass = detectedClass;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getBoundingBoxX() {
        return boundingBoxX;
    }

    public void setBoundingBoxX(double boundingBoxX) {
        this.boundingBoxX = boundingBoxX;
    }

    public double getBoundingBoxY() {
        return boundingBoxY;
    }

    public void setBoundingBoxY(double boundingBoxY) {
        this.boundingBoxY = boundingBoxY;
    }

    public double getBoundBoxWidth() {
        return boundBoxWidth;
    }

    public void setBoundBoxWidth(double boundBoxWidth) {
        this.boundBoxWidth = boundBoxWidth;
    }

    public double getBoundingBoxHeight() {
        return boundingBoxHeight;
    }

    public void setBoundingBoxHeight(double boundingBoxHeight) {
        this.boundingBoxHeight = boundingBoxHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageMinX() {
        return imageMinX;
    }

    public void setImageMinX(int imageMinX) {
        this.imageMinX = imageMinX;
    }

    public int getImageMinY() {
        return imageMinY;
    }

    public void setImageMinY(int imageMinY) {
        this.imageMinY = imageMinY;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("detectedClass='" + detectedClass + "'")
                .add("probability=" + probability)
                .add("rank=" + rank)
                .add("boundingBoxX=" + boundingBoxX)
                .add("boundingBoxY=" + boundingBoxY)
                .add("boundBoxWidth=" + boundBoxWidth)
                .add("boundingBoxHeight=" + boundingBoxHeight)
                .add("imageWidth=" + imageWidth)
                .add("imageHeight=" + imageHeight)
                .add("imageMinX=" + imageMinX)
                .add("imageMinY=" + imageMinY)
                .add("extraInformation='" + extraInformation + "'")
                .toString();
    }


}