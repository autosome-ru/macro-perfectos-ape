package ru.autosome.ape;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FindThresholdBsearchBuilder;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PWM;

public class FindThreshold extends ru.autosome.ape.cli.generalized.FindThreshold<PWM, BackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.ape.FindThreshold";
  }
  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }

  @Override
  protected Named<PWM> loadMotif(String filename) {
    PWMImporter importer = new PWMImporter(background, data_model, effective_count, transpose, pseudocount);
    return importer.loadMotifWithName(filename);
  }

  @Override
  protected void extract_background(String str) {
    background = Background.fromString(str);
  }

  @Override
  protected CanFindThreshold calculator() {
    if (cache_calculator == null) {
      if (thresholds_folder == null) {
        cache_calculator = new FindThresholdAPE<PWM, BackgroundModel>(motif.getObject(), background, discretizer);
      } else {
        cache_calculator = new FindThresholdBsearchBuilder(thresholds_folder).thresholdCalculator(motif.getName());
      }
    }
    return cache_calculator;
  }

  private static FindThreshold from_arglist(String[] args) {
    FindThreshold result = new FindThreshold();
    result.setup_from_arglist(args);
    return result;
  }

  public static void main(String args[]) {
    try {
      FindThreshold cli = FindThreshold.from_arglist(args);
      System.out.println(cli.report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new FindThreshold().documentString());
      System.exit(1);
    }
  }

}
