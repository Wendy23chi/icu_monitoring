package com.icumonitoring;

import java.util.Random;

/**
 *
 * @author defalt
 */
public class Randomizer {
    
    private final static Random RANDOM = new Random(System.currentTimeMillis());
    
    /**
     * This method returns a random Integer in a given range
     *
     * @param min
     * @param max
     * @return
     */
    public static Integer getRandomInteger(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * This method returns a random Double in a given range
     *
     * @param min
     * @param max
     * @return
     */
    public static Double getRandomDouble(Double min, Double max) {
        return ((Math.random() * (max - min)) + min);
    }

    /**
     * This method returns a random Integer in a given range with a tendency to
     * a certain point
     *
     * @param min
     * @param max
     * @param bias
     * @param stddev
     * @return
     */
    public static Integer getBiasedRandomInteger(int min, int max, int bias, Double stddev) {
        return (int) Math.max(min, Math.min(max, (int) bias + RANDOM.nextGaussian() * stddev));
    }
    
    /**
     * This method returns a random Double in a given range with a tendency to
     * a certain point
     *
     * @param min
     * @param max
     * @param bias
     * @param stddev
     * @return
     */
    public static Double getBiasedRandomDouble(Double min, Double max, Double bias, Double stddev) {
        return Math.max(min, Math.min(max, bias + RANDOM.nextGaussian() * stddev));
    }

}
