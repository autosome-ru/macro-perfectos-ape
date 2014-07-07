package ru.autosome.macroape;

import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;
import ru.autosome.macroape.calculation.mono.CompareModels;

import java.util.ArrayList;
import java.util.Collections;

public class EvalSimilarity extends ru.autosome.macroape.cli.generalized.EvalSimilarity<PWM, BackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.EvalSimilarity";
  }

  private void initialize_defaults() {
    firstBackground = new WordwiseBackground();
    secondBackground = new WordwiseBackground();
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
  protected BackgroundModel extract_background(String str) {
    return Background.fromString(str);
  }

  @Override
  protected void extractFirstPWM() {
    PWMImporter firstMotifImporter = new PWMImporter(firstBackground, dataModelFirst, effectiveCountFirst);
    firstPWM = firstMotifImporter.loadPWMFromParser(PMParser.from_file_or_stdin(firstPMFilename));
  }
  @Override
  protected void extractSecondPWM() {
    PWMImporter secondMotifImporter = new PWMImporter(secondBackground, dataModelSecond, effectiveCountSecond);
    secondPWM = secondMotifImporter.loadPWMFromParser(PMParser.from_file_or_stdin(secondPMFilename));
  }


  private EvalSimilarity() {
    initialize_defaults();
  }

  private static EvalSimilarity from_arglist(ArrayList<String> argv) {
    EvalSimilarity result = new EvalSimilarity();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  private static EvalSimilarity from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  CompareModels calculator() {
    return new CompareModels(firstPWM, secondPWM, firstBackground, secondBackground, discretization, maxPairHashSize, maxHashSize);
  }

  OutputInformation report_table() throws Exception {
    CompareModelsCountsGiven.SimilarityInfo results = calculator().jaccard(thresholdFirst(), thresholdSecond());
    return report_table(results);
  }

  public static void main(String[] args) {
    try {
      EvalSimilarity cli = EvalSimilarity.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new EvalSimilarity().documentString());
      System.exit(1);
    }
  }
}
