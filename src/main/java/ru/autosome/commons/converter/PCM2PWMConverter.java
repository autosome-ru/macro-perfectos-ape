package ru.autosome.commons.converter;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.types.PositionCountModel;
import ru.autosome.commons.motifModel.types.PositionWeightModel;

// TODO: extract interface for converter
public class PCM2PWMConverter <ModelTypeFrom extends PositionCountModel & Named & BackgroundCompatible,
                               ModelTypeTo extends PositionWeightModel & Named>{
  public GeneralizedBackgroundModel background;
  private final Double const_pseudocount;
  private final ModelTypeFrom pcm;
  private final Class<ModelTypeTo> toClass;

  public PCM2PWMConverter(ModelTypeFrom pcm, Class<ModelTypeTo> toClass) {
    this.pcm = pcm;
    this.background = pcm.compatibleBackground().wordwiseModel();
    this.const_pseudocount = null; // to be calculated automatically as logarithm of count
    this.toClass = toClass;
  }
  public PCM2PWMConverter(ModelTypeFrom pcm, double pseudocount, Class<ModelTypeTo> toClass) {
    this.pcm = pcm;
    this.background = pcm.compatibleBackground().wordwiseModel();
    this.const_pseudocount = pseudocount;
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

  double pseudocount(double count) {
    return (const_pseudocount != null) ? const_pseudocount : Math.log(count);
  }

  double[] convert_position(double[] pos) {
    double count = count(pos);
    double pseudocount = pseudocount(count);

    double[] converted_pos = new double[pcm.alphabetSize()];

    for (int letter = 0; letter < pcm.alphabetSize(); ++letter) {
      double numerator = pos[letter] + background.probability(letter) * pseudocount;
      double denominator = background.probability(letter) * (count + pseudocount);
      converted_pos[letter] = Math.log(numerator / denominator);
    }
    return converted_pos;
  }

  public ModelTypeTo convert() {
    double new_matrix[][] = new double[pcm.getMatrix().length][];
    for (int pos = 0; pos < pcm.getMatrix().length; ++pos) {
      new_matrix[pos] = convert_position(pcm.getMatrix()[pos]);
    }
    try {
      return toClass.getConstructor(double[][].class, String.class).newInstance(new_matrix, pcm.getName());
    } catch (Exception exception) {
      throw new Error("Shouldn't be here!", exception);
    }
  }
}
