package ru.autosome.commons.scoringModel;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.perfectosape.model.Sequence;

import java.util.HashMap;

public class DiPWMOnBackground implements ScoringModel {

  static final HashMap<String, Integer> indexByLetter;
  static {
    indexByLetter = new HashMap<String, Integer>();
    indexByLetter.put("AA", 0);
    indexByLetter.put("AC", 1);
    indexByLetter.put("AG", 2);
    indexByLetter.put("AT", 3);

    indexByLetter.put("CA", 4);
    indexByLetter.put("CC", 5);
    indexByLetter.put("CG", 6);
    indexByLetter.put("CT", 7);

    indexByLetter.put("GA", 8);
    indexByLetter.put("GC", 9);
    indexByLetter.put("GG", 10);
    indexByLetter.put("GT", 11);

    indexByLetter.put("TA", 12);
    indexByLetter.put("TC", 13);
    indexByLetter.put("TG", 14);
    indexByLetter.put("TT", 15);
  }

  private final DiPWM dipwm;
  private final DiBackgroundModel dibackground;
  public DiPWMOnBackground(DiPWM dipwm, DiBackgroundModel dibackground) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
  }
  public DiPWMOnBackground(DiPWM dipwm) {
    this.dipwm = dipwm;
    this.dibackground = new DiWordwiseBackground();
  }

  @Override
  public int length() {
    return dipwm.length();
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
    for (int pos_index = 0; pos_index < dipwm.getMatrix().length; ++pos_index) {
      String dinucleotide = word.substring(pos_index, pos_index + 2);
      Integer superletter_index = indexByLetter.get(dinucleotide);
      if (superletter_index != null) {
        sum += dipwm.getMatrix()[pos_index][superletter_index];
      } /*else if (letter == 'N') {    //  alphabet should include letters such AN, CN, GN, TN, NA, NC, NG, NT, NN
        sum += dibackground.mean_value(dipwm.getMatrix()[pos_index]);
      } */ else {
        throw new IllegalArgumentException("word in PWM#score(" + word + ") should have only {ACGT}^2 dinucleotides , but has '" + dinucleotide + "' dinucleotide at position " + (pos_index + 1));
      }
    }
    return sum;
  }

}
