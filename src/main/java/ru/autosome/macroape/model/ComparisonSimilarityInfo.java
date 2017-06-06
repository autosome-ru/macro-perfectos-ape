package ru.autosome.macroape.model;

import ru.autosome.commons.model.Orientation;

public class ComparisonSimilarityInfo {
  public final PairAligned alignment;
  public final double recognizedByBoth;
  public final double recognizedByFirst;
  public final double recognizedBySecond;

  public ComparisonSimilarityInfo(PairAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
    this.recognizedByFirst = recognizedByFirst;
    this.recognizedBySecond = recognizedBySecond;
    this.recognizedByBoth = recognizedByBoth;
    this.alignment = alignment;
  }

  public Double realPvalueFirst(int backgroundVolume) {
    double vocabularyVolume = Math.pow(backgroundVolume, alignment.length());
    return recognizedByFirst / vocabularyVolume;
  }
  public Double realPvalueSecond(int backgroundVolume) {
    double vocabularyVolume = Math.pow(backgroundVolume, alignment.length());
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

  public int length() {
    return alignment.length();
  }

  public String first_model_alignment() {
    return alignment.first_model_alignment();
  }

  public String second_model_alignment() {
    return alignment.second_model_alignment();
  }

  public double getRecognizedByBoth() {
    return recognizedByBoth;
  }
  public double getRecognizedByFirst() {
    return recognizedByFirst;
  }
  public double getRecognizedBySecond() {
    return recognizedBySecond;
  }
}
