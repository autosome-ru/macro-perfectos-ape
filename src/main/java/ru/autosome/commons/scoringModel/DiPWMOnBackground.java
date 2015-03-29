package ru.autosome.commons.scoringModel;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.motifModel.Encodable;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.perfectosape.model.encoded.di.SequenceDiEncoded;
import ru.autosome.perfectosape.model.SequenceWithSNP;
import ru.autosome.perfectosape.model.encoded.di.SequenceWithSNPDiEncoded;

public class DiPWMOnBackground implements ScoringModel<SequenceDiEncoded>, Encodable<SequenceDiEncoded, SequenceWithSNPDiEncoded> {

  private final DiPWM dipwm;
  private final DiBackgroundModel dibackground;
  private final double[][] matrixIUPAC;
  public DiPWMOnBackground(DiPWM dipwm, DiBackgroundModel dibackground) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
    this.matrixIUPAC = calculateMatrixIUPAC();
  }
  public DiPWMOnBackground(DiPWM dipwm) {
    this.dipwm = dipwm;
    this.dibackground = new DiWordwiseBackground();
    this.matrixIUPAC = calculateMatrixIUPAC();
  }

  private double[][] calculateMatrixIUPAC() {
    double[][] result = new double[dipwm.length()][];
    for (int posIndex = 0; posIndex < dipwm.length(); ++posIndex) {
      result[posIndex] = new double[25];
      for (int firstLetterIndex = 0; firstLetterIndex < 4; ++firstLetterIndex) {
        // AA,AC,AG,AT, CA,CC,CG,CT, GA,GC,GG,GT, TA,TC,TG,TT
        for (int secondLetterIndex = 0; secondLetterIndex < 4; ++secondLetterIndex) {
          result[posIndex][5 * firstLetterIndex + secondLetterIndex] =
           dipwm.getMatrix()[posIndex][4 * firstLetterIndex + secondLetterIndex];
        }
        // AN,CN,GN,TN
        result[posIndex][5 * firstLetterIndex + 4] =
         dibackground.average_by_second_letter(dipwm.getMatrix()[posIndex], firstLetterIndex);
      }
      for (int secondLetterIndex = 0; secondLetterIndex < 4; ++secondLetterIndex) {
        // NA,NC,NG,NT
        result[posIndex][20 + secondLetterIndex] =
         dibackground.average_by_first_letter(dipwm.getMatrix()[posIndex], secondLetterIndex);
      }
      // NN
      result[posIndex][24] = dibackground.mean_value(dipwm.getMatrix()[posIndex]);
    }
    return result;
  }

  @Override
  public int length() {
    return dipwm.length(); // It is model length, not matrix length
  }

  public double score(SequenceDiEncoded word) {
    return score(word, Orientation.direct, 0);
  }

  @Override
  public double score(SequenceDiEncoded word, Orientation orientation, int position) {
    byte[] seq;
    int startPos;
    if (orientation == Orientation.direct) {
      seq = word.directSequence;
      startPos = position;
    } else  {
      seq = word.revcompSequence;
      startPos = word.length() - (position + length());
    }

    double sum = 0.0;
    for (int pos_index = 0; pos_index < matrixIUPAC.length; ++pos_index) {
      byte letter = seq[startPos + pos_index];
      sum += matrixIUPAC[pos_index][letter];
    }
    return sum;
  }

  @Override
  public double score_mean() {
    double result = 0.0;
    for (double[] pos : dipwm.getMatrix()) {
      result += dibackground.mean_value(pos);
    }
    return result;
  }

  @Override
  public double score_variance() {
    double variance = 0.0;
    for (double[] pos : dipwm.getMatrix()) {
      variance += dibackground.variance(pos);
    }
    return variance;
  }

  @Override
  public SequenceDiEncoded encodeSequence(Sequence sequence) {
    return sequence.diEncode();
  }
  @Override
  public SequenceWithSNPDiEncoded encodeSequenceWithSNP(SequenceWithSNP sequenceWithSNP) {
    return sequenceWithSNP.diEncode();
  }
}
