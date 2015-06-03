package ru.autosome.commons;

import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer_two.ConversionAlgorithm;
import ru.autosome.commons.importer_two.ConversionAlgorithmType;
import ru.autosome.commons.importer_two.matrixLoaders.MatrixLoader;
import ru.autosome.commons.importer_two.matrixLoaders.NormalMatrixLoader;
import ru.autosome.commons.importer_two.matrixLoaders.TransposedMatrixLoader;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.motifModel.types.DataModelExpanded;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConvertMotif {

  public abstract static class Formatter<Model> {
    abstract String outputModel(Model file);
  }

  boolean is_dinucleotide_input;
  boolean is_transposed_input;
  DataModelExpanded dataModel_input;
  ConversionAlgorithm algorithm;
  Formatter formatter;

  List<File> filelist;

  // ToDo: make it possible
  MatrixLoader matrixLoader() {
    int alphabet_size = (!is_dinucleotide_input) ? 4 : 16;

    if (!is_transposed_input) {
      return new NormalMatrixLoader(alphabet_size);
    } else {
      return new TransposedMatrixLoader(alphabet_size);
    }
  }

  void run() {
    for (File file: filelist) {
      Named<double[][]> namedMatrix = matrixLoader().loadMatrix(file);
      Named model = dataModel_input.createModel(namedMatrix);
      Named convertedModel = algorithm.convertNamed(model);
      formatter.outputModel(convertedModel);
    }
  }

  protected void initialize_defaults() {
  }


  protected ConvertMotif() {
    initialize_defaults();
  }

  protected void setup_from_arglist(ArrayList<String> argv) {
    ConversionAlgorithmType algorithmType = ConversionAlgorithmType.valueOf(argv.remove(0));
    this.algorithm = algorithmType.getAlgorithm();
    this.algorithm.extractArguments(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }
    motif = loadMotif(pm_filename);
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      extract_background(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("-d") || opt.equals("--discretization")) {
      discretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      data_model = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      data_model = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effective_count = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      pseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if (opt.equals("--transpose")) {
      transpose = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected static ConvertMotif from_arglist(ArrayList<String> argv) {
    ConvertMotif result = new ConvertMotif();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static ConvertMotif from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      ConvertMotif cli = ConvertMotif.from_arglist(args);
      cli.run();
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ConvertMotif().documentString());
      System.exit(1);
    }
  }
}
