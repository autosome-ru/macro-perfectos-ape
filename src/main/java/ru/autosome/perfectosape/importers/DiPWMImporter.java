package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class DiPWMImporter extends PWMImporterGeneralized<DiPWM> {
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
        throw new Error("PCM dinucleotide mode not yet implemented");
      case PPM:
        throw new Error("PPM dinucleotide mode not yet implemented");
      case PWM:
        dipwm = new DiPWM(matrix, name);
        break;
      default:
        throw new Error("This code never reached");
    }
    return dipwm;
  }

}
