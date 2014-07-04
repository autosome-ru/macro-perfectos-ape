package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.backgroundModels.DiBackground;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueBsearchBuilder;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.importers.MotifCollectionImporter;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiPWMMultiSNPScan extends MultiSNPScanGeneralized<DiBackgroundModel> {
  @Override
  protected String DOC_run_string(){
    return "java ru.autosome.perfectosape.cli.DiPWMMultiSNPScan";
  }
  @Override
  protected String DOC_background_option() {
    // ToDo: fix help string for diBackground
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2";
  }

  private DiPWMMultiSNPScan() {
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
  protected void load_collection_of_pwms() throws FileNotFoundException {
    DiPWMImporter pwmImporter = new DiPWMImporter(background, dataModel, effectiveCount);
    MotifCollectionImporter<DiPWM> importer = new MotifCollectionImporter<DiPWM>(pwmImporter);
    List<DiPWM> pwmList = importer.loadPWMCollection(path_to_collection_of_pwms);

    pwmCollection = new ArrayList<ThresholdEvaluator>();
    for (DiPWM motif: pwmList) {
      CanFindPvalue pvalueCalculator;
      if (thresholds_folder == null) {
        pvalueCalculator = new FindPvalueAPE<DiPWM, DiBackgroundModel>(motif, background, discretizer, max_hash_size);
      } else {
        pvalueCalculator = new FindPvalueBsearchBuilder(thresholds_folder).pvalueCalculator(motif);
      }
      pwmCollection.add(new ThresholdEvaluator(motif, pvalueCalculator, motif.getName()));
    }
  }

  protected static MultiSNPScanGeneralized from_arglist(ArrayList<String> argv) throws FileNotFoundException {
    DiPWMMultiSNPScan result = new DiPWMMultiSNPScan();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static MultiSNPScanGeneralized from_arglist(String[] args) throws FileNotFoundException {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      MultiSNPScanGeneralized calculation = DiPWMMultiSNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new DiPWMMultiSNPScan().documentString());
      System.exit(1);

    }
  }
}
