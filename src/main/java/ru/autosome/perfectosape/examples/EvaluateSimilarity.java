package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.ComparePWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.motifModels.PWM;

public class EvaluateSimilarity {
  public static void main(String[] args){
    PWM firstPWM = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    PWM secondPWM = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/SP1_f1.pwm"));
    Discretizer discretizer = new Discretizer(null);
    try {
      CountingPWM firstPWMCounting = new CountingPWM(firstPWM.discrete(discretizer), new WordwiseBackground(), null);
      CountingPWM secondPWMCounting = new CountingPWM(secondPWM.discrete(discretizer), new WordwiseBackground(), null);
      ComparePWM calculation = new ComparePWM(firstPWMCounting, secondPWMCounting,
                                              new FindPvalueAPE< PWM, BackgroundModel>(firstPWM, new WordwiseBackground(), new Discretizer(10.0), null),
                                              new FindPvalueAPE< PWM, BackgroundModel>(secondPWM, new WordwiseBackground(), new Discretizer(10.0), null),
                                              new Discretizer(10.0), null);
      ComparePWM.SimilarityInfo similarityInfo = calculation.jaccard(3, 3);
      System.out.println("\n----------\n" + similarityInfo.similarity());
      System.out.println(similarityInfo.alignment);
      System.out.println(similarityInfo.recognizedByBoth);
      //System.out.println("\n----------\n" + new MotifsAligned(firstPWMCounting, secondPWMCounting, similarityInfo.alignment).firstMotifAligned);
      //System.out.println("\n----------\n" + new MotifsAligned(firstPWMCounting, secondPWMCounting, similarityInfo.alignment).secondMotifAligned);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
