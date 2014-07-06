package ru.autosome.ape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.ape.calculation.PrecalculateThresholdList;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.cli.Helper;

import java.util.ArrayList;
import java.util.Collections;

public class PrecalculateThresholdLists extends ru.autosome.ape.cli.generalized.PrecalculateThresholdLists<DiPWM,DiBackgroundModel> {

  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }
  @Override
  protected void extract_background(String str) {
    background = DiBackground.fromString(str);
  }

  @Override
  protected PrecalculateThresholdList<DiPWM, DiBackgroundModel> calculator() {
    return new PrecalculateThresholdList<DiPWM, DiBackgroundModel>(pvalues, discretization, background, pvalue_boundary, max_hash_size);
  }

  @Override
  protected DiPWMImporter motifImporter() {
    return new DiPWMImporter(background, data_model, effective_count);
  }

  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }

  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.ape.di.PrecalculateThresholdLists";
  }

  protected PrecalculateThresholdLists() {
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
      PrecalculateThresholdLists calculation = ru.autosome.ape.di.PrecalculateThresholdLists.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ru.autosome.ape.PrecalculateThresholdLists().documentString());
      System.exit(1);
    }
  }
}
