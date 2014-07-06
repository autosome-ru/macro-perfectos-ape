package ru.autosome.macroape.di;

import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.macroape.calculation.generalized.CompareModel;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.motifModel.di.DiPWM;

import java.util.ArrayList;
import java.util.Collections;

public class EvalSimilarity extends ru.autosome.macroape.cli.generalized.EvalSimilarity<DiPWM, DiBackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.di.EvalSimilarity";
  }

  protected String DOC_additional_options() {
    return "These options can be used for PWM vs DiPWM comparison:\n" +
           "  [--first-from-mono]  - obtain first DiPWM from mono PWM/PCM/PPM.\n" +
           "  [--second-from-mono] - obtain second DiPWM from mono PWM/PCM/PPM.\n" +
           "  [--first-mono-background <background>]  - ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
           "  [--second-mono-background <background>] - ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
           "                                            Mononucleotide background for PCM/PPM --> PWM conversion of mono models\n";
  }

  boolean firstPWMFromMononucleotide, secondPWMFromMononucleotide;
  BackgroundModel firstBackgroundMononucleotide, secondBackgroundMononucleotide;

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

    firstPWMFromMononucleotide = false;
    secondPWMFromMononucleotide = false;
    firstBackgroundMononucleotide = new WordwiseBackground();
    secondBackgroundMononucleotide = new WordwiseBackground();
  }

  @Override
  protected DiBackgroundModel extract_background(String str) {
    return DiBackground.fromString(str);
  }

  protected boolean recognize_additional_options(String opt, ArrayList<String> argv) {
    if (opt.equals("--first-from-mono")) {
      firstPWMFromMononucleotide= true;
      return true;
    } else if (opt.equals("--second-from-mono")) {
      secondPWMFromMononucleotide = true;
      return true;
    } else if (opt.equals("--first-mono-background")) {
      firstBackgroundMononucleotide = Background.fromString(argv.remove(0));
      return true;
    } else if (opt.equals("--second-mono-background")) {
      secondBackgroundMononucleotide = Background.fromString(argv.remove(0));
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void extractFirstPWM() {
    if (firstPWMFromMononucleotide) {
      PWMImporter firstMotifImporter = new PWMImporter(firstBackgroundMononucleotide, dataModelFirst, effectiveCountFirst);
      firstPWM = DiPWM.fromPWM( firstMotifImporter.loadPWMFromParser(PMParser.from_file_or_stdin(firstPMFilename)) );
    } else {
      DiPWMImporter firstMotifImporter = new DiPWMImporter(firstBackground, dataModelFirst, effectiveCountFirst);
      firstPWM = firstMotifImporter.loadPWMFromParser(PMParser.from_file_or_stdin(firstPMFilename));
    }
  }
  @Override
  protected void extractSecondPWM() {
    if (secondPWMFromMononucleotide) {
      PWMImporter secondMotifImporter = new PWMImporter(secondBackgroundMononucleotide, dataModelSecond, effectiveCountSecond);
      secondPWM = DiPWM.fromPWM( secondMotifImporter.loadPWMFromParser(PMParser.from_file_or_stdin(secondPMFilename)) );
    } else {
      DiPWMImporter secondMotifImporter = new DiPWMImporter(secondBackground, dataModelSecond, effectiveCountSecond);
      secondPWM = secondMotifImporter.loadPWMFromParser(PMParser.from_file_or_stdin(secondPMFilename));
    }
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

  ru.autosome.macroape.calculation.di.CompareModel calculator() {
    ru.autosome.macroape.calculation.di.CompareModel result = new ru.autosome.macroape.calculation.di.CompareModel(firstPWM, secondPWM,
                                           firstBackground, secondBackground,
                                           new FindPvalueAPE<DiPWM, DiBackgroundModel>(firstPWM, firstBackground, discretization, maxHashSize),
                                           new FindPvalueAPE<DiPWM, DiBackgroundModel>(secondPWM, secondBackground, discretization, maxHashSize),
                                           discretization, maxPairHashSize);
    return result;
  }





  OutputInformation report_table() throws Exception {
    CompareModel.SimilarityInfo results = calculator().jaccard(thresholdFirst(), thresholdSecond());
    return report_table(results);
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
