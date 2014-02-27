package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.converters.PPM2PCMConverter;

public class PPM extends PM {
  public PPM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
    for (int pos = 0; pos < matrix.length; ++pos) {
      double sum = 0;
      for (int letter = 0; letter < PPM.ALPHABET_SIZE; ++letter) {
        sum += matrix[pos][letter];
      }
      if (Math.abs(sum - 1.0) > 0.001) {
        throw new IllegalArgumentException("sum of each column should be 1.0(+-0.001), but was " + sum);
      }
    }
  }

  public PCM to_pcm(double count) {
    PPM2PCMConverter converter = new PPM2PCMConverter(this, count);
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