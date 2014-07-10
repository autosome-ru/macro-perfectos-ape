package ru.autosome.perfectosape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.motifModel.di.DiPWM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiSNPScan extends ru.autosome.perfectosape.cli.generalized.MultiSNPScan<DiPWM, DiBackgroundModel> {
  @Override
  protected String DOC_run_string(){
    return "java ru.autosome.perfectosape.di.MultiSNPScan";
  }
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }

  private MultiSNPScan() {
    super();
  }

  @Override
  protected void extract_background(String str) {
    background = DiBackground.fromString(str);
  }
  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }

  @Override
  protected List<DiPWM> load_collection_of_pwms() {
    DiPWMImporter importer = new DiPWMImporter(background, dataModel, effectiveCount, transpose);
    return importer.loadMotifCollection(path_to_collection_of_pwms);
  }

  protected static ru.autosome.perfectosape.cli.generalized.MultiSNPScan from_arglist(ArrayList<String> argv) {
    MultiSNPScan result = new MultiSNPScan();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static ru.autosome.perfectosape.cli.generalized.MultiSNPScan from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      ru.autosome.perfectosape.cli.generalized.MultiSNPScan calculation = ru.autosome.perfectosape.di.MultiSNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new MultiSNPScan().documentString());
      System.exit(1);

    }
  }
}
