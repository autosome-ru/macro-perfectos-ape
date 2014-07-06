package ru.autosome.commons.motifModel.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.converter.PCM2PPMConverter;
import ru.autosome.commons.converter.PCM2PWMConverter;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.types.PositionCountModel;

public class PCM extends PM implements PositionCountModel {
  public PCM(double[][] matrix, String name) throws IllegalArgumentException {
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

  public PWM to_pwm(BackgroundModel background) {
    PCM2PWMConverter<PCM, PWM> converter = new PCM2PWMConverter<PCM, PWM>(this, PWM.class);
    converter.background = background;
    return converter.convert();
  }
  public PPM to_ppm(BackgroundModel background) {
    return new PCM2PPMConverter<PCM, PPM>(this, PPM.class).convert();
  }

  public static PCM fromParser(PMParser parser) {
    if (parser == null)  return null;
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new PCM(matrix, name);
  }
}
