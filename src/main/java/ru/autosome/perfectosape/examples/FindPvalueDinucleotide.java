package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.Sequence;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindPvalueDinucleotide {
  static void print_result(CanFindPvalue.PvalueInfo info, DiBackgroundModel background, int pwmLength) {
    System.out.println( "threshold: " + info.threshold + "\n" +
                         "pvalue: " + info.pvalue + "\n" +
                         "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    DiBackgroundModel background = new DiWordwiseBackground();
    double discretization = 10000;
    Integer max_hash_size = null;
    double threshold = 3;
    double[] thresholds = {3,5,7};

    Sequence word = new Sequence("ACAGTGACAA");
    DiPWM dipwm_1 = DiPWM.fromPWM(pwm); // A way to transform mono-nucleotide to dinucleotide matrix

    System.out.println(pwm.score(word));
    System.out.println(dipwm_1.score(word));


    DiPWM dipwm_2 = DiPWM.fromParser(PMParser.from_file_or_stdin("test_data/dipwm/AP2A.di"));

    FindPvalueAPE calculator = new FindPvalueAPE<DiPWM, DiBackgroundModel>(dipwm_2, background, discretization, max_hash_size);

    // Single threshold
    CanFindPvalue.PvalueInfo info = null;
    try {
      info = calculator.pvalueByThreshold(threshold);
    } catch (HashOverflowException e) {
      e.printStackTrace();
    }
    print_result(info, background, pwm.length());
  }
}
