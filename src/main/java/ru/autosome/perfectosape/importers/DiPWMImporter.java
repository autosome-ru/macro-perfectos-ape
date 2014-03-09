package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.motifModels.*;

public class DiPWMImporter extends MotifImporter<DiPWM> {
  DiBackgroundModel dibackground;
  DataModel dataModel;
  Double effectiveCount;

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
