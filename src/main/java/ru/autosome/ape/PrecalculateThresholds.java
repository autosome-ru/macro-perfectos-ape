package ru.autosome.ape;

import ru.autosome.ape.calculation.PrecalculateThresholdList;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PWM;

import java.io.File;

public class PrecalculateThresholds extends ru.autosome.ape.cli.generalized.PrecalculateThresholds<PWM, BackgroundModel> {

  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }
  @Override
  protected void extract_background(String str) {
    background = Background.fromString(str);
  }

  @Override
  protected PrecalculateThresholdList<PWM, BackgroundModel> calculator() {
    return new PrecalculateThresholdList<>(pvalues, discretizer, background, pvalue_boundary);
  }

  @Override
  protected Named<PWM> loadMotif(File file){
    PWMImporter importer = new PWMImporter(background, data_model, effective_count, transpose, pseudocount);
    return importer.loadMotifWithName(file);
  }

  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }

  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.ape.PrecalculateThresholds";
  }

  protected static PrecalculateThresholds from_arglist(String[] args) {
    PrecalculateThresholds result = new PrecalculateThresholds();
    result.setup_from_arglist(args);
    return result;
  }

  public static void main(String[] args) {
    try {
      PrecalculateThresholds calculation = PrecalculateThresholds.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new PrecalculateThresholds().documentString());
      System.exit(1);
    }
  }
}
