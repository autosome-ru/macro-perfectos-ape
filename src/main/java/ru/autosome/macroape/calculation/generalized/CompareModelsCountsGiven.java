package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.macroape.model.PairAligned;

import java.util.ArrayList;
import java.util.List;

abstract public class CompareModelsCountsGiven <ModelType extends Alignable<ModelType> & Discretable<ModelType>,
                                                BackgroundType extends GeneralizedBackgroundModel> {

  public final ModelType firstPWM; // here we store discreted PWMs
  public final ModelType secondPWM;
  public final BackgroundType firstBackground;
  public final BackgroundType secondBackground;
  public final Double discretization; // PWMs are already stored discreted, disretization needed in order to upscale thresholds
  public final Integer maxPairHashSize;

  public CompareModelsCountsGiven(ModelType firstPWM, ModelType secondPWM,
                               BackgroundType firstBackground,
                               BackgroundType secondBackground,
                               Double discretization, Integer maxPairHashSize) {
    this.firstPWM = firstPWM.discrete(discretization);
    this.secondPWM = secondPWM.discrete(discretization);
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.discretization = discretization;
    this.maxPairHashSize = maxPairHashSize;
  }


  public double upscaleThreshold(double threshold) {
    if (discretization == null) {
      return threshold;
    } else {
      return threshold * discretization;
    }
  }

  private List<Position> relative_alignments() {
    List<Position> result = new ArrayList<Position>();
    for(int shift = -secondPWM.length(); shift <= firstPWM.length(); ++shift) {
      result.add(new Position(shift, true));
      result.add(new Position(shift, false));
    }
    return result;
  }

  protected double firstCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(firstBackground.volume(), alignment.length() - firstPWM.length());
  }
  protected double secondCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(secondBackground.volume(), alignment.length() - secondPWM.length());
  }

  public SimilarityInfo<ModelType> jaccard(double thresholdFirst, double thresholdSecond,
                                double firstCount, double secondCount) throws HashOverflowException {
    double bestSimilarity = -1;
    SimilarityInfo<ModelType> bestSimilarityInfo = null;
    for (Position position: relative_alignments()) {
      SimilarityInfo<ModelType> similarityInfo;
      similarityInfo = jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position);
      double similarity = similarityInfo.similarity();
      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestSimilarityInfo = similarityInfo;
      }
    }
    return bestSimilarityInfo;
  }

  public SimilarityInfo<ModelType> jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                          double firstCount, double secondCount,
                                          Position position) throws HashOverflowException {
    PairAligned<ModelType> alignment = new PairAligned<ModelType>(firstPWM, secondPWM, position);
    double intersection = calculator(alignment).count_in_intersection(upscaleThreshold(thresholdFirst),
                                                                      upscaleThreshold(thresholdSecond));

    double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
    double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

    return new SimilarityInfo<ModelType>(alignment, intersection, firstCountRenormed, secondCountRenormed);
  }

  protected abstract ru.autosome.macroape.calculation
                      .generalized.AlignedModelIntersection
                      <ModelType, BackgroundType> calculator(PairAligned<ModelType> alignment);

  public static class SimilarityInfo<ModelType extends Alignable<ModelType>> extends ResultInfo {
    public final PairAligned<ModelType> alignment;
    public final double recognizedByBoth;
    public final double recognizedByFirst;
    public final double recognizedBySecond;

    public SimilarityInfo(PairAligned<ModelType> alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
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

    public String orientation() {
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
}