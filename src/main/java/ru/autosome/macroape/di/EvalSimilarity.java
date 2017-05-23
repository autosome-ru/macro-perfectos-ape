package ru.autosome.macroape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.cli.*;
import ru.autosome.commons.importer.DiPWMFromMonoImporter;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.di.CompareModels;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;

import java.util.List;

public class EvalSimilarity extends ru.autosome.macroape.cli.generalized.EvalSimilarity<DiPWM, DiBackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.di.EvalSimilarity";
  }

  @Override
  protected String DOC_additional_options() {
    return "  [--first-from-mono]  - obtain first DiPWM from mono PWM/PCM/PPM.\n" +
           "  [--second-from-mono] - obtain second DiPWM from mono PWM/PCM/PPM.\n";
  }

  boolean firstPWMFromMononucleotide, secondPWMFromMononucleotide;

  @Override
  protected void initialize_defaults() {
    super.initialize_defaults();
    firstPWMFromMononucleotide = false;
    secondPWMFromMononucleotide = false;
  }

  @Override
  protected void initialize_default_background() {
    firstBackground = new DiWordwiseBackground();
    secondBackground = new DiWordwiseBackground();
  }

  @Override
  protected DiBackgroundModel extract_background(String str) {
    return DiBackground.fromString(str);
  }

  @Override
  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    // TODO: --from-mono-background and --from-mono
    // TODO: Make a pair of options --from-mono-background to PCM-->PWM conversion and to set dinucleotide background.
    // TODO: or it may be the same option (for now it's only PCM-->PWM conversion, not diPWM background)
    if (opt.equals("--first-from-mono")) {
      firstPWMFromMononucleotide= true;
      return false;
    } else if (opt.equals("--second-from-mono")) {
      secondPWMFromMononucleotide = true;
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected DiPWM loadFirstPWM(String filename) {
    MotifImporter<DiPWM> importer;
    if (firstPWMFromMononucleotide) {
      importer = new DiPWMFromMonoImporter(firstBackground, dataModelFirst, effectiveCountFirst, transposeFirst, pseudocountFirst);
    } else {
      importer = new DiPWMImporter(firstBackground, dataModelFirst, effectiveCountFirst, transposeFirst, pseudocountFirst);
    }
    return importer.loadMotif(filename);

  }
  @Override
  protected DiPWM loadSecondPWM(String filename) {
    MotifImporter<DiPWM> importer;
    if (secondPWMFromMononucleotide) {
      importer = new DiPWMFromMonoImporter(secondBackground, dataModelSecond, effectiveCountSecond, transposeSecond, pseudocountSecond);
    } else {
      importer = new DiPWMImporter(secondBackground, dataModelSecond, effectiveCountSecond, transposeSecond, pseudocountSecond);
    }
    return importer.loadMotif(filename);
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
      EvalSimilarity cli = ru.autosome.macroape.di.EvalSimilarity.from_arglist(args);
      ComparisonSimilarityInfo<DiPWM> result = cli.results();
      ReportLayout<ComparisonSimilarityInfo<DiPWM>> layout = cli.report_table_layout();
      Reporter<ComparisonSimilarityInfo<DiPWM>> reporter = new TextReporter<>();
      System.out.println(reporter.report(result, layout));
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new EvalSimilarity().documentString());
      System.exit(1);
    }
  }
}
