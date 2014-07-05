package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.motifModels.PWM;

public class EvaluateSimilarity {
  public static void main(String[] args){
    PWM firstPWM = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    PWM secondPWM = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/SP1_f1.pwm"));
    try {
      /*ComparePWM.SimilarityInfo similarityInfo =
       new ComparePWM(new CountingPWM(firstPWMCounting.discrete(10.0), new WordwiseBackground(), null),
                     new CountingPWM(secondPWMCounting.discrete(10.0), new WordwiseBackground(), null))
        .jaccard_by_weak_pvalue(0.0005);
      System.out.println("\n----------\n" + similarityInfo.similarity());
      System.out.println(similarityInfo.alignment);
      System.out.println(similarityInfo.recognizedByBoth);
      //System.out.println("\n----------\n" + new PairAligned(firstPWMCounting, secondPWMCounting, similarityInfo.alignment).firstModelAligned);
      //System.out.println("\n----------\n" + new PairAligned(firstPWMCounting, secondPWMCounting, similarityInfo.alignment).secondModelAligned);
        */
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
