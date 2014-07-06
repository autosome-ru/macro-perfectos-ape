package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.backgroundModels.*;
import ru.autosome.perfectosape.calculations.CompareDiPWM;
import ru.autosome.perfectosape.calculations.CompareModels;
import ru.autosome.perfectosape.calculations.ComparePWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

public class EvaluateSimilarity {
  public static void main(String[] args){
    PWM firstPWM = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    PWM secondPWM = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/SP1_f1.pwm"));
    try {
//      CountingPWM firstPWMCounting = new CountingPWM(firstPWM.discrete(10.0), new WordwiseBackground(), null);
//      CountingPWM secondPWMCounting = new CountingPWM(secondPWM.discrete(10.0), new WordwiseBackground(), null);

      Double discretization = 100.0;
      BackgroundModel background = new WordwiseBackground();
//      BackgroundModel background = new Background(new double[]{0.25,0.25,0.25,0.25});

      ComparePWM comparator = new ComparePWM( firstPWM, secondPWM,
                                              background, background,
                                              new FindPvalueAPE<PWM, BackgroundModel>(firstPWM, background, discretization, null),
                                              new FindPvalueAPE<PWM, BackgroundModel>(secondPWM, background, discretization, null),
                                             discretization, null);
      CompareModels.SimilarityInfo similarityInfo = comparator.jaccard_by_weak_pvalue(0.0005);
      System.out.println("\n----------\n" + similarityInfo.similarity());
      System.out.println(similarityInfo.alignment);
      System.out.println(similarityInfo.recognizedByBoth);
      //System.out.println("\n----------\n" + new PairAligned(firstPWMCounting, secondPWMCounting, similarityInfo.alignment).firstModelAligned);
      //System.out.println("\n----------\n" + new PairAligned(firstPWMCounting, secondPWMCounting, similarityInfo.alignment).secondModelAligned);

      DiPWM firstDiPWM = DiPWM.fromPWM(firstPWM);
      DiPWM secondDiPWM = DiPWM.fromPWM(secondPWM);
      DiBackgroundModel dibackground = new DiWordwiseBackground();
//      DiBackgroundModel dibackground = new DiBackground(new double[]{ 0.0625,0.0625,0.0625,0.0625,
//                                                                      0.0625,0.0625,0.0625,0.0625,
//                                                                      0.0625,0.0625,0.0625,0.0625,
//                                                                      0.0625,0.0625,0.0625,0.0625});
      CompareDiPWM dicomparator = new CompareDiPWM(firstDiPWM , secondDiPWM,
                                                   dibackground, dibackground,
                                                   new FindPvalueAPE<DiPWM, DiBackgroundModel>(firstDiPWM, dibackground, discretization, null),
                                                   new FindPvalueAPE<DiPWM, DiBackgroundModel>(secondDiPWM, dibackground, discretization, null),
                                                   discretization, null);

      CompareModels.SimilarityInfo diSimilarityInfo = dicomparator.jaccard_by_weak_pvalue(0.0005);
      System.out.println("\n----------\n" + diSimilarityInfo.similarity());
      System.out.println(diSimilarityInfo.alignment);
      System.out.println(diSimilarityInfo.recognizedByBoth);

    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
