package ru.autosome.commons.converter.generalized;

import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.types.PositionCountModel;
import ru.autosome.commons.motifModel.types.PositionFrequencyModel;

public abstract class PPM2PCM<ModelTypeFrom extends PositionFrequencyModel,
                              ModelTypeTo extends PositionCountModel
                              > implements MotifConverter<ModelTypeFrom, ModelTypeTo> {
  public final double count;

  protected abstract ModelTypeTo createMotif(double[][] matrix);

  public PPM2PCM(double count) {
    this.count = count;
  }

  public Named<ModelTypeTo> convert(Named<ModelTypeFrom> namedModel) {
    return new Named<>(convert(namedModel.getObject()),
                          namedModel.getName());
  }

  public ModelTypeTo convert(ModelTypeFrom ppm) {
    double new_matrix[][] = new double[ppm.getMatrix().length][];
    for (int pos = 0; pos < ppm.getMatrix().length; ++pos) {
      new_matrix[pos] = convert_position(ppm.getMatrix()[pos]);
    }
    return createMotif(new_matrix);
  }

  private double[] convert_position(double[] pos) {
    double[] converted_pos = new double[pos.length];

    for (int letter = 0; letter < pos.length; ++letter) {
      converted_pos[letter] = pos[letter] * count;
    }
    return converted_pos;
  }
}
