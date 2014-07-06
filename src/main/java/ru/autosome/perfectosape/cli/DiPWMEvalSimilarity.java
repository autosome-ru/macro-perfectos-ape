package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.*;
import ru.autosome.perfectosape.calculations.CompareDiPWM;
import ru.autosome.perfectosape.calculations.CompareModels;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.importers.PWMImporter;
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

  protected String DOC_additional_options() {
    return "These options can be used for PWM vs DiPWM comparison:\n" +
           "  [--first-from-mononucleotide]  - obtain first DiPWM from mononucleotide PWM/PCM/PPM.\n" +
           "  [--second-from-mononucleotide] - obtain second DiPWM from mononucleotide PWM/PCM/PPM.\n" +
           "  [--first-mono-background <background>]  - ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
           "  [--second-mono-background <background>] - ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
           "                                            Mononucleotide background for PCM/PPM --> PWM conversion of mononucleotide models\n";
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
    if (opt.equals("--first-from-mononucleotide")) {
      firstPWMFromMononucleotide= true;
      return true;
    } else if (opt.equals("--second-from-mononucleotide")) {
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
