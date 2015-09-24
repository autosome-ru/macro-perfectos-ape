package ru.autosome.macroape.cli.generalized;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.macroape.calculation.generalized.CompareModels;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;

import java.util.ArrayList;
import java.util.List;

public abstract class EvalSimilarity<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType> & Alignable<ModelType>,
                                     BackgroundType extends GeneralizedBackgroundModel> {
  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();
  protected String DOC_additional_options() {
    return "";
  }

  protected String documentString() {
   return "Command-line format:\n" +
    DOC_run_string() + " <1st matrix pat-file> <2nd matrix pat-file> [options]\n" +
    "\n" +
    "Options:\n" +
    "  [--pvalue <P-value>] or [-p]\n" +
    "  [--discretization <discretization level>] or [-d]\n" +
    "  [--[first-|second-]pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--[first-|second-]ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--[first-|second-]effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
    "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
    "  [--first-threshold <threshold for the first matrix>]\n" +
    "  [--second-threshold <threshold for the second matrix>]\n" +
    "  [--position <shift>,<direct|revcomp>] - specify relative alignment to test. By default every alignment tested (example: --position -3,revcomp). Comma not allowed.\n" +
    "  [--[first-|second-]transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
    DOC_additional_options() +
    "\n" +
    "Examples:\n" +
    "  "+ DOC_run_string() + " motifs/KLF4_f2.pat motifs/SP1_f1.pat -p 0.0005 -d 100 -b 0.3,0.2,0.2,0.3\n";
  }

  protected BackgroundType firstBackground, secondBackground;
  protected Discretizer discretizer;
  protected double pvalue;
  protected BoundaryType pvalueBoundary;
  protected String firstPMFilename, secondPMFilename;
  protected DataModel dataModelFirst, dataModelSecond;

  protected PseudocountCalculator pseudocountFirst, pseudocountSecond;
  protected Double effectiveCountFirst, effectiveCountSecond;

  protected Double predefinedFirstThreshold, predefinedSecondThreshold;
  protected ModelType firstPWM, secondPWM;

  protected Double cacheFirstThreshold, cacheSecondThreshold;

  protected Position alignment; // if null, all orientations are shifts and orientations are tested
  protected boolean transposeFirst, transposeSecond;

  protected abstract BackgroundType extract_background(String str);
  protected abstract ModelType loadFirstPWM(String filename);
  protected abstract ModelType loadSecondPWM(String filename);
  protected abstract CompareModels<ModelType, BackgroundType> calculator();

  protected void setup_from_arglist(ArrayList<String> argv) {
    extract_first_pm_filename(argv);
    extract_second_pm_filename(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    firstPWM = loadFirstPWM(firstPMFilename);
    secondPWM = loadSecondPWM(secondPMFilename);
  }

  protected void extract_first_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    firstPMFilename = argv.remove(0);
  }

  protected void extract_second_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    secondPMFilename = argv.remove(0);
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }


  protected abstract void initialize_default_background();

  protected void initialize_defaults() {
    initialize_default_background();
    dataModelFirst = DataModel.PWM;
    dataModelSecond = DataModel.PWM;
    effectiveCountFirst = 100.0;
    effectiveCountSecond = 100.0;
    pseudocountFirst = PseudocountCalculator.logPseudocount;
    pseudocountSecond = PseudocountCalculator.logPseudocount;
    pvalue = 0.0005;
    discretizer = new Discretizer(10.0);
    transposeFirst = false;
    transposeSecond = false;

    pvalueBoundary = BoundaryType.UPPER;
    alignment = null;
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      BackgroundType background = extract_background(argv.remove(0));
      firstBackground = background;
      secondBackground = background;
    } else if (opt.equals("-p") || opt.equals("--pvalue")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--first-background")) {
      firstBackground = extract_background(argv.remove(0));
    } else if (opt.equals("--second-background")) {
      secondBackground = extract_background(argv.remove(0));
    } else if (opt.equals("-d") || opt.equals("--discretization")) {
      discretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundary = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--pcm")) {
      dataModelFirst = DataModel.PCM;
      dataModelSecond = DataModel.PCM;
    } else if (opt.equals("--first-pcm")) {
      dataModelFirst = DataModel.PCM;
    } else if (opt.equals("--second-pcm")) {
      dataModelSecond = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModelFirst = DataModel.PPM;
      dataModelSecond = DataModel.PPM;
    } else if (opt.equals("--first-ppm") || opt.equals("--first-pfm")) {
      dataModelFirst = DataModel.PPM;
    } else if (opt.equals("--second-ppm") || opt.equals("--second-pfm")) {
      dataModelSecond = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      Double effectiveCount = Double.valueOf(argv.remove(0));
      effectiveCountFirst = effectiveCount;
      effectiveCountSecond = effectiveCount;
    } else if (opt.equals("--first-effective-count")) {
      effectiveCountFirst = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--second-effective-count")) {
      effectiveCountSecond = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      PseudocountCalculator pseudocount = PseudocountCalculator.fromString(argv.remove(0));
      pseudocountFirst = pseudocount;
      pseudocountSecond = pseudocount;
    } else if (opt.equals("--first-pseudocount")) {
      pseudocountFirst = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--second-pseudocount")) {
      pseudocountSecond = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--first-threshold")) {
      predefinedFirstThreshold = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--second-threshold")) {
      predefinedSecondThreshold = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--position")) {
      String pos_string = argv.remove(0);
      String[] pos_tokens = pos_string.split(",");
      Integer shift = Integer.valueOf(pos_tokens[0]);
      String orientation = pos_tokens[1];
      alignment = new Position(shift, orientation);
    } else if (opt.equals("--transpose")) {
      transposeFirst = true;
      transposeSecond = true;
    } else if (opt.equals("--first-transpose")) {
      transposeFirst = true;
    } else if (opt.equals("--second-transpose")) {
      transposeSecond = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();

    infos.add_parameter("V", "discretization", discretizer);
    if (predefinedFirstThreshold == null || predefinedSecondThreshold == null) {
      infos.add_parameter("P", "requested P-value", pvalue);
    }
    if (predefinedFirstThreshold != null) {
      infos.add_parameter("T1", "threshold for the 1st matrix", predefinedFirstThreshold);
    }
    if (predefinedSecondThreshold != null) {
      infos.add_parameter("T2", "threshold for the 2nd matrix", predefinedSecondThreshold);
    }
    infos.add_parameter("PB", "P-value boundary", pvalueBoundary);
    if (firstBackground.equals(secondBackground)) {
      infos.background_parameter("B", "background", firstBackground);
    } else {
      infos.background_parameter("B1", "background for the 1st model", firstBackground);
      infos.background_parameter("B2", "background for the 2nd model", secondBackground);
    }

    return infos;
  }

  protected OutputInformation report_table(CompareModelsCountsGiven.SimilarityInfo info) {
    OutputInformation infos = report_table_layout();
    infos.add_resulting_value("S", "similarity", info.similarity());
    infos.add_resulting_value("D", "distance (1-similarity)", info.distance());
    infos.add_resulting_value("L", "length of the alignment", info.alignment.length());
    infos.add_resulting_value("SH", "shift of the 2nd PWM relative to the 1st", info.alignment.shift());
    infos.add_resulting_value("OR", "orientation of the 2nd PWM relative to the 1st", info.alignment.orientation());
    infos.add_resulting_value("A1", "aligned 1st matrix", info.alignment.first_model_alignment());
    infos.add_resulting_value("A2", "aligned 2nd matrix", info.alignment.second_model_alignment());
    infos.add_resulting_value("W", "number of words recognized by both models (model = PWM + threshold)", info.recognizedByBoth );
    infos.add_resulting_value("W1", "number of words and recognized by the first model", info.recognizedByFirst );
    infos.add_resulting_value("P1", "P-value for the 1st matrix", info.realPvalueFirst(firstBackground));
    if (predefinedFirstThreshold == null) {
      infos.add_resulting_value("T1", "threshold for the 1st matrix", thresholdFirst() );
    }
    infos.add_resulting_value("W2", "number of words recognized by the 2nd model", info.recognizedBySecond );
    infos.add_resulting_value("P2", "P-value for the 2nd matrix", info.realPvalueSecond(secondBackground));
    if (predefinedSecondThreshold == null) {
      infos.add_resulting_value("T2", "threshold for the 2nd matrix", thresholdSecond() );
    }
    return infos;
  }

  protected CompareModelsCountsGiven.SimilarityInfo<ModelType> results() {
    if (alignment == null) {
      return calculator().jaccard(thresholdFirst(), thresholdSecond());
    } else {
      return calculator().jaccardAtPosition(thresholdFirst(), thresholdSecond(), alignment);
    }
  }

  protected OutputInformation report_table() throws Exception {
    return report_table(results());
  }

  protected double thresholdFirst() {
    if (cacheFirstThreshold == null) {
      if (predefinedFirstThreshold != null) {
        cacheFirstThreshold = predefinedFirstThreshold;
      } else {
        CanFindThreshold pvalue_calculator = new FindThresholdAPE<ModelType, BackgroundType>(firstPWM, firstBackground, discretizer);
        cacheFirstThreshold = pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundary).threshold;
      }
    }
    return cacheFirstThreshold;
  }

  protected double thresholdSecond() {
    if (cacheSecondThreshold == null) {
      if (predefinedSecondThreshold != null) {
        cacheSecondThreshold = predefinedSecondThreshold;
      } else {
        CanFindThreshold pvalue_calculator = new FindThresholdAPE<ModelType, BackgroundType>(secondPWM, secondBackground, discretizer);
        cacheSecondThreshold = pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundary).threshold;
      }
    }
    return cacheSecondThreshold;
  }
}
