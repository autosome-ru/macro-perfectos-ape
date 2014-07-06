package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.DataModel;

public class PWMImporter extends MotifImporter<PWM> {
  final BackgroundModel background;
  final DataModel dataModel;
  final Double effectiveCount;

  public PWMImporter(BackgroundModel background, DataModel dataModel, Double effectiveCount) {
    this.background = background;
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
  }
  
  // constructs PWM from any source: pwm/pcm/ppm matrix
  @Override
  public PWM transformToPWM(double matrix[][], String name) {
    PWM pwm;
    switch (dataModel) {
      case PCM:
        pwm = new PCM(matrix, name).to_pwm(background);
        break;
      case PPM:
        pwm = new PPM(matrix, name).to_pwm(background, effectiveCount);
        break;
      case PWM:
        pwm = new PWM(matrix, name);
        break;
      default:
        throw new Error("This code never reached");
    }
    return pwm;
  }
}
