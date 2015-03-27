package ru.autosome.commons.converter.generalized;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.types.PositionCountModel;
import ru.autosome.commons.motifModel.types.PositionWeightModel;

// TODO: extract interface for converter
public abstract class PCM2PWM<ModelTypeFrom extends PositionCountModel,
                              ModelTypeTo extends PositionWeightModel,
                              BackgroundType extends GeneralizedBackgroundModel>
                              implements MotifConverter<ModelTypeFrom, ModelTypeTo> {


  public final PseudocountCalculator pseudocountCalculator;
  public final GeneralizedBackgroundModel background;

  protected abstract BackgroundType defaultBackground();
  protected abstract ModelTypeTo createMotif(double[][] matrix);

  public PCM2PWM(BackgroundType background, PseudocountCalculator pseudocountCalculator) {
    this.background = background;
    this.pseudocountCalculator = pseudocountCalculator;
  }

  public PCM2PWM() {
    this.background = defaultBackground();
    this.pseudocountCalculator = PseudocountCalculator.logPseudocount; // to be calculated automatically as logarithm of count
  }

  public Named<ModelTypeTo> convert(Named<ModelTypeFrom> namedModel) {
    return new Named<>(convert(namedModel.getObject()),
                       namedModel.getName());
  }
  public ModelTypeTo convert(ModelTypeFrom pcm) {
    double new_matrix[][] = new double[pcm.getMatrix().length][];
    for (int pos = 0; pos < pcm.getMatrix().length; ++pos) {
      new_matrix[pos] = convert_position(pcm.getMatrix()[pos]);
    }
    return createMotif(new_matrix);
  }

  // columns can have different counts for some PCMs
  private double count(double[] pos) {
    double count = 0.0;
    for(double element: pos) {
      count += element;
    }
    return count;
  }


  private double[] convert_position(double[] pos) {
    double count = count(pos);
    double pseudocount = pseudocountCalculator.calculatePseudocount(count);
    double[] converted_pos = new double[pos.length];

    for (int letter = 0; letter < pos.length; ++letter) {
      double numerator = pos[letter] + background.probability(letter) * pseudocount;
      double denominator = background.probability(letter) * (count + pseudocount);
      converted_pos[letter] = Math.log(numerator / denominator);
    }
    return converted_pos;
  }
}
