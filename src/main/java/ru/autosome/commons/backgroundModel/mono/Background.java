package ru.autosome.commons.backgroundModel.mono;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.support.ArrayExtensions;

import java.util.StringTokenizer;

public class Background implements BackgroundModel {
  private double[] background;

  // TODO: whether we should check symmetricity of background
  public Background(double[] background) {
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  public static BackgroundModel fromArray(double[] background) {
    if (background.length != ALPHABET_SIZE) {
      throw new IllegalArgumentException("Background constructor accepts double array of length " + ALPHABET_SIZE);
    }
    boolean wordwise = true;
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      if (Math.abs(background[i] - 1) > 0.0001) {
        wordwise = false;
      }
    }
    if (wordwise) {
      return new WordwiseBackground();
    } else {
      return new Background(background);
    }
  }

  // Approximation of mononucleotide background
  public static BackgroundModel fromDiBackground(DiBackgroundModel backgroundModel) {
    if (backgroundModel.is_wordwise()) {
      return new WordwiseBackground();
    } else {
      double[] background = new double[4];
      for (int letter = 0; letter < 4; ++letter) {
        double sum = 0;
        // probability for A is an average of AA+AC+AT+AG and AA+CA+GA+TA
        for (int anotherLetter = 0; anotherLetter < 4; ++anotherLetter) {
          sum += backgroundModel.probability(4 * letter + anotherLetter) +
                 backgroundModel.probability(4 * anotherLetter + letter);
        }
        background[letter] = sum / 2;
      }
      return new Background(background);
    }
  }


  @Override
  public double probability(int index) {
    return background[index];
  }

  @Override
  public double count(int index) {
    return background[index];
  }

  @Override
  public double volume() {
    return 1;
  }

  public static BackgroundModel fromString(String s) {
    double[] background = new double[4];
    StringTokenizer parser = new StringTokenizer(s);
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      background[i] = Double.valueOf(parser.nextToken(","));
    }
    return Background.fromArray(background);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      if (i != 0) {
        builder.append(',');
      }
      builder.append(background[i]);
    }
    return builder.toString();
  }

  @Override
  public boolean is_wordwise() {
    return false;
  }

  @Override
  public double mean_value(double[] values) {
    double result = 0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      result += values[letter] * probability(letter);
    }
    return result;
  }

  @Override
  public double mean_square_value(double[] values) {
    double mean_square = 0.0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      mean_square += values[letter] * values[letter] * probability(letter);
    }
    return mean_square;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof BackgroundModel) {
      boolean result = true;
      for (int i = 0; i < ALPHABET_SIZE; ++i) {
        result = result && count(i) == ((BackgroundModel)other).count(i);
      }
      return result;
    } else {
      return false;
    }
  }

  // GC-content should be 0 to 1
  public static BackgroundModel fromGCContent(double gcContent) {
    double p_at = (1 - gcContent) / 2;
    double p_cg = gcContent / 2;
    return new Background(new double[]{p_at, p_cg, p_cg, p_at});
  }
}