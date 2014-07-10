package ru.autosome.commons.converter.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PWM;

public class PCM2PWM extends ru.autosome.commons.converter.generalized.PCM2PWM<PCM, PWM, BackgroundModel>{

  public PCM2PWM(BackgroundModel background, double pseudocount) {
    super(background);
  }

  public PCM2PWM(BackgroundModel background) {
    super(background);
  }

  public PCM2PWM(double pseudocount) {
    super(pseudocount);
  }

  public PCM2PWM() {
    super();
  }

  protected BackgroundModel defaultBackground() {
    return new WordwiseBackground();
  }

  @Override
  protected PWM createMotif(double[][] matrix, String name) {
    return new PWM(matrix, name);
  }
}
