package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.Background;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.SNPScan;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueBsearch;
import ru.autosome.perfectosape.importers.InputExtensions;
import ru.autosome.perfectosape.importers.PWMCollectionImporter;
import ru.autosome.perfectosape.importers.PWMImporter;
import ru.autosome.perfectosape.motifModels.DataModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiSNPScan {
  private BackgroundModel background;
  private Double discretization;
  private Integer max_hash_size;

  private File path_to_collection_of_pwms;
  private File path_to_file_w_snps;

  private DataModel dataModel;
  private double effectiveCount;
  private File thresholds_folder;

  private List<String> snp_list;
  private MotifEvaluatorCollection motifEvaluatorCollection;

  private double max_pvalue_cutoff;
  private double min_fold_change_cutoff;



  // Split by spaces and return last part
  // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  private static String last_part_of_string(String s) {
    String[] string_parts = s.replaceAll("\\s+", " ").split(" ");
    String result = string_parts[string_parts.length - 1];
    if (result.matches("[ACGTacgt]+(/[ACGTacgt]+)+") || result.matches("[ACGTacgt]+\\[(/?[ACGTacgt]+)+\\][ACGTacgt]+")) {
      return result;
    } else {
      return string_parts[string_parts.length - 3] + "[" + string_parts[string_parts.length - 2].replaceAll("\\[|\\]", "") + "]" + string_parts[string_parts.length - 1];
    }
  }

  // Output: "rs9929218"
  private static String first_part_of_string(String s) {
    return s.replaceAll("\\s+", " ").split(" ")[0];
  }

  private void load_collection_of_pwms() throws FileNotFoundException {
    PWMImporter pwmImporter = new PWMImporter(background, dataModel, effectiveCount);

    CanFindPvalue.Builder builder;
    if (thresholds_folder == null) {
      builder = new FindPvalueAPE.Builder(discretization, background, max_hash_size);
    } else {
      builder = new FindPvalueBsearch.Builder(thresholds_folder);
    }
    PWMCollectionImporter importer = new PWMCollectionImporter(pwmImporter, builder);
    motifEvaluatorCollection = importer.loadPWMCollection(path_to_collection_of_pwms);
  }

  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.perfectosape.cli.MultiSNPScan <folder with pwms> <file with SNPs> [options]\n" +
    "\n" +
    "Options:\n" +
    "  [--pvalue-cutoff <maximal pvalue to be considered>] or [-P] - drop results having both allele-variant pvalues greater than given\n" +
    "                                                       (default: 0.0005)\n" +
    "  [--fold-change-cutoff <minmal fold change to be considered>] or [-F] - drop results having fold change (both 1st pvalue to 2nd, 2nd to 1st)\n" +
    "                                                                 less than given (default: 5)\n" +
    "        In order to get all fold changes - set both pvalue-cutoff and fold-change-cutoff to 1.0.\n" +
    "  [-d <discretization level>]\n" +
    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
    "\n" +
    "Example:\n" +
    "  java ru.autosome.perfectosape.cli.MultiSNPScan ./hocomoco/pwms/ snp.txt --precalc ./collection_thresholds\n" +
    "  java ru.autosome.perfectosape.cli.MultiSNPScan ./hocomoco/pcms/ snp.txt --pcm -d 10\n";

  void extract_path_to_collection_of_pwms(ArrayList<String> argv) {
    try {
      path_to_collection_of_pwms = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  void extract_path_to_file_w_snps(ArrayList<String> argv) {
    try {
      path_to_file_w_snps = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify file with SNPs", e);
    }
  }

  private void initialize_defaults() {
    background = new WordwiseBackground();
    discretization = 100.0;
    max_hash_size = 10000000;

    dataModel = DataModel.PWM;
    effectiveCount = 100;
    thresholds_folder = null;
    max_pvalue_cutoff = 0.0005;
    min_fold_change_cutoff = 5.0;
  }

  private MultiSNPScan() {
    initialize_defaults();
  }

  private static MultiSNPScan from_arglist(ArrayList<String> argv) throws FileNotFoundException {
    MultiSNPScan result = new MultiSNPScan();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static MultiSNPScan from_arglist(String[] args) throws FileNotFoundException {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  void setup_from_arglist(ArrayList<String> argv) throws FileNotFoundException {
    extract_path_to_collection_of_pwms(argv);
    extract_path_to_file_w_snps(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }
    load_collection_of_pwms();
    load_snp_list();
  }

  private void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      background = Background.fromString(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("-d")) {
      discretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      dataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if(opt.equals("--pvalue-cutoff") || opt.equals("-P")) {
      max_pvalue_cutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--fold-change-cutoff") || opt.equals("-F")) {
      min_fold_change_cutoff = Double.valueOf(argv.remove(0));
      if (min_fold_change_cutoff < 1.0) {
        min_fold_change_cutoff = 1.0 / min_fold_change_cutoff;
      }
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  private void load_snp_list() {
    try {
      InputStream reader = new FileInputStream(path_to_file_w_snps);
      snp_list = InputExtensions.filter_empty_strings(InputExtensions.readLinesFromInputStream(reader));
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to load pack of SNPs", e);
    }
  }

  private void process_snp(String snp_input) throws HashOverflowException {
    String snp_name = first_part_of_string(snp_input);
    SequenceWithSNP seq_w_snp = SequenceWithSNP.fromString(last_part_of_string(snp_input));

    for (MotifEvaluatorCollection.MotifEvaluator motifEvaluator : motifEvaluatorCollection) {
      SNPScan.RegionAffinityInfos result = new SNPScan(motifEvaluator.pwm, seq_w_snp, motifEvaluator.pvalueCalculator).affinityInfos();
      boolean pvalueSignificant = (result.getInfo_1().getPvalue() <= max_pvalue_cutoff ||
                                    result.getInfo_2().getPvalue() <= max_pvalue_cutoff);
      boolean foldChangeSignificant = (result.foldChange() >= min_fold_change_cutoff ||
                                        result.foldChange() <= 1.0/min_fold_change_cutoff);
      if (pvalueSignificant && foldChangeSignificant) {
        System.out.println(snp_name + "\t" + motifEvaluator.pwm.name + "\t" + result.toString());
      }
    }
  }

  void process() throws HashOverflowException {
    System.out.println("# SNP name\tmotif\tposition 1\torientation 1\tword 1\tposition 2\torientation 2\tword 2\tallele 1/allele 2\tP-value 1\tP-value 2\tFold change");
    for (String snp_input : snp_list) {
      process_snp(snp_input);
    }

  }

  public static void main(String[] args) {
    try {
      MultiSNPScan calculation = MultiSNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);

    }
  }

}
