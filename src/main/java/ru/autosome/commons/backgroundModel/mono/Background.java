package ru.autosome.commons.backgroundModel.mono;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.support.ArrayExtensions;

import java.util.List;
import java.util.StringTokenizer;

public class Background implements BackgroundModel {
  private double[] background;

  // TODO: whether we should check symmetricity of background
  public Background(double[] background) {
    if (background.length != 4) {
      throw new IllegalArgumentException("Background constructor takes an array of 4 frequencies");
    }
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  public Background(List<Double> background) {
    double background_array[] = ArrayExtensions.toPrimitiveArray(background);

    if (background_array.length != 4) {
      throw new IllegalArgumentException("Background constructor takes an array of 4 frequencies");
    }
    if (Math.abs(ArrayExtensions.sum(background_array) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background_array;
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
    if (s.toLowerCase().equals("wordwise")) {
      return new WordwiseBackground();
    }

    List<Double> tokens = InputExtensions.listOfDoubleTokens(s);
    if (tokens.size() == 4) {
      return new Background(tokens);
    } else if (tokens.size() == 1) {
      return Background.fromGCContent(tokens.get(0));
    } else {
      throw new IllegalArgumentException("Background string `" + s + "` not recognized.\n" +
       "It should be either string `wordwise` or monobackground(4 numbers) or GC-content(1 number).\n"+
       "Numbers should be comma separated, spaces not allowed.\n" +
       "String you've passed has "+ tokens.size() + " numbers");
    }
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