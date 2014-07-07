package ru.autosome.macroape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.types.DataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanCollection extends ru.autosome.macroape.cli.generalized.ScanCollection<DiPWM, DiBackgroundModel> {

  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.di.ScanCollection";
  }

  protected DiBackgroundModel extractBackground(String str) {
    return DiBackground.fromString(str);
  }

  private void initialize_defaults() {
    queryBackground = new DiWordwiseBackground();
    collectionBackground = new DiWordwiseBackground();
    roughDiscretization = 1.0;
    preciseDiscretization = 10.0;
    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    dataModel = DataModel.PWM;
    effectiveCount = 100.0;
    thresholds_folder = null;
    silenceLog = false;
    pvalueBoundaryType = BoundaryType.UPPER;
    pvalue = 0.0005;
    similarityCutoff = 0.05;
    preciseRecalculationCutoff = null;
  }

  private ScanCollection() {
    initialize_defaults();
  }

  private static ScanCollection from_arglist(ArrayList<String> argv) {
    ScanCollection result = new ScanCollection();
    Helper.print_help_if_requested(argv, new ScanCollection().documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  private static ScanCollection from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  protected MotifImporter<DiPWM> motifImporter(DiBackgroundModel background, DataModel dataModel, Double effectiveCount) {
    return new DiPWMImporter(background, dataModel, effectiveCount);
  }

  protected ru.autosome.macroape.calculation.di.ScanCollection calculator() {
    ru.autosome.macroape.calculation.di.ScanCollection calculator;
    calculator = new ru.autosome.macroape.calculation.di.ScanCollection(pwmCollection, queryPWM);
    calculator.pvalue = pvalue;
    calculator.queryPredefinedThreshold = queryPredefinedThreshold;
    calculator.roughDiscretization = roughDiscretization;
    calculator.preciseDiscretization = preciseDiscretization;
    calculator.queryBackground = queryBackground;
    calculator.collectionBackground = collectionBackground;
    calculator.pvalueBoundaryType = pvalueBoundaryType;
    calculator.maxHashSize = maxHashSize;
    calculator.maxPairHashSize = maxPairHashSize;
    calculator.similarityCutoff = similarityCutoff;
    calculator.preciseRecalculationCutoff = preciseRecalculationCutoff;
    return calculator;
  }


  public static void main(String[] args) {
    try {
      ScanCollection calculation = ScanCollection.from_arglist(args);
      List<? extends ResultInfo> infos = calculation.process();
      System.out.println(calculation.report_table(infos).report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ScanCollection().documentString());
      System.exit(1);
    }
  }
}
