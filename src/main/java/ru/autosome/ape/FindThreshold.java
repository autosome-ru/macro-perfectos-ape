package ru.autosome.ape;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FindThresholdBsearch;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PWM;

import java.io.File;
import java.io.FileNotFoundException;

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
    if (thresholds_folder == null) {
      return new FindThresholdAPE<>(motif.getObject(), background, discretizer);
    } else {
      File thresholds_file = new File(thresholds_folder, motif.getName() + ".thr");
      try {
        return new FindThresholdBsearch(thresholds_file);
      } catch (FileNotFoundException e) {
        System.err.println("Thresholds file `" + thresholds_file + "` not found. Fallback to APE-calculation of threshold");
        return new FindThresholdAPE<>(motif.getObject(), background, discretizer);
      }
    }
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
