package ru.autosome.ape.example;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.commons.backgroundModel.*;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PWM;

public class FindPvalueDinucleotide {
  static void print_result(CanFindPvalue.PvalueInfo info, GeneralizedBackgroundModel background, int pwmLength) {
    System.out.println( "threshold: " + info.threshold + "\n" +
                         "pvalue: " + info.pvalue + "\n" +
                         "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  static void run_mono_and_di(PWM mono_pwm, BackgroundModel mono_background, Discretizer discretizer, Integer max_hash_size, double threshold) {
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
    PWM pwm = new PWMImporter().loadMotif("test_data/pwm/KLF4_f2.pwm");

    Discretizer discretizer = new Discretizer(10000.0);
    Integer max_hash_size = null;
    double threshold = 7;
//    double[] thresholds = {3,5,7};

    Sequence word = new Sequence("ACAGTGACAA");
    DiPWM dipwm = DiPWM.fromPWM(pwm); // A way to transform mono-nucleotide to di matrix

    System.out.println(pwm.score(word));
    System.out.println(dipwm.score(word));

    //DiPWM dipwm_2 = new DiPWMImporter().loadMotif("test_data/dipwm/AP2A.di"));

    run_mono_and_di(pwm, new WordwiseBackground(), discretizer, max_hash_size, threshold);
    run_mono_and_di(pwm, new Background(new double[] {0.1, 0.4, 0.4, 0.1}), discretizer, max_hash_size, threshold);
    run_mono_and_di(pwm, new Background(new double[] {0.25, 0.25, 0.25, 0.25}), discretizer, max_hash_size, threshold);
  }
}
