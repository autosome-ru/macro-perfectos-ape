package ru.autosome.commons.motifModel.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public class PPM extends PM implements PositionFrequencyModel {
  public PPM(double[][] matrix) throws IllegalArgumentException {
    super(matrix);
    for (double[] pos : matrix) {
      double sum = 0;
      for (int letter = 0; letter < PPM.ALPHABET_SIZE; ++letter) {
        sum += pos[letter];
      }
      if (Math.abs(sum - 1.0) > 0.001) {
        System.err.println("Warning! Sum of each column of a PFM should be 1.0(+-0.001), but was " + sum);
      }
    }
  }

  public PCM to_pcm(double count) {
    return new ru.autosome.commons.converter.mono.PPM2PCM(count).convert(this);
  }

  public PWM to_pwm(BackgroundModel background, double count, PseudocountCalculator pseudocount) {
    PCM pcm = new ru.autosome.commons.converter.mono.PPM2PCM(count).convert(this);
    return new ru.autosome.commons.converter.mono.PCM2PWM(background, pseudocount).convert(pcm);
  }
}
