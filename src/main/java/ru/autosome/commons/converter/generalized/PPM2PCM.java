package ru.autosome.commons.converter.generalized;

import ru.autosome.commons.motifModel.Named;
import ru.autosome.commons.motifModel.types.PositionCountModel;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public abstract class PPM2PCM<ModelTypeFrom extends PositionFrequencyModel & Named,
                     ModelTypeTo extends PositionCountModel & Named> implements MotifConverter<ModelTypeFrom, ModelTypeTo> {
  public final double count;

  protected abstract ModelTypeTo createMotif(double[][] matrix, String name);

  public PPM2PCM(double count) {
    this.count = count;
  }

  public ru.autosome.commons.model.Named<ModelTypeTo> convert(ru.autosome.commons.model.Named<ModelTypeFrom> namedModel) {
    return new ru.autosome.commons.model.Named<>(convert(namedModel.getObject()),
                                                 namedModel.getName());
  }

  public ModelTypeTo convert(ModelTypeFrom ppm) {
    double new_matrix[][] = new double[ppm.getMatrix().length][];
    for (int pos = 0; pos < ppm.getMatrix().length; ++pos) {
      new_matrix[pos] = convert_position(ppm.getMatrix()[pos]);
    }
    return createMotif(new_matrix, ppm.getName());
  }

  private double[] convert_position(double[] pos) {
    double[] converted_pos = new double[pos.length];

    for (int letter = 0; letter < pos.length; ++letter) {
      converted_pos[letter] = pos[letter] * count;
    }
    return converted_pos;
  }
}
