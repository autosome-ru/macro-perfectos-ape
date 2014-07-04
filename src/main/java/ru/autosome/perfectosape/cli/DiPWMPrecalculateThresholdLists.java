package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.backgroundModels.DiBackground;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.calculations.PrecalculateThresholdList;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.util.ArrayList;
import java.util.Collections;

public class DiPWMPrecalculateThresholdLists extends PrecalculateThresholdListsGeneralized<DiPWM,DiBackgroundModel> {

  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }
  @Override
  protected void extract_background(String str) {
    background = DiBackground.fromString(str);
  }

  @Override
  PrecalculateThresholdList calculator() {
    return new PrecalculateThresholdList<DiPWM, DiBackgroundModel>(pvalues, discretizer, background, pvalue_boundary, max_hash_size);
  }

  @Override
  DiPWMImporter motifImporter() {
    return new DiPWMImporter(background, data_model, effective_count);
  }

  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }

  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.perfectosape.cli.DiPWMPrecalculateThresholdLists";
  }

  protected DiPWMPrecalculateThresholdLists() {
    initialize_defaults();
  }

  protected static DiPWMPrecalculateThresholdLists from_arglist(ArrayList<String> argv) {
    DiPWMPrecalculateThresholdLists result = new DiPWMPrecalculateThresholdLists();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static DiPWMPrecalculateThresholdLists from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      DiPWMPrecalculateThresholdLists calculation = DiPWMPrecalculateThresholdLists.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new PrecalculateThresholdLists().documentString());
      System.exit(1);
    }
  }
}
