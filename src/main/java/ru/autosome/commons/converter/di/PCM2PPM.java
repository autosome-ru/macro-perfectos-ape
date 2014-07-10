package ru.autosome.commons.converter.di;

import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPPM;

public class PCM2PPM extends ru.autosome.commons.converter.generalized.PCM2PPM<DiPCM, DiPPM> {
  public PCM2PPM() {
    super();
  }

  @Override
  protected DiPPM createMotif(double[][] matrix, String name) {
    return new DiPPM(matrix, name);
  }
}