package ru.autosome.commons.converter.di;

import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPPM;

public class PPM2PCM extends ru.autosome.commons.converter.generalized.PPM2PCM<DiPPM, DiPCM> {
  public PPM2PCM(double count) {
    super(count);
  }

  @Override
  protected DiPCM createMotif(double[][] matrix, String name) {
    return new DiPCM(matrix, name);
  }
}