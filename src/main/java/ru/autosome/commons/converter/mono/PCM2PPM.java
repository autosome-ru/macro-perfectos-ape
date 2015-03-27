package ru.autosome.commons.converter.mono;

import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PPM;


public class PCM2PPM extends ru.autosome.commons.converter.generalized.PCM2PPM<PCM, PPM> {
  public PCM2PPM() {
    super();
  }

  @Override
  protected PPM createMotif(double[][] matrix) {
    return new PPM(matrix);
  }
}
