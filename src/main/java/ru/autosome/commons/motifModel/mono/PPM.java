package ru.autosome.commons.motifModel.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.converter.PPM2PCMConverter;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public class PPM extends PM implements PositionFrequencyModel {
  public PPM(double[][] matrix, String name) throws IllegalArgumentException {
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

  public PCM to_pcm(double count) {
    PPM2PCMConverter<PPM, PCM> converter = new PPM2PCMConverter<PPM,PCM>(this, count, PCM.class);
    return converter.convert();
  }
  public PWM to_pwm(BackgroundModel background, double count) {
    return to_pcm(count).to_pwm(background);
  }

  public static PPM fromParser(PMParser parser) {
    if (parser == null)  return null;
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new PPM(matrix, name);
  }
}
