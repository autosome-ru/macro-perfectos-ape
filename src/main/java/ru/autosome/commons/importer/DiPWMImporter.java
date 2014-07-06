package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPPM;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.types.DataModel;

public class DiPWMImporter extends MotifImporter<DiPWM> {
  final DiBackgroundModel dibackground;
  final DataModel dataModel;
  final Double effectiveCount;

  public DiPWMImporter(DiBackgroundModel dibackground, DataModel dataModel, Double effectiveCount) {
    this.dibackground = dibackground;
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
  }

  // constructs DiPWM from any source: pwm/pcm/ppm matrix
  @Override
  public DiPWM transformToPWM(double matrix[][], String name) {
    DiPWM dipwm;
    switch (dataModel) {
      case PCM:
        dipwm = new DiPCM(matrix, name).to_pwm(dibackground);
        break;
      case PPM:
        dipwm = new DiPPM(matrix, name).to_pwm(dibackground, effectiveCount);
        break;
      case PWM:
        dipwm = new DiPWM(matrix, name);
        break;
      default:
        throw new Error("This code never reached");
    }
    return dipwm;
  }

}
