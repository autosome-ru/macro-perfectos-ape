package ru.autosome.commons.converter.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPWM;

public class PCM2PWM extends ru.autosome.commons.converter.generalized.PCM2PWM<DiPCM, DiPWM, DiBackgroundModel>{

  public PCM2PWM(DiBackgroundModel background, double pseudocount) {
    super(background);
  }

  public PCM2PWM(DiBackgroundModel background) {
    super(background);
  }

  public PCM2PWM(double pseudocount) {
    super(pseudocount);
  }

  public PCM2PWM() {
    super();
  }

  protected DiBackgroundModel defaultBackground() {
    return new DiWordwiseBackground();
  }

  @Override
  protected DiPWM createMotif(double[][] matrix, String name) {
    return new DiPWM(matrix, name);
  }
}
