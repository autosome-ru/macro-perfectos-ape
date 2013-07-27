package ru.autosome.jMacroape;

/**
 * Created with IntelliJ IDEA.
 * User: MSI
 * Date: 7/24/13
 * Time: 8:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class PCM extends PM {
    public PCM(double[][] matrix, double[] background, String name) throws IllegalArgumentException {
        super(matrix, background, name);
    }
    public PCM(PM pm) {
        super(pm);
    }
}
