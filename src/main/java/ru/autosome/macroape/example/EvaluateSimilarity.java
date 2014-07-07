package ru.autosome.macroape.example;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;
import ru.autosome.macroape.calculation.mono.CompareModels;

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

      CompareModels comparator = new CompareModels(firstPWM, secondPWM, background, background, discretization, null, null);
      CompareModelsCountsGiven.SimilarityInfo similarityInfo = comparator.jaccard_by_weak_pvalue(0.0005);
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
      ru.autosome.macroape.calculation.di.CompareModels dicomparator = new ru.autosome.macroape.calculation.di.CompareModels(firstDiPWM , secondDiPWM,
                                                   dibackground, dibackground,
                                                   new FindPvalueAPE<DiPWM, DiBackgroundModel>(firstDiPWM, dibackground, discretization, null),
                                                   new FindPvalueAPE<DiPWM, DiBackgroundModel>(secondDiPWM, dibackground, discretization, null),
                                                   discretization, null);

      CompareModelsCountsGiven.SimilarityInfo diSimilarityInfo = dicomparator.jaccard_by_weak_pvalue(0.0005);
      System.out.println("\n----------\n" + diSimilarityInfo.similarity());
      System.out.println(diSimilarityInfo.alignment);
      System.out.println(diSimilarityInfo.recognizedByBoth);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
