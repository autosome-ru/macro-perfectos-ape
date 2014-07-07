package ru.autosome.perfectosape;

import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearchBuilder;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.MotifCollectionImporter;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiSNPScan extends ru.autosome.perfectosape.cli.generalized.MultiSNPScan<BackgroundModel> {
  @Override
  protected String DOC_run_string(){
    return "java ru.autosome.perfectosape.MultiSNPScan";
  }
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }

  private MultiSNPScan() {
    super();
  }

  @Override
  protected void extract_background(String str) {
    background = Background.fromString(str);
  }
  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }

  @Override
  protected void load_collection_of_pwms() {
    PWMImporter pwmImporter = new PWMImporter(background, dataModel, effectiveCount);
    MotifCollectionImporter<PWM> importer = new MotifCollectionImporter<PWM>(pwmImporter);
    List<PWM> pwmList = importer.loadPWMCollection(path_to_collection_of_pwms);

    pwmCollection = new ArrayList<ThresholdEvaluator>();
    for (PWM pwm: pwmList) {
      CanFindPvalue pvalueCalculator;
      if (thresholds_folder == null) {
        pvalueCalculator = new FindPvalueAPE<PWM, BackgroundModel>(pwm, background, discretizer, max_hash_size);
      } else {
        pvalueCalculator = new FindPvalueBsearchBuilder(thresholds_folder).pvalueCalculator(pwm);
      }
      pwmCollection.add(new ThresholdEvaluator(pwm, pvalueCalculator, pwm.getName()));
    }
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
      ru.autosome.perfectosape.cli.generalized.MultiSNPScan calculation = MultiSNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new MultiSNPScan().documentString());
      System.exit(1);

    }
  }
}
