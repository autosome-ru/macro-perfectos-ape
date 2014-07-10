package ru.autosome.commons.converter.mono;

import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PPM;

public class PPM2PCM extends ru.autosome.commons.converter.generalized.PPM2PCM<PPM, PCM> {
  public PPM2PCM(double count) {
    super(count);
  }

  @Override
  protected PCM createMotif(double[][] matrix, String name) {
    return new PCM(matrix, name);
  }
}
