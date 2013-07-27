package ru.autosome.jMacroape;

import java.util.HashMap;

public class PCM extends PM {
    public PCM(Double[][] matrix, Double[] background, String name) throws IllegalArgumentException {
        super(matrix, background, name);
    }
    public PCM(PM pm) {
        super(pm);
    }
    public double count() {
      return matrix[0][0] + matrix[0][1] + matrix[0][2] + matrix[0][3];
    }
    public PWM to_pwm(){
      return PCM2PWMConverter.convert(this,new HashMap<String,Object>());
    }
}
