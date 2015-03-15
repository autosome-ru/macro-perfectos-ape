package ru.autosome.commons.scoringModel;

import gnu.trove.impl.unmodifiable.TUnmodifiableCharIntMap;
import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.perfectosape.model.Sequence;

public class PWMOnBackground implements ScoringModel {
  protected static final TCharIntMap indexByLetter =
   new TUnmodifiableCharIntMap( new TCharIntHashMap(new char[]{'A','C','G','T'},
                                                    new int[] {0, 1, 2, 3}) );

  private final PWM pwm;
  private final BackgroundModel background;
  public PWMOnBackground(PWM pwm, BackgroundModel background) {
    this.pwm = pwm;
    this.background = background;
  }

  public PWMOnBackground(PWM pwm) {
    this.pwm = pwm;
    this.background = new WordwiseBackground();
  }

  @Override
  public int length() {
    return pwm.length();
  }

  @Override
  public double score(Sequence word) {
    return score(word.sequence);
  }


  private double score(String word) throws IllegalArgumentException {
    word = word.toUpperCase();
    if (word.length() != length()) {
      throw new IllegalArgumentException("word '" + word + "' in PWM#score(word) should have the same length(" + word.length() + ") as matrix has (" + length() + ")");
    }
    double sum = 0.0;
    for (int pos_index = 0; pos_index < length(); ++pos_index) {
      char letter = word.charAt(pos_index);
      if (indexByLetter.containsKey(letter)) {
        int letter_index = indexByLetter.get(letter);
        sum += pwm.getMatrix()[pos_index][letter_index];
      } else if (letter == 'N') {
        sum += background.mean_value(pwm.getMatrix()[pos_index]);
      } else {
        throw new IllegalArgumentException("word in PWM#score(" + word + ") should have only ACGT or N letters, but have '" + letter + "' letter at position " + (pos_index + 1));
      }
    }
    return sum;
  }
}
