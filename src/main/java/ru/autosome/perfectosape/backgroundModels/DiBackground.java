package ru.autosome.perfectosape.backgroundModels;

import ru.autosome.perfectosape.ArrayExtensions;

import java.util.StringTokenizer;

public class DiBackground implements DiBackgroundModel {
  private double[] background;
  private double[][] _conditionalProbabilities;

  // TODO: whether we should check symmetricity of background

  // probabilities are absolute, not conditional. Indices are (4*firstLetter + secondLetter)
  public DiBackground(double[] background) {
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  public static DiBackgroundModel fromArray(double[] background) {
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
      return new DiWordwiseBackground();
    } else {
      return new DiBackground(background);
    }
  }

  // letter should be independent from previous one (for score distribution recalculation it's ok)
  // However it can be dependent from the next one =\
  public static DiBackgroundModel fromMonoBackground(BackgroundModel backgroundModel) {
    if (backgroundModel.is_wordwise()) {
      return new DiWordwiseBackground();
    } else {
      double[] background = new double[16];
      for (int letter = 0; letter < 4; ++letter) {
        for (int previousLetter = 0; previousLetter < 4; ++previousLetter) {
          background[4 * previousLetter + letter] = backgroundModel.probability(letter) / 4;
        }
      }
      return new DiBackground(background);
    }
  }

  @Override
  public double probability(int index) {
    return background[index];
  }

  @Override
  public double conditionalCount(int previousLetter, int letter) {
    return conditionalProbabilities()[previousLetter][letter];
  }

  @Override
  public double countAnyFirstLetter(int secondLetter) {
    double probabilityAnyLetter = 0;
    for (int firstLetter = 0; firstLetter < 4; ++firstLetter) {
      probabilityAnyLetter += probability(4 * firstLetter + secondLetter);
    }
    return probabilityAnyLetter;
  }

  @Override
  public double countAnySecondLetter(int firstLetter) {
    double probabilityAnyLetter = 0;
    for (int secondLetter = 0; secondLetter < 4; ++secondLetter) {
      probabilityAnyLetter += probability(4 * firstLetter + secondLetter);
    }
    return probabilityAnyLetter;
  }

  private double[][] conditionalProbabilities() {
    if (_conditionalProbabilities == null) {
      _conditionalProbabilities = new double[4][4];
      for (int letterInCondition = 0; letterInCondition < 4; ++letterInCondition) {
        for (int letter = 0; letter < 4; ++letter) {
          _conditionalProbabilities[letterInCondition][letter] = probability(4 * letterInCondition + letter) /
                                                                  countAnySecondLetter(letterInCondition);
        }
      }
    }
    return _conditionalProbabilities;
  }

  @Override
  public double volume() {
    return 1;
  }

  public static DiBackgroundModel fromString(String s) {
    double[] background = new double[ALPHABET_SIZE];
    StringTokenizer parser = new StringTokenizer(s);
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      background[i] = Double.valueOf(parser.nextToken(","));
    }
    return DiBackground.fromArray(background);
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
}
