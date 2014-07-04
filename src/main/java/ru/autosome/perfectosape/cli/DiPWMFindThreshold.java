package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.DiBackground;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdBsearchBuilder;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.importers.MotifImporter;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.util.ArrayList;
import java.util.Collections;

public class DiPWMFindThreshold extends FindThresholdGeneralized<DiPWM, DiBackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.perfectosape.cli.DiPWMFindThreshold";
  }
  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }
  @Override
  protected MotifImporter<DiPWM> motifImporter() {
    return new DiPWMImporter(background, data_model, effective_count);
  }
  @Override
  protected void extract_background(String str) {
    background = DiBackground.fromString(str);
  }
  @Override
  CanFindThreshold calculator() {
    if (cache_calculator == null) {
      if (thresholds_folder == null) {
        cache_calculator = new FindThresholdAPE<DiPWM, DiBackgroundModel>(motif, background, discretizer, max_hash_size);
      } else {
        cache_calculator = new FindThresholdBsearchBuilder(thresholds_folder).thresholdCalculator(motif);
      }
    }
    return cache_calculator;
  }

  public DiPWMFindThreshold() {
    initialize_defaults();
  }

  protected static DiPWMFindThreshold from_arglist(ArrayList<String> argv) {
    DiPWMFindThreshold result = new DiPWMFindThreshold();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static DiPWMFindThreshold from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String args[]) {
    try {
      DiPWMFindThreshold cli = DiPWMFindThreshold.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new DiPWMFindPvalue().documentString());
      System.exit(1);
    }
  }

}
