package ru.autosome.macroape;

import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearchBuilder;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FindThresholdBsearchBuilder;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.importer.MotifCollectionImporter;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.macroape.calculation.generalized.ScanCollectionSimilarityInfo;
import ru.autosome.macroape.calculation.generalized.ThresholdEvaluator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanCollection {

  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.macroape.ScanCollection <query PWM file> <folder with PWMs> [options]\n" +
    "\n" +
    "Options:\n" +
    "  [-p <P-value>]\n" +
    "  [-c <similarity cutoff>] minimal similarity to be included in output, '-c 0.05' by default, [--all] to print all results\n" +
    "  [--precise [<level>]] minimal similarity to check on the second pass in precise mode, off by default, '--precise 0.01' if level is not set\n" +
    "  [--rough-discretization <discretization level>] or [-d]\n" +
    "  [--precise-discretization <discretization level>]\n" +
    "  [--silent] - hide current progress information during scan (printed to stderr by default)\n" +
    "  [--pcm] - treat the query input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the query input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
    "                         Attention! Don't use threshold lists calculated for a different discretization (or background)!\n" +
    "\n" +
    "Output format:\n" +
    "           <name> <jaccard index> <shift> <overlap> <orientation> ['*' in case that result was calculated on the second pass (in precise mode), '.' otherwise]\n" +
    "              Attention! Name can contain whitespace characters.\n" +
    "              Attention! The shift and orientation are reported for the collection matrix relative to the query matrix.\n" +
    "\n" +
    "Examples:\n" +
    "  java ru.autosome.macroape.ScanCollection ./query_motif.pwm ./hocomoco/ --precalc ./hocomoco_thresholds\n" +
    "  java ru.autosome.macroape.ScanCollection ./query_motif.pcm ./hocomoco/ --pcm -p 0.0005 --precise 0.03\n";

  DataModel dataModel;
  Double effectiveCount;
  BackgroundModel queryBackground, collectionBackground;
  Double roughDiscretization, preciseDiscretization;
  Integer maxHashSize;
  Integer maxPairHashSize;
  double pvalue;
  Double queryPredefinedThreshold;
  Double similarityCutoff;
  Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  BoundaryType pvalueBoundaryType;
  boolean silenceLog;
  String queryPMFilename;
  File pathToCollectionOfPWMs;
  File thresholds_folder;
  PWM queryPWM;
  List<ThresholdEvaluator<PWM>> pwmCollection;

  private void initialize_defaults() {
    queryBackground = new WordwiseBackground();
    collectionBackground = new WordwiseBackground();
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
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static ScanCollection from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  private void extract_query_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    queryPMFilename = argv.remove(0);
  }

  void extract_path_to_collection_of_pwms(ArrayList<String> argv) {
    try {
     pathToCollectionOfPWMs = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  private List<ThresholdEvaluator<PWM>> load_collection_of_pwms() {
    PWMImporter pwmImporter = new PWMImporter(collectionBackground, dataModel, effectiveCount);
    MotifCollectionImporter<PWM> collectionImporter = new MotifCollectionImporter<PWM>(pwmImporter);
    List<PWM> pwmList = collectionImporter.loadPWMCollection(pathToCollectionOfPWMs);
    List<ThresholdEvaluator<PWM>> result;
    result = new ArrayList<ThresholdEvaluator<PWM>>();
    for (PWM pwm: pwmList) {
      if (thresholds_folder == null) {
        result.add(new ThresholdEvaluator(pwm,
                                          new FindThresholdAPE<PWM, BackgroundModel>(pwm, collectionBackground, roughDiscretization, maxHashSize),
                                          new FindThresholdAPE<PWM, BackgroundModel>(pwm, collectionBackground, preciseDiscretization, maxHashSize),
                                          new FindPvalueAPE<PWM, BackgroundModel>(pwm, collectionBackground, roughDiscretization, maxHashSize),
                                          new FindPvalueAPE<PWM, BackgroundModel>(pwm, collectionBackground, preciseDiscretization, maxHashSize)));
      } else {
        result.add(new ThresholdEvaluator(pwm,
                                          new FindThresholdBsearchBuilder(thresholds_folder).thresholdCalculator(pwm),
                                          null,
                                          new FindPvalueBsearchBuilder(thresholds_folder).pvalueCalculator(pwm),
                                          null));
      }
    }
    return result;
  }

  void setup_from_arglist(ArrayList<String> argv) {
    extract_query_pm_filename(argv);
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }

    queryPWM = new PWMImporter(queryBackground,
                               dataModel,
                               effectiveCount).loadPWMFromParser(PMParser.from_file_or_stdin(queryPMFilename));
    pwmCollection = load_collection_of_pwms();
  }

  private void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      BackgroundModel background = Background.fromString(argv.remove(0));
      queryBackground = background;
      collectionBackground = background;
    } else if (opt.equals("--query-background")) {
      queryBackground = Background.fromString(argv.remove(0));
    } else if (opt.equals("--collection-background")) {
      collectionBackground = Background.fromString(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      maxHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--max-2d-hash-size")) {
      maxPairHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--rough-discretization") || opt.equals("-d")) {
      roughDiscretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundaryType = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--pcm")) {
      dataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if(opt.equals("-p")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--predefined-threshold")) {
      queryPredefinedThreshold = Double.valueOf(argv.remove(0));
    } else if(opt.equals("-c")) {
      similarityCutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--all")) {
      similarityCutoff = 0.0;
    } else if(opt.equals("--precise")) {
      preciseRecalculationCutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--silent")) {
      silenceLog = true;
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.add_parameter("MS", "minimal similarity to output", similarityCutoff);
    infos.add_parameter("P", "P-value", pvalue);
    infos.add_parameter("PB", "P-value boundary", pvalueBoundaryType);
    if (preciseRecalculationCutoff != null) {
      infos.add_parameter("VR", "discretization value, rough", roughDiscretization);
      infos.add_parameter("VP", "discretization value, precise", preciseDiscretization);
      infos.add_parameter("MP", "minimal similarity for the 2nd pass in \'precise\' mode", preciseRecalculationCutoff);
    } else {
      infos.add_parameter("V", "discretization value", roughDiscretization);
    }
    infos.background_parameter("BQ", "background for query matrix", queryBackground);
    infos.background_parameter("BC", "background for collection", collectionBackground);

    infos.add_table_parameter_without_description("motif", "name");
    infos.add_table_parameter_without_description("similarity", "similarity");
    infos.add_table_parameter_without_description("shift", "shift");
    infos.add_table_parameter_without_description("overlap", "overlap");
    infos.add_table_parameter_without_description("orientation", "orientation");
    if (preciseRecalculationCutoff != null) {
      infos.add_table_parameter_without_description("precise mode", "precision_mode", new OutputInformation.Callback<ScanCollectionSimilarityInfo>(){
        @Override
        public String run(ScanCollectionSimilarityInfo cell) {
          return cell.precise ? "*" : ".";
        }
      });
    }
    return infos;
  }


  private ru.autosome.macroape.calculation.mono.ScanCollection calculator() {
    ru.autosome.macroape.calculation.mono.ScanCollection calculator;
    calculator = new ru.autosome.macroape.calculation.mono.ScanCollection(pwmCollection, queryPWM);
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

  OutputInformation report_table(List<? extends ResultInfo> data) {
    OutputInformation result = report_table_layout();
    result.data = data;
    return result;
  }

  <R extends ResultInfo> OutputInformation report_table(R[] data) {
    List<R> data_list = new ArrayList<R>(data.length);
    Collections.addAll(data_list, data);
    return report_table(data_list);
  }

  List<? extends ResultInfo> process() throws Exception {
    List<ScanCollectionSimilarityInfo> infos;
    infos = calculator().similarityInfos();
    return infos;
  }

  public static void main(String[] args) {
    try {
      ScanCollection calculation = ScanCollection.from_arglist(args);
      List<? extends ResultInfo> infos = calculation.process();
      System.out.println(calculation.report_table(infos).report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);

    }
  }
}
