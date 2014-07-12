package ru.autosome.commons.backgroundModel.di;

import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.support.ArrayExtensions;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;

import java.util.List;

public class DiBackground implements DiBackgroundModel {
  private double[] background;
  private double[][] _conditionalProbabilities;

  // TODO: whether we should check symmetricity of background

  // probabilities are absolute, not conditional. Indices are (4*firstLetter + secondLetter)
  public DiBackground(double[] background) {
    if (background.length != 16) {
      throw new IllegalArgumentException("Background constructor takes an array of 16 frequencies");
    }
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  public DiBackground(List<Double> background) {
    double[] background_array = ArrayExtensions.toPrimitiveArray(background);
    if (background_array.length != 16) {
      throw new IllegalArgumentException("Background constructor takes an array of 16 frequencies");
    }
    if (Math.abs(ArrayExtensions.sum(background_array) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background_array;
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

  public static DiBackgroundModel fromGCContent(double gc_content) {
    return DiBackground.fromMonoBackground( Background.fromGCContent(gc_content) );
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
    if (s.toLowerCase().equals("wordwise")) {
      return new DiWordwiseBackground();
    }

    List<Double> tokens = InputExtensions.listOfDoubleTokens(s);
    if (tokens.size() == 16) {
      return new DiBackground(ArrayExtensions.toPrimitiveArray(tokens));
    } else if (tokens.size() == 4) {
      BackgroundModel monoBackground = new Background(tokens);
      return DiBackground.fromMonoBackground( monoBackground );
    } else if (tokens.size() == 1) {
      return DiBackground.fromGCContent(tokens.get(0));
    } else {
      throw new IllegalArgumentException("Background string `" + s + "` not recognized.\n" +
       "It should be either string `wordwise` or dibackground (16 numbers) or monobackground(4 numbers) or GC-content(1 number).\n"+
       "Numbers should be comma separated, spaces not allowed\n" +
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
}
