package ru.autosome.perfectosape.converters;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.motifModels.PCM;
import ru.autosome.perfectosape.motifModels.PWM;

// TODO: extract interface for converters
public class PCM2PWMConverter {
  public BackgroundModel background;
  private final Double const_pseudocount;
  private final PCM pcm;

  public PCM2PWMConverter(PCM pcm) {
    this.pcm = pcm;
    this.background = new WordwiseBackground();
    this.const_pseudocount = null; // to be calculated automatically as logarithm of count
  }
  public PCM2PWMConverter(PCM pcm, double pseudocount) {
    this.pcm = pcm;
    this.background = new WordwiseBackground();
    this.const_pseudocount = pseudocount;
  }

  public PWM convert() {
    double new_matrix[][] = new double[pcm.matrix.length][];
    for (int pos = 0; pos < pcm.matrix.length; ++pos) {
      new_matrix[pos] = new double[pcm.ALPHABET_SIZE];

      // columns can have different counts for some PCMs
      double count = 0.0;
      for(double element: pcm.matrix[pos]) {
        count += element;
      }
      double pseudocount = (const_pseudocount != null) ? const_pseudocount : Math.log(count);

      for (int letter = 0; letter < pcm.ALPHABET_SIZE; ++letter) {
        double numerator = pcm.matrix[pos][letter] + background.probability(letter) * pseudocount;
        double denominator = background.probability(letter) * (count + pseudocount);
        new_matrix[pos][letter] = Math.log(numerator / denominator);
      }
    }
    return new PWM(new_matrix, pcm.name);
  }
}
