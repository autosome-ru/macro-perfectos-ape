package ru.autosome.macroape.cli.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearchBuilder;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FindThresholdBsearchBuilder;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.types.DataModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ScanCollection<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType> &Alignable<ModelType>,
                                     BackgroundType extends GeneralizedBackgroundModel> {

  public class ThresholdEvaluator {
    public final ModelType pwm;
    public final CanFindThreshold roughThresholdCalculator;
    public final CanFindThreshold preciseThresholdCalculator;

    public final CanFindPvalue roughPvalueCalculator;
    public final CanFindPvalue precisePvalueCalculator;

    public ThresholdEvaluator(ModelType pwm,
                              CanFindThreshold roughThresholdCalculator, CanFindThreshold preciseThresholdCalculator,
                              CanFindPvalue roughPvalueCalculator, CanFindPvalue precisePvalueCalculator) {
      this.pwm = pwm;
      this.roughThresholdCalculator = roughThresholdCalculator;
      this.preciseThresholdCalculator = preciseThresholdCalculator;
      this.roughPvalueCalculator = roughPvalueCalculator;
      this.precisePvalueCalculator = precisePvalueCalculator;
    }
  }

  protected DataModel collectionDataModel;
  protected Double collectionEffectiveCount;
  protected PseudocountCalculator collectionPseudocount;
  protected DataModel queryDataModel;
  protected Double queryEffectiveCount;
  protected PseudocountCalculator queryPseudocount;

  protected BackgroundType queryBackground, collectionBackground;
  protected Discretizer roughDiscretizer, preciseDiscretizer;
  protected Integer maxHashSize;
  protected Integer maxPairHashSize;
  protected double pvalue;
  protected Double queryPredefinedThreshold;
  protected Double similarityCutoff;
  protected Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  protected BoundaryType pvalueBoundaryType;
  protected String queryPMFilename;
  protected File pathToCollectionOfPWMs;
  protected File thresholds_folder;
  protected ModelType queryPWM;
  protected List<ThresholdEvaluator> pwmCollection;
  protected boolean queryTranspose, collectionTranspose;

  abstract protected String DOC_background_option();
  abstract protected String DOC_run_string();
  protected String documentString() {
    return "Command-line format:\n" +
     DOC_run_string() + " <query PWM file> <folder with PWMs> [options]\n" +
     "\n" +
     "Options:\n" +
     "  [--pvalue <P-value>] or [-p]\n" +
     "  [--similarity-cutoff <similarity cutoff>] or [-c] minimal similarity to be included in output, '--similarity-cutoff 0.05' by default, [--all] to print all results\n" +
     "  [--precise [<level>]] minimal similarity to check on the second pass in precise mode, off by default, '--precise 0.01' if level is not set\n" +
     "  [--rough-discretization <discretization level>] or [-d]\n" +
     "  [--precise-discretization <discretization level>]\n" +
     "  [--[query-|collection-]pcm] - treat the query input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
     "  [--[query-|collection-]ppm] or [--pfm] - treat the query input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
     "  [--[query-|collection-]effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
     "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
     "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
     "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
     "                         Attention! Don't use threshold lists calculated for a different discretization (or background)!\n" +
     "  [--[query-|collection-]transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
     DOC_additional_options() +
     "\n" +
     "Output format:\n" +
     "           <name> <jaccard index> <shift> <overlap> <orientation> ['*' in case that result was calculated on the second pass (in precise mode), '.' otherwise]\n" +
     "              Attention! Name can contain whitespace characters.\n" +
     "              Attention! The shift and orientation are reported for the collection matrix relative to the query matrix.\n" +
     "\n" +
     "Examples:\n" +
     "  " + DOC_run_string() + " ./query_motif.pwm ./hocomoco/ --precalc ./hocomoco_thresholds\n" +
     "  " + DOC_run_string() + " ./query_motif.pcm ./hocomoco/ --pcm -p 0.0005 --precise 0.03\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

  protected void extract_query_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    queryPMFilename = argv.remove(0);
  }

  protected void extract_path_to_collection_of_pwms(ArrayList<String> argv) {
    try {
      pathToCollectionOfPWMs = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  protected abstract BackgroundType extractBackground(String str);
  protected abstract void initialize_default_background();
  protected void initialize_defaults() {
    initialize_default_background();
    roughDiscretizer = new Discretizer(1.0);
    preciseDiscretizer = new Discretizer(10.0);
    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    queryDataModel = DataModel.PWM;
    collectionDataModel = DataModel.PWM;
    queryEffectiveCount = 100.0;
    queryPseudocount = PseudocountCalculator.logPseudocount;
    collectionEffectiveCount = 100.0;
    collectionPseudocount = PseudocountCalculator.logPseudocount;
    thresholds_folder = null;
    pvalueBoundaryType = BoundaryType.UPPER;
    pvalue = 0.0005;
    similarityCutoff = 0.05;
    preciseRecalculationCutoff = null;
    queryTranspose = false;
    collectionTranspose = false;
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      BackgroundType background = extractBackground(argv.remove(0));
      queryBackground = background;
      collectionBackground = background;
    } else if (opt.equals("--query-background")) {
      queryBackground = extractBackground(argv.remove(0));
    } else if (opt.equals("--collection-background")) {
      collectionBackground = extractBackground(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      maxHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--max-2d-hash-size")) {
      maxPairHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--rough-discretization") || opt.equals("-d") || opt.equals("--discretization")) {
      roughDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundaryType = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--pcm")) {
      queryDataModel = DataModel.PCM;
      collectionDataModel = DataModel.PCM;
    } else if (opt.equals("--query-pcm")) {
      queryDataModel = DataModel.PCM;
    } else if (opt.equals("--collection-pcm")) {
      collectionDataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      queryDataModel = DataModel.PPM;
      collectionDataModel = DataModel.PPM;
    } else if (opt.equals("--query-ppm") || opt.equals("--query-pfm")) {
      queryDataModel = DataModel.PPM;
    } else if (opt.equals("--collection-ppm") || opt.equals("--collection-pfm")) {
      collectionDataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      Double effectiveCount = Double.valueOf(argv.remove(0));
      queryEffectiveCount = effectiveCount;
      collectionEffectiveCount = effectiveCount;
    } else if (opt.equals("--query-effective-count")) {
      queryEffectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--collection-effective-count")) {
      collectionEffectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      PseudocountCalculator pseudocount = PseudocountCalculator.fromString(argv.remove(0));
      queryPseudocount = pseudocount;
      collectionPseudocount = pseudocount;
    } else if (opt.equals("--query-pseudocount")) {
      queryPseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--collection-pseudocount")) {
      collectionPseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if(opt.equals("-p") || opt.equals("--pvalue")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--predefined-threshold")) {
      queryPredefinedThreshold = Double.valueOf(argv.remove(0));
    } else if(opt.equals("-c") || opt.equals("--similarity-cutoff")) {
      similarityCutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--all")) {
      similarityCutoff = 0.0;
    } else if(opt.equals("--precise")) {
      if (!argv.isEmpty() && InputExtensions.isDouble(argv.get(0))) {
        preciseRecalculationCutoff = Double.valueOf(argv.remove(0));
      } else {
        preciseRecalculationCutoff = 0.01;
      }
    } else if(opt.equals("--transpose")) {
      queryTranspose = true;
      collectionTranspose = true;
    } else if(opt.equals("--query-transpose")) {
      queryTranspose = true;
    } else if(opt.equals("--collection-transpose")) {
      collectionTranspose = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }

  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.add_parameter("MS", "minimal similarity to output", similarityCutoff);
    infos.add_parameter("P", "P-value", pvalue);
    infos.add_parameter("PB", "P-value boundary", pvalueBoundaryType);
    if (preciseRecalculationCutoff != null) {
      infos.add_parameter("VR", "discretization value, rough", roughDiscretizer);
      infos.add_parameter("VP", "discretization value, precise", preciseDiscretizer);
      infos.add_parameter("MP", "minimal similarity for the 2nd pass in \'precise\' mode", preciseRecalculationCutoff);
    } else {
      infos.add_parameter("V", "discretization value", roughDiscretizer);
    }
    infos.background_parameter("BQ", "background for query matrix", queryBackground);
    infos.background_parameter("BC", "background for collection", collectionBackground);

    infos.add_table_parameter_without_description("motif", "name");
    infos.add_table_parameter_without_description("similarity", "similarity");
    infos.add_table_parameter_without_description("shift", "shift");
    infos.add_table_parameter_without_description("overlap", "overlap");
    infos.add_table_parameter_without_description("orientation", "orientation");
    if (preciseRecalculationCutoff != null) {
      infos.add_table_parameter_without_description("precise mode", "precision_mode", new OutputInformation.Callback<ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo>(){
        @Override
        public String run(ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo cell) {
          return cell.precise ? "*" : ".";
        }
      });
    }
    return infos;
  }

  protected OutputInformation report_table(List<? extends ResultInfo> data) {
    OutputInformation result = report_table_layout();
    Collections.sort(data, new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo s1, s2;
        s1 = (ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo)o1;
        s2 = (ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo)o2;
        return s1.similarity().compareTo(s2.similarity());
      }
    });
    result.data = data;
    return result;
  }

  protected <R extends ResultInfo> OutputInformation report_table(R[] data) {
    List<R> data_list = new ArrayList<R>(data.length);
    Collections.addAll(data_list, data);
    return report_table(data_list);
  }

  protected List<? extends ResultInfo> process() throws Exception {
    List<ru.autosome.macroape.calculation.generalized.ScanCollection<ModelType, BackgroundType>.SimilarityInfo> infos;
    infos = calculator().similarityInfos();
    return infos;
  }



  // TODO: Refactor usage of one-stage and two-stage search
  protected List<ThresholdEvaluator> load_collection_of_pwms() {
    List<Named<ModelType>> pwmList = loadMotifCollection();
    List<ThresholdEvaluator> result;
    result = new ArrayList<ThresholdEvaluator>();
    for (Named<ModelType> namedModel: pwmList) {
      ModelType pwm = namedModel.getObject();
      if (thresholds_folder == null) {
        result.add(new ThresholdEvaluator( pwm,
                                           new FindThresholdAPE<ModelType, BackgroundType>(pwm, collectionBackground, roughDiscretizer, maxHashSize),
                                           new FindThresholdAPE<ModelType, BackgroundType>(pwm, collectionBackground, preciseDiscretizer, maxHashSize),
                                           new FindPvalueAPE<ModelType, BackgroundType>(pwm, collectionBackground, roughDiscretizer, maxHashSize),
                                           new FindPvalueAPE<ModelType, BackgroundType>(pwm, collectionBackground, preciseDiscretizer, maxHashSize)));
      } else {
        result.add(new ThresholdEvaluator( pwm,
                                           new FindThresholdBsearchBuilder(thresholds_folder).thresholdCalculator(namedModel.getName()),
                                           null,
                                           new FindPvalueBsearchBuilder(thresholds_folder).pvalueCalculator(namedModel.getName()),
                                           null));
      }
    }
    return result;
  }

  protected void setup_from_arglist(ArrayList<String> argv) {
    extract_query_pm_filename(argv);
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }

    queryPWM = loadQueryMotif();
    pwmCollection = load_collection_of_pwms();
  }

  protected abstract ru.autosome.macroape.calculation.generalized.ScanCollection<ModelType,BackgroundType> calculator();
  protected abstract List<Named<ModelType>> loadMotifCollection();
  protected abstract ModelType loadQueryMotif();
}
