package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.backgroundModels.Background;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.PrecalculateThresholdList;
import ru.autosome.perfectosape.importers.PWMImporter;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.Collections;

public class PrecalculateThresholdLists extends PrecalculateThresholdListsGeneralized<PWM, BackgroundModel>  {

  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }
  @Override
  protected void extract_background(String str) {
    background = Background.fromString(str);
  }

  @Override
  PrecalculateThresholdList calculator() {
    return new PrecalculateThresholdList<PWM,BackgroundModel>(pvalues, discretizer, background, pvalue_boundary, max_hash_size);
  }

  @Override
  PWMImporter motifImporter() {
    return new PWMImporter(background, data_model, effective_count);
  }

  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }

  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.perfectosape.cli.PrecalculateThresholdLists";
  }

  protected PrecalculateThresholdLists() {
    initialize_defaults();
  }

  private static PrecalculateThresholdLists from_arglist(ArrayList<String> argv) {
    PrecalculateThresholdLists result = new PrecalculateThresholdLists();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  private static PrecalculateThresholdLists from_arglist(String[] args) {
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
