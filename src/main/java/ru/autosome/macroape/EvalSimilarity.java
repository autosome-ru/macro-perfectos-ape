package ru.autosome.macroape;

import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.mono.CompareModels;

public class EvalSimilarity extends ru.autosome.macroape.cli.generalized.EvalSimilarity<PWM, BackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.EvalSimilarity";
  }

  @Override
  protected void initialize_default_background() {
    firstBackground = new WordwiseBackground();
    secondBackground = new WordwiseBackground();
  }

  @Override
  protected BackgroundModel extract_background(String str) {
    return Background.fromString(str);
  }

  @Override
  protected PWM loadFirstPWM(String filename) {
    PWMImporter firstMotifImporter = new PWMImporter(firstBackground, dataModelFirst, effectiveCountFirst, transposeFirst, pseudocountFirst);
    return firstMotifImporter.loadMotif(filename);
  }
  @Override
  protected PWM loadSecondPWM(String filename) {
    PWMImporter secondMotifImporter = new PWMImporter(secondBackground, dataModelSecond, effectiveCountSecond, transposeSecond, pseudocountSecond);
    return secondMotifImporter.loadMotif(filename);
  }

  private static EvalSimilarity from_arglist(String[] args) {
    EvalSimilarity result = new EvalSimilarity();
    result.setup_from_arglist(args);
    return result;
  }

  @Override
  protected CompareModels calculator() {
    return new CompareModels(firstPWM, secondPWM, firstBackground, secondBackground, discretizer);
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
