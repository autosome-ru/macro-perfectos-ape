package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.converters.PCM2PPMConverter;
import ru.autosome.perfectosape.converters.PCM2PWMConverter;
import ru.autosome.perfectosape.importers.PMParser;

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

  public DiPWM to_pwm(DiBackgroundModel background) {
    PCM2PWMConverter<DiPCM,DiPWM> converter = new PCM2PWMConverter<DiPCM, DiPWM>(this, DiPWM.class); // ToDo: !!!!!
    converter.background = background;
    return converter.convert();
  }
  public DiPPM to_ppm(DiBackgroundModel background) {
    return new PCM2PPMConverter<DiPCM, DiPPM>(this, DiPPM.class).convert();
  }

  public static DiPCM fromParser(PMParser parser) {
    if (parser == null)  return null;
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new DiPCM(matrix, name);
  }
}
