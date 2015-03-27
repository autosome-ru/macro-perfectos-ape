package ru.autosome.commons.motifModel.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.types.PositionCountModel;

public class PCM extends PM implements PositionCountModel {
  public PCM(double[][] matrix) throws IllegalArgumentException {
    super(matrix);
  }

  public double count() {
    double[] pos = getMatrix()[0];
    double sum = 0;
    for (int i = 0; i < alphabetSize(); ++i) {
      sum += pos[i];
    }
    return sum;
  }

  public PWM to_pwm(BackgroundModel background, PseudocountCalculator pseudocount) {
    return new ru.autosome.commons.converter.mono.PCM2PWM(background, pseudocount).convert(this);
  }

  public PPM to_ppm(BackgroundModel background) {
    return new ru.autosome.commons.converter.mono.PCM2PPM().convert(this);
  }

}
