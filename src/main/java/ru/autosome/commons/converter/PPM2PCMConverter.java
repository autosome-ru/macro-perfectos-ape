package ru.autosome.commons.converter;

import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.types.PositionCountModel;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public class PPM2PCMConverter <ModelTypeFrom extends PositionFrequencyModel & Named,
                               ModelTypeTo extends PositionCountModel & Named> {
  private final ModelTypeFrom ppm;
  private final double count;
  private final Class<ModelTypeTo> toClass;

  public PPM2PCMConverter(ModelTypeFrom ppm, double count, Class<ModelTypeTo> toClass) {
    this.ppm = ppm;
    this.count = count;
    this.toClass = toClass;
  }

  double[] convert_position(double[] pos) {
    double[] converted_pos = new double[ppm.alphabetSize()];

    for (int letter = 0; letter < ppm.alphabetSize(); ++letter) {
      converted_pos[letter] = pos[letter] * count;
    }
    return converted_pos;
  }

  public ModelTypeTo convert() {
    double new_matrix[][] = new double[ppm.getMatrix().length][];
    for (int pos = 0; pos < ppm.getMatrix().length; ++pos) {
      new_matrix[pos] = convert_position(ppm.getMatrix()[pos]);
    }
    try {
      return toClass.getConstructor(double[][].class).newInstance(new_matrix, ppm.getName());
    } catch (Exception exception) {
     throw new Error("Shouldn't be here!", exception);
    }
  }
}
