package ru.autosome.macroape;

import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.importer.MotifCollectionImporter;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.generalized.SimilarityInfo;
import ru.autosome.macroape.calculation.mono.ComparePWMCountsGiven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectDistanceMatrix {
  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.macroape.CollectDistanceMatrix <folder with PWMs> [options]\n" +
    "\n" +
    "Options:\n" +
    "  [--rough-discretization <discretization level>] or [-d]\n" +
    "  [--precise-discretization <discretization level>]\n" +
    "  [--precise [<level>]] minimal similarity to check on the second pass in precise mode, off by default, '--precise 0.01' if level is not set\n" +
    "  [-p <P-value>]\n" +
    "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "  [--parallelize <num of threads> <thread number>] - run only one task per numOfThreads (those equal to thread number modulo numOfThreads)\n" +
    "\n" +
    "Examples:\n" +
    "  java ru.autosome.macroape.CollectDistanceMatrix ./motifs/ -d 10\n";


  Double roughDiscretization, preciseDiscretization;
  File pathToCollectionOfPWMs;
  BackgroundModel background;
  DataModel dataModel;
  Integer maxHashSize, maxPairHashSize;
  double effectiveCount;
  BoundaryType pvalueBoundary;
  double pvalue;
  Double preciseRecalculationCutoff; // null means that no recalculation will be performed

  int numOfThreads, numThread;
  List<PWM> pwmCollection;

  static class PWMWithThreshold {
    final PWM pwm;
    final double roughThreshold;
    final double roughCount;
    final double preciseThreshold;
    final double preciseCount;
    PWMWithThreshold(PWM pwm,
                     double roughThreshold, double roughCount,
                     double preciseThreshold, double preciseCount) {
      this.pwm = pwm;
      this.roughThreshold = roughThreshold;
      this.roughCount = roughCount;
      this.preciseThreshold = preciseThreshold;
      this.preciseCount = preciseCount;
    }
  }

  private void initialize_defaults() {
    roughDiscretization = 1.0;
    preciseDiscretization = 10.0;

    background = new WordwiseBackground();
    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    dataModel = DataModel.PWM;
    effectiveCount = 100;
    pvalue = 0.0005;
    pvalueBoundary = BoundaryType.UPPER;
    preciseRecalculationCutoff = null;

    numOfThreads = 1;
    numThread = 0;

    pathToCollectionOfPWMs = null;
    pwmCollection = null;
  }

  void extract_path_to_collection_of_pwms(List<String> argv) {
    try {
      pathToCollectionOfPWMs = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  private void extract_option(List<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      background = Background.fromString(argv.remove(0));
    } else if(opt.equals("-p")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      maxHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--max-2d-hash-size")) {
      maxPairHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--rough-discretization") || opt.equals("-d")) {
      roughDiscretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      dataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundary = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--precise")) {
      preciseRecalculationCutoff = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--parallelize")) {
      numOfThreads = Integer.valueOf(argv.remove(0));
      numThread = Integer.valueOf(argv.remove(0));
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  void setup_from_arglist(List<String> argv) {
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    PWMImporter importer = new PWMImporter(background, dataModel, effectiveCount);
    pwmCollection = new MotifCollectionImporter<PWM>(importer).loadPWMCollection(pathToCollectionOfPWMs);
  }

  private CollectDistanceMatrix() {
    initialize_defaults();
  }

  private static CollectDistanceMatrix from_arglist(List<String> argv) {
    CollectDistanceMatrix result = new CollectDistanceMatrix();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static CollectDistanceMatrix from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  List<PWMWithThreshold> collectThreshold() throws HashOverflowException {
    List<PWMWithThreshold> result = new ArrayList<PWMWithThreshold>();
    for (PWM pwm: pwmCollection) {
      CanFindThreshold roughThresholdCalculator = new FindThresholdAPE<PWM, BackgroundModel>(pwm, background, roughDiscretization, maxHashSize);
      CanFindThreshold.ThresholdInfo roughThresholdInfo = roughThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double roughThreshold = roughThresholdInfo.threshold;
      double roughCount = roughThresholdInfo.numberOfRecognizedWords(background, pwm.length());

      CanFindThreshold preciseThresholdCalculator = new FindThresholdAPE<PWM, BackgroundModel>(pwm, background, preciseDiscretization, maxHashSize);
      CanFindThreshold.ThresholdInfo preciseThresholdInfo = preciseThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double preciseThreshold = preciseThresholdInfo.threshold;
      double preciseCount = preciseThresholdInfo.numberOfRecognizedWords(background, pwm.length());

      result.add(new PWMWithThreshold(pwm,
                                      roughThreshold, roughCount,
                                      preciseThreshold, preciseCount));
    }
    return result;
  }

  double calculateDistance(PWMWithThreshold first, PWMWithThreshold second) throws HashOverflowException {
    ComparePWMCountsGiven calc;
    SimilarityInfo info;

    calc = new ComparePWMCountsGiven(first.pwm, second.pwm,
                                                background, background,
                                                roughDiscretization, maxPairHashSize);

    info = calc.jaccard(first.roughThreshold, second.roughThreshold,
                        first.roughCount, second.roughCount);
    if (preciseRecalculationCutoff != null && info.similarity() > preciseRecalculationCutoff) {
      calc = new ComparePWMCountsGiven(first.pwm, second.pwm,
                                                  background, background,
                                                  preciseDiscretization, maxPairHashSize);
      info = calc.jaccard(first.preciseThreshold, second.preciseThreshold,
                          first.preciseCount, second.preciseCount);
    }
    return info.distance();
  }

  public void process() throws HashOverflowException {
    int taskNum = 0;
    List<PWMWithThreshold> thresholds = collectThreshold();
    Collections.sort(thresholds, new Comparator<PWMWithThreshold>() {
      @Override
      public int compare(PWMWithThreshold o1, PWMWithThreshold o2) {
        return o1.pwm.name.compareTo(o2.pwm.name);
      }
    });

    System.out.print("Motif name"+ "\t");
    for(PWMWithThreshold second: thresholds) {
      System.out.print(second.pwm.name + "\t");
    }
    System.out.println();
    for(PWMWithThreshold first: thresholds) {
      System.out.print(first.pwm.name + "\t");
      for(PWMWithThreshold second: thresholds) {

        if (taskNum % numOfThreads == numThread % numOfThreads) {
          // so that numThread in range 0..(n-1) was equal to 1..n
          int cmp = first.pwm.name.compareTo(second.pwm.name);
          if (cmp == 0) {
            System.out.print("0.0\t");
          } else if (cmp < 0) {
            System.out.print("x\t");
          } else {
            double distance = calculateDistance(first, second);
            System.out.print(distance + "\t");
          }
        } else {
          System.out.print("x\t");
        }
        taskNum += 1;

      }
      System.out.println();
      System.err.print(".");
    }
  }

  public static void main(String[] args) {
    try {
      CollectDistanceMatrix cli = CollectDistanceMatrix.from_arglist(args);
      cli.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }
}
