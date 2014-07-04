package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.Background;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.ComparePWM;
import ru.autosome.perfectosape.calculations.ComparePWMCountsGiven;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.importers.MotifCollectionImporter;
import ru.autosome.perfectosape.importers.PWMImporter;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.PWM;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectDistanceMatrix {
  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.perfectosape.cli.CollectDistanceMatrix <folder with PWMs> [options]\n" +
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
    "  java ru.autosome.perfectosape.cli.CollectDistanceMatrix ./motifs/ -d 10\n";


  private Discretizer roughDiscretizer;
  private Discretizer preciseDiscretizer;
  private File pathToCollectionOfPWMs;
  private BackgroundModel background;
  private DataModel dataModel;
  private Integer maxHashSize;
  private Integer maxPairHashSize;
  private double effectiveCount;
  private BoundaryType pvalueBoundary;
  private double pvalue;
  private Double preciseRecalculationCutoff; // null means that no recalculation will be performed

  private int numOfThreads;
  private int numThread;
  private List<PWM> pwmCollection;

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
    roughDiscretizer = new Discretizer(1.0);
    preciseDiscretizer = new Discretizer(10.0);

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
      roughDiscretizer = new Discretizer(Double.valueOf(argv.remove(0)));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretizer = new Discretizer(Double.valueOf(argv.remove(0)));
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
    ru.autosome.perfectosape.cli.Helper.print_help_if_requested(argv, DOC);
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
      CanFindThreshold roughThresholdCalculator = new FindThresholdAPE<PWM, BackgroundModel>(pwm, background, roughDiscretizer, maxHashSize);
      CanFindThreshold.ThresholdInfo roughThresholdInfo = roughThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double roughThreshold = roughThresholdInfo.threshold;
      double roughCount = roughThresholdInfo.numberOfRecognizedWords(background, pwm.length());

      CanFindThreshold preciseThresholdCalculator = new FindThresholdAPE<PWM, BackgroundModel>(pwm, background, preciseDiscretizer, maxHashSize);
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
    ComparePWM.SimilarityInfo info;

    ComparePWMCountsGiven roughCalc;
    roughCalc = new ComparePWMCountsGiven(new CountingPWM(first.pwm, background, maxHashSize).discrete(roughDiscretizer),
                                          new CountingPWM(second.pwm, background, maxHashSize).discrete(roughDiscretizer),
                                          maxPairHashSize);

    Discretizer roughDiscretizer = this.roughDiscretizer;
    Discretizer preciseDiscretizer = this.preciseDiscretizer;

    info = roughCalc.jaccard( roughDiscretizer.upscale(first.roughThreshold),
                              roughDiscretizer.upscale(second.roughThreshold),
                              first.roughCount, second.roughCount);
    if (preciseRecalculationCutoff != null && info.similarity() > preciseRecalculationCutoff) {
      ComparePWMCountsGiven preciseCalc;
      preciseCalc = new ComparePWMCountsGiven(new CountingPWM(first.pwm, background, maxHashSize).discrete(this.preciseDiscretizer),
                                              new CountingPWM(second.pwm, background, maxHashSize).discrete(this.preciseDiscretizer),
                                              maxPairHashSize);
      info = preciseCalc.jaccard( preciseDiscretizer.upscale(first.preciseThreshold),
                                  preciseDiscretizer.upscale(second.preciseThreshold),
                                  first.preciseCount, second.preciseCount);
    }
    return info.distance();
  }

  private final static Comparator<PWMWithThreshold> nameComparator = new Comparator<PWMWithThreshold>() {
    @Override
    public int compare(PWMWithThreshold o1, PWMWithThreshold o2) {
      return o1.pwm.name.compareTo(o2.pwm.name);
    }
  };

  void process() throws HashOverflowException {
    int taskNum = 0;
    List<PWMWithThreshold> thresholds = collectThreshold();
    Collections.sort(thresholds, nameComparator);

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
