package ru.autosome.commons.motifModel.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public class DiPPM extends DiPM implements PositionFrequencyModel {
  public DiPPM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
    for (double[] pos : matrix) {
      double sum = 0;
      for (int letter = 0; letter < PPM.ALPHABET_SIZE; ++letter) {
        sum += pos[letter];
      }
      if (Math.abs(sum - 1.0) > 0.001) {
        throw new IllegalArgumentException("sum of each column should be 1.0(+-0.001), but was " + sum);
      }
    }
  }

  public DiPCM to_pcm(double count) {
    return new ru.autosome.commons.converter.di.PPM2PCM(count).convert(this);
  }

  public DiPWM to_pwm(DiBackgroundModel background, double count, PseudocountCalculator pseudocount) {
    DiPCM pcm = new ru.autosome.commons.converter.di.PPM2PCM(count).convert(this);
    return new ru.autosome.commons.converter.di.PCM2PWM(background, pseudocount).convert(pcm);
  }
}
