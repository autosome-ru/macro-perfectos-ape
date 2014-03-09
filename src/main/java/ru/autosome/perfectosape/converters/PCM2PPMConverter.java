package ru.autosome.perfectosape.converters;

import ru.autosome.perfectosape.motifModels.*;

public class PCM2PPMConverter <ModelTypeFrom extends PositionCountModel & Named,
                               ModelTypeTo extends PositionFrequencyModel & Named> {
  private final ModelTypeFrom pcm;
  private final Class<ModelTypeTo> toClass;

  public PCM2PPMConverter(ModelTypeFrom pcm, Class<ModelTypeTo> toClass) {
    this.pcm = pcm;
    this.toClass = toClass;
  }

  // columns can have different counts for some PCMs
  double count(double[] pos) {
    double count = 0.0;
    for(double element: pos) {
      count += element;
    }
    return count;
  }

  double[] convert_position(double[] pos) {
    double count = count(pos);

    double[] converted_pos = new double[pcm.alphabetSize()];
    for (int letter = 0; letter < pcm.alphabetSize(); ++letter) {
      converted_pos[letter] = pos[letter] / count;
    }
    return converted_pos;
  }


  public ModelTypeTo convert() {
    double new_matrix[][] = new double[pcm.getMatrix().length][];
    for (int pos = 0; pos < pcm.getMatrix().length; ++pos) {
      new_matrix[pos] = convert_position(pcm.getMatrix()[pos]);
    }
    try{
      return toClass.getConstructor(double[][].class, String.class).newInstance(new_matrix, pcm.getName());
    } catch (Exception exception) {
      throw new Error("Should not be here", exception);
    }
  }
}
