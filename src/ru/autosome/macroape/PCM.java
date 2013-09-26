package ru.autosome.macroape;

public class PCM extends PM {
    public PCM(double[][] matrix, String name) throws IllegalArgumentException {
        super(matrix, name);
    }

    public double count() {
        double[] pos = matrix[0];
        return pos[0] + pos[1] + pos[2] + pos[3];
    }

    public PWM to_pwm(BackgroundModel background) {
        PCM2PWMConverter converter = new PCM2PWMConverter(this);
        converter.background = background;
        return converter.convert();
    }
}
