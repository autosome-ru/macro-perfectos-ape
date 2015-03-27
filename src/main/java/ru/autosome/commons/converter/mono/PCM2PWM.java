package ru.autosome.commons.converter.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PWM;

public class PCM2PWM extends ru.autosome.commons.converter.generalized.PCM2PWM<PCM, PWM, BackgroundModel>{

  public PCM2PWM(BackgroundModel background, PseudocountCalculator pseudocountCalculator) {
    super(background, pseudocountCalculator);
  }

  public PCM2PWM() {
    super();
  }

  protected BackgroundModel defaultBackground() {
    return new WordwiseBackground();
  }

  @Override
  protected PWM createMotif(double[][] matrix) {
    return new PWM(matrix);
  }
}
