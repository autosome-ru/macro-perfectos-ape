package ru.autosome.macroape;

class PwmWithFilename {
    public final PWM pwm;
    public final String filename;
    public CanFindPvalue pvalue_calculation;

    public PwmWithFilename(PWM pwm, String filename) {
        this.pwm = pwm;
        this.filename = filename;
        this.pvalue_calculation = null; // One should specify this after creation
    }
}
