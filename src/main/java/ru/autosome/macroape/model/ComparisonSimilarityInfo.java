package ru.autosome.macroape.model;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.motifModel.Alignable;

public class ComparisonSimilarityInfo<ModelType extends Alignable<ModelType>> {
  public final PairAligned<ModelType> alignment;
  public final double recognizedByBoth;
  public final double recognizedByFirst;
  public final double recognizedBySecond;

  public ComparisonSimilarityInfo(PairAligned<ModelType> alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
    this.recognizedByFirst = recognizedByFirst;
    this.recognizedBySecond = recognizedBySecond;
    this.recognizedByBoth = recognizedByBoth;
    this.alignment = alignment;
  }

  public Double realPvalueFirst(GeneralizedBackgroundModel background) {
    double vocabularyVolume = Math.pow(background.volume(), alignment.length());
    return recognizedByFirst / vocabularyVolume;
  }
  public Double realPvalueSecond(GeneralizedBackgroundModel background) {
    double vocabularyVolume = Math.pow(background.volume(), alignment.length());
    return recognizedBySecond / vocabularyVolume;
  }

  public int shift() {
    return alignment.shift();
  }

  public Orientation orientation() {
    return alignment.orientation();
  }

  public int overlap() {
    return alignment.overlapSize();
  }


  public static Double jaccardByCounts(double recognizedByFirst, double recognizedBySecond, double recognizedByBoth) {
    if (recognizedByFirst == 0 || recognizedBySecond == 0) {
      return null;
    }
    double union = recognizedByFirst + recognizedBySecond - recognizedByBoth;
    return recognizedByBoth / union;
  }

  public Double similarity() {
    return jaccardByCounts(recognizedByFirst, recognizedBySecond, recognizedByBoth);
  }

  public Double distance() {
    Double similarity = similarity();
    if (similarity == null) {
      return null;
    } else {
      return 1.0 - similarity;
    }
  }

}
