package ru.autosome.macroape.model;

public class ScanningSimilarityInfo extends ComparisonSimilarityInfo {
  public final String name;
  public final boolean precise;

  public ScanningSimilarityInfo(String name, PairAligned alignment,
                                double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                                boolean precise) {
    super(alignment, recognizedByBoth, recognizedByFirst, recognizedBySecond);
    this.name = name;
    this.precise = precise;

  }
  public ScanningSimilarityInfo(String name, ComparisonSimilarityInfo similarityInfo, boolean precise) {
    super(similarityInfo.alignment,
          similarityInfo.recognizedByBoth,
          similarityInfo.recognizedByFirst,
          similarityInfo.recognizedBySecond);
    this.name = name;
    this.precise = precise;
  }
}
