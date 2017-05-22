package ru.autosome.macroape;

import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.model.ScanningSimilarityInfo;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.cli.generalized.ScanCollection<PWM, BackgroundModel> {

  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.ScanCollection";
  }

  @Override
  protected BackgroundModel extractBackground(String str) {
    return Background.fromString(str);
  }

  @Override
  protected void initialize_default_background() {
    queryBackground = new WordwiseBackground();
    collectionBackground = new WordwiseBackground();
  }

  private static ScanCollection from_arglist(String[] args) {
    ScanCollection result = new ScanCollection();
    result.setup_from_arglist(args);
    return result;
  }

  protected List<Named<PWM>> loadMotifCollection() {
    PWMImporter importer = new PWMImporter(collectionBackground, collectionDataModel, collectionEffectiveCount, collectionTranspose, collectionPseudocount);
    return importer.loadMotifCollectionWithNames(pathToCollectionOfPWMs);
  }

  protected PWM loadQueryMotif() {
    PWMImporter importer = new PWMImporter(queryBackground, queryDataModel, queryEffectiveCount, queryTranspose, queryPseudocount);
    return importer.loadMotif(queryPMFilename);
  }


  protected ru.autosome.macroape.calculation.mono.ScanCollection calculator() {
    ru.autosome.macroape.calculation.mono.ScanCollection calculator;
    calculator = new ru.autosome.macroape.calculation.mono.ScanCollection(pwmCollection, queryPWM);
    calculator.pvalue = pvalue;
    calculator.queryPredefinedThreshold = queryPredefinedThreshold;
    calculator.roughDiscretizer = roughDiscretizer;
    calculator.preciseDiscretizer = preciseDiscretizer;
    calculator.queryBackground = queryBackground;
    calculator.collectionBackground = collectionBackground;
    calculator.pvalueBoundaryType = pvalueBoundaryType;
    calculator.similarityCutoff = similarityCutoff;
    calculator.preciseRecalculationCutoff = preciseRecalculationCutoff;
    return calculator;
   }


  public static void main(String[] args) {
    try {
      ScanCollection calculation = ScanCollection.from_arglist(args);
      List<ScanningSimilarityInfo> infos = calculation.process();
      System.out.println(calculation.report(infos));
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ScanCollection().documentString());
      System.exit(1);
    }
  }
}
