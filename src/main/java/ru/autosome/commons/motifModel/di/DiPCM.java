package ru.autosome.commons.motifModel.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.types.PositionCountModel;

public class DiPCM extends DiPM implements PositionCountModel {
  public DiPCM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
  }

  public double count() {
    double[] pos = getMatrix()[0];
    double sum = 0;
    for (int i = 0; i < alphabetSize(); ++i) {
      sum += pos[i];
    }
    return sum;
  }

  public DiPWM to_pwm(DiBackgroundModel background, PseudocountCalculator pseudocount) {
    return new ru.autosome.commons.converter.di.PCM2PWM(background, pseudocount).convert(this);
  }

  public DiPPM to_ppm() {
    return new ru.autosome.commons.converter.di.PCM2PPM().convert(this);
  }
}
