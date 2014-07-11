package ru.autosome.macroape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PWM;

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

  @Override
  protected String DOC_additional_options() {
    return "These options can be used for PWM vs DiPWM comparison:\n" +
     "  [--query-from-mono]      - obtain query DiPWM from mono PWM/PCM/PPM.\n" +
     "  [--collection-from-mono] - obtain collection DiPWMs from mono PWM/PCM/PPMs.\n" +
     "  [--query-mono-background <background>]      - ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
     "  [--collection-mono-background <background>] - ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
     "                                                Mononucleotide background for PCM/PPM --> PWM conversion of mono models\n";
  }

  boolean queryFromMononucleotide, collectionFromMononucleotide;
  BackgroundModel queryBackgroundMononucleotide, collectionBackgroundMononucleotide;

  @Override
  protected void initialize_defaults() {
    super.initialize_defaults();
    queryFromMononucleotide = false;
    collectionFromMononucleotide = false;
    queryBackgroundMononucleotide = new WordwiseBackground();
    collectionBackgroundMononucleotide = new WordwiseBackground();
  }

  @Override
  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    if (opt.equals("--query-from-mono")) {
      queryFromMononucleotide = true;
      return false;
    } else if (opt.equals("--collection-from-mono")) {
      collectionFromMononucleotide = true;
      return false;
    } else if (opt.equals("--query-mono-background")) {
      queryBackgroundMononucleotide = Background.fromString(argv.remove(0));
      return false;
    } else if (opt.equals("--collection-mono-background")) {
      collectionBackgroundMononucleotide = Background.fromString(argv.remove(0));
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected DiBackgroundModel extractBackground(String str) {
    return DiBackground.fromString(str);
  }

  @Override
  protected void initialize_default_background() {
    queryBackground = new DiWordwiseBackground();
    collectionBackground = new DiWordwiseBackground();
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

  @Override
  protected List<DiPWM> loadMotifCollection() {
    if (collectionFromMononucleotide) {
      PWMImporter importer = new PWMImporter(collectionBackgroundMononucleotide, dataModel, effectiveCount, collectionTranspose);
      List<PWM> monoCollection = importer.loadMotifCollection(pathToCollectionOfPWMs);
      List<DiPWM> diCollection = new ArrayList<DiPWM>(monoCollection.size());
      for(PWM monoPWM: monoCollection) {
        diCollection.add(DiPWM.fromPWM(monoPWM));
      }
      return diCollection;
    } else {
      DiPWMImporter importer = new DiPWMImporter(collectionBackground, dataModel, effectiveCount, collectionTranspose);
      return importer.loadMotifCollection(pathToCollectionOfPWMs);
    }
  }

  @Override
  protected DiPWM loadQueryMotif() {
    if (queryFromMononucleotide) {
      PWMImporter importer = new PWMImporter(queryBackgroundMononucleotide, dataModel, effectiveCount, queryTranspose);
      return DiPWM.fromPWM(importer.loadMotif(queryPMFilename));
    } else {
      DiPWMImporter importer = new DiPWMImporter(queryBackground, dataModel, effectiveCount, queryTranspose);
      return importer.loadMotif(queryPMFilename);
    }
  }

  protected ru.autosome.macroape.calculation.di.ScanCollection calculator() {
    ru.autosome.macroape.calculation.di.ScanCollection calculator;
    calculator = new ru.autosome.macroape.calculation.di.ScanCollection(pwmCollection, queryPWM);
    calculator.pvalue = pvalue;
    calculator.queryPredefinedThreshold = queryPredefinedThreshold;
    calculator.roughDiscretizer = roughDiscretizer;
    calculator.preciseDiscretizer = preciseDiscretizer;
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
