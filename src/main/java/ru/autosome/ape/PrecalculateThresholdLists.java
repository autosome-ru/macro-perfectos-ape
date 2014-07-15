package ru.autosome.ape;

import ru.autosome.ape.calculation.PrecalculateThresholdList;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.mono.PWM;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PrecalculateThresholdLists extends ru.autosome.ape.cli.generalized.PrecalculateThresholdLists<PWM, BackgroundModel> {

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
    return new PrecalculateThresholdList<PWM,BackgroundModel>(pvalues, discretizer, background, pvalue_boundary, max_hash_size);
  }

  @Override
  protected PWM loadMotif(File file){
    PWMImporter importer = new PWMImporter(background, data_model, effective_count, transpose);
    return importer.loadMotif(file);
  }

  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }

  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.ape.PrecalculateThresholdLists";
  }

  public PrecalculateThresholdLists() {
    initialize_defaults();
  }

  protected static PrecalculateThresholdLists from_arglist(ArrayList<String> argv) {
    PrecalculateThresholdLists result = new PrecalculateThresholdLists();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static PrecalculateThresholdLists from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      PrecalculateThresholdLists calculation = PrecalculateThresholdLists.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new PrecalculateThresholdLists().documentString());
      System.exit(1);
    }
  }
}
