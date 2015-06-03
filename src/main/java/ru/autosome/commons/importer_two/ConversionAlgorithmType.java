package ru.autosome.commons.importer_two;

public enum ConversionAlgorithmType {
    pcm2pwm, pcm2ppm, ppm2pwm, pcm2pwmMARA, pwm2dipwm, discretize, reformat;

    public abstract ConversionAlgorithm getAlgorithm();
}
