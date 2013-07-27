package ru.autosome.jMacroape;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: MSI
 * Date: 7/25/13
 * Time: 6:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class HashExtensions {
    public static double sum_values(Map<Double, Double> hsh) {
        double result = 0;
        for(Map.Entry<Double, Double> entry: hsh.entrySet()) {
            result += entry.getValue();
        }
        return result;
    }

}
