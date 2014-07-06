package ru.autosome.commons.motifModel.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.converter.PPM2PCMConverter;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public class DiPPM extends DiPM implements PositionFrequencyModel {
  public DiPPM(double[][] matrix, String name) throws IllegalArgumentException {
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

  public DiPCM to_pcm(double count) {
    PPM2PCMConverter<DiPPM, DiPCM> converter = new PPM2PCMConverter<DiPPM, DiPCM>(this, count, DiPCM.class); // ToDo: !!!!!!!!!!!
    return converter.convert();
  }
  public DiPWM to_pwm(DiBackgroundModel background, double count) {
    return to_pcm(count).to_pwm(background);
  }

  public static DiPPM fromParser(PMParser parser) {
    if (parser == null)  return null;
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new DiPPM(matrix, name);
  }
}
