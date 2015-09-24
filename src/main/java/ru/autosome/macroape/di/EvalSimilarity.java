package ru.autosome.macroape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.DiPWMFromMonoImporter;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.di.CompareModels;

import java.util.ArrayList;
import java.util.Collections;
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

  @Override
  protected CompareModels calculator() {
    return new CompareModels(firstPWM, secondPWM, firstBackground, secondBackground, discretizer, maxPairHashSize, maxHashSize);
  }

  public static void main(String[] args) {
    try {
      EvalSimilarity cli = ru.autosome.macroape.di.EvalSimilarity.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new EvalSimilarity().documentString());
      System.exit(1);
    }
  }
}
