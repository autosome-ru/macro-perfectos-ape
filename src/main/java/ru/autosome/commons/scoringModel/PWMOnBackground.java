package ru.autosome.commons.scoringModel;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.motifModel.ScoreStatistics;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.perfectosape.model.encoded.mono.SequenceMonoEncoded;

public class PWMOnBackground implements ScoreStatistics, ScoringModel<SequenceMonoEncoded> {
  private final PWM pwm;
  private final BackgroundModel background;
  private final double[][] matrixIUPAC;
  private final int length;

  public PWMOnBackground(PWM pwm, BackgroundModel background) {
    this.pwm = pwm;
    this.background = background;
    this.matrixIUPAC = calculateMatrixIUPAC();
    this.length = pwm.length();
  }

  private double[][] calculateMatrixIUPAC() {
    double[][] result = new double[pwm.length()][];
    for (int posIndex = 0; posIndex < pwm.length(); ++posIndex) {
      result[posIndex] = new double[5];
      System.arraycopy(pwm.getMatrix()[posIndex], 0, result[posIndex], 0, PWM.ALPHABET_SIZE);
      result[posIndex][4] = background.mean_value(pwm.getMatrix()[posIndex]);
    }
    return result;
  }

  @Override
  public int length() {
    return this.length;
  }

  @Override
  public double score(SequenceMonoEncoded word) {
    return score(word, Orientation.direct, 0);
  }

  @Override
  public double score(SequenceMonoEncoded word, Orientation orientation, int position) {
    byte[] seq;
    int startPos;
    if (orientation == Orientation.direct) {
      seq = word.directSequence;
      startPos = position;
    } else {
      seq = word.revcompSequence;
      startPos = seq.length - (position + length());
    }

    double sum = 0.0;
    for (int pos_index = 0; pos_index < length(); ++pos_index) {
      byte letter = seq[startPos + pos_index];
      sum += matrixIUPAC[pos_index][letter];
    }
    return sum;
  }

  @Override
  public double score_mean() {
    double result = 0.0;
    for (double[] pos : pwm.getMatrix()) {
      result += background.mean_value(pos);
    }
    return result;
  }

  @Override
  public double score_variance() {
    double variance = 0.0;
    for (double[] pos : pwm.getMatrix()) {
      variance += background.variance(pos);
    }
    return variance;
  }
}
