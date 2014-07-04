package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.Background;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdBsearchBuilder;
import ru.autosome.perfectosape.importers.MotifImporter;
import ru.autosome.perfectosape.importers.PWMImporter;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.Collections;

public class FindThreshold extends FindThresholdGeneralized<PWM, BackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.perfectosape.cli.FindThreshold";
  }
  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }
  @Override
  protected MotifImporter<PWM> motifImporter() {
    return new PWMImporter(background, data_model, effective_count);
  }
  @Override
  protected void extract_background(String str) {
    background = Background.fromString(str);
  }
  @Override
  CanFindThreshold calculator() {
    if (cache_calculator == null) {
      if (thresholds_folder == null) {
        cache_calculator = new FindThresholdAPE<PWM, BackgroundModel>(motif, background, discretizer, max_hash_size);
      } else {
        cache_calculator = new FindThresholdBsearchBuilder(thresholds_folder).thresholdCalculator(motif);
      }
    }
    return cache_calculator;
  }

  public FindThreshold() {
    initialize_defaults();
  }

  private static FindThreshold from_arglist(ArrayList<String> argv) {
    FindThreshold result = new FindThreshold();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  private static FindThreshold from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String args[]) {
    try {
      FindThreshold cli = FindThreshold.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new FindThreshold().documentString());
      System.exit(1);
    }
  }

}
