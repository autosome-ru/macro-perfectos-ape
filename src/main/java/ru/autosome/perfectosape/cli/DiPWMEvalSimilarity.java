package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.DiBackground;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.calculations.CompareDiPWM;
import ru.autosome.perfectosape.calculations.CompareModels;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.util.ArrayList;
import java.util.Collections;

public class DiPWMEvalSimilarity extends EvalSimilarityGeneralized<DiPWM, DiBackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.perfectosape.cli.DiPWMEvalSimilarity";
  }

  private void initialize_defaults() {
    firstBackground = new DiWordwiseBackground();
    secondBackground = new DiWordwiseBackground();
    dataModelFirst = DataModel.PWM;
    dataModelSecond = DataModel.PWM;
    effectiveCountFirst = 100.0;
    effectiveCountSecond = 100.0;
    pvalue = 0.0005;
    discretization = 10.0;

    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    pvalueBoundary = BoundaryType.UPPER;
  }

  @Override
  protected DiBackgroundModel extract_background(String str) {
    return DiBackground.fromString(str);
  }

  @Override
  protected DiPWMImporter firstMotifImporter(){
    return new DiPWMImporter(firstBackground, dataModelFirst, effectiveCountFirst);
  }
  @Override
  protected DiPWMImporter secondMotifImporter(){
    return new DiPWMImporter(secondBackground, dataModelSecond, effectiveCountSecond);
  }

  private DiPWMEvalSimilarity() {
    initialize_defaults();
  }

  private static DiPWMEvalSimilarity from_arglist(ArrayList<String> argv) {
    DiPWMEvalSimilarity result = new DiPWMEvalSimilarity();
    ru.autosome.perfectosape.cli.Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  private static DiPWMEvalSimilarity from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  CompareDiPWM calculator() {
    CompareDiPWM result = new CompareDiPWM(firstPWM, secondPWM,
                                           firstBackground, secondBackground,
                                           new FindPvalueAPE(firstPWM, firstBackground, discretization, maxHashSize),
                                           new FindPvalueAPE(secondPWM, secondBackground, discretization, maxHashSize),
                                           discretization, maxPairHashSize);
    return result;
  }





  OutputInformation report_table() throws Exception {
    CompareModels.SimilarityInfo results = calculator().jaccard(thresholdFirst(), thresholdSecond());
    return report_table(results);
  }

  public static void main(String[] args) {
    try {
      DiPWMEvalSimilarity cli = DiPWMEvalSimilarity.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new DiPWMEvalSimilarity().documentString());
      System.exit(1);
    }
  }
}
