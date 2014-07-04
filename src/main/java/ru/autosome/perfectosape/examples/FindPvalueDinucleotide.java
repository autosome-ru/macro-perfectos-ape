package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.Sequence;
import ru.autosome.perfectosape.backgroundModels.*;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindPvalueDinucleotide {
  private static void print_result(CanFindPvalue.PvalueInfo info, GeneralizedBackgroundModel background, int pwmLength) {
    System.out.println( "threshold: " + info.threshold + "\n" +
                         "pvalue: " + info.pvalue + "\n" +
                         "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  private static void run_mono_and_di(PWM mono_pwm, BackgroundModel mono_background, Discretizer discretizer, Integer max_hash_size, double threshold) {
    DiPWM di_pwm = DiPWM.fromPWM(mono_pwm);
    DiBackgroundModel di_background = DiBackground.fromMonoBackground(mono_background);

    FindPvalueAPE calculator = new FindPvalueAPE<PWM, BackgroundModel>(mono_pwm,
                                                                       mono_background,
                                                                       discretizer, max_hash_size);
    FindPvalueAPE dicalculator = new FindPvalueAPE<DiPWM, DiBackgroundModel>(di_pwm,
                                                                             di_background,
                                                                             discretizer, max_hash_size);

    // Single threshold
    try {
      System.out.println( "================");
      CanFindPvalue.PvalueInfo info = calculator.pvalueByThreshold(threshold);
      print_result(info, mono_background, mono_pwm.length());

      CanFindPvalue.PvalueInfo di_info = dicalculator.pvalueByThreshold(threshold);
      print_result(di_info, di_background, di_pwm.length());
      System.out.println( "================");
    } catch (HashOverflowException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));

    Discretizer discretizer = new Discretizer(10000.0);
    Integer max_hash_size = null;
    double threshold = 7;
//    double[] thresholds = {3,5,7};

    Sequence word = new Sequence("ACAGTGACAA");
    DiPWM dipwm = DiPWM.fromPWM(pwm); // A way to transform mono-nucleotide to dinucleotide matrix

    System.out.println(pwm.score(word));
    System.out.println(dipwm.score(word));

    //DiPWM dipwm_2 = DiPWM.fromParser(PMParser.from_file_or_stdin("test_data/dipwm/AP2A.di"));

    run_mono_and_di(pwm, new WordwiseBackground(), discretizer, max_hash_size, threshold);
    run_mono_and_di(pwm, new Background(new double[] {0.1, 0.4, 0.4, 0.1}), discretizer, max_hash_size, threshold);
    run_mono_and_di(pwm, new Background(new double[] {0.25, 0.25, 0.25, 0.25}), discretizer, max_hash_size, threshold);
  }
}
