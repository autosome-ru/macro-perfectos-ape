package ru.autosome.perfectosape.cli.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearchBuilder;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.Named;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.perfectosape.calculation.SingleSNPScan;
import ru.autosome.perfectosape.model.SequenceWithSNP;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

abstract public class SNPScan<MotifType extends Named & ScoringModel & Discretable<MotifType> & ScoreDistribution<BackgroundType>,
                                   BackgroundType extends GeneralizedBackgroundModel> {
  public static class ThresholdEvaluator {
    public final ScoringModel pwm;
    public final CanFindPvalue pvalueCalculator;
    public final String name;

    public ThresholdEvaluator(ScoringModel pwm, CanFindPvalue pvalueCalculator, String name) {
      this.pwm = pwm;
      this.pvalueCalculator = pvalueCalculator;
      this.name = name;
    }
  }

  protected abstract void initialize_default_background();
  protected abstract void extract_background(String s);
  protected abstract List<MotifType> load_collection_of_pwms();

  protected void load_collection_of_pwms_with_evaluators() {
    List<MotifType> motifList = load_collection_of_pwms();

    pwmCollection = new ArrayList<ThresholdEvaluator>();
    for (MotifType motif: motifList) {
      CanFindPvalue pvalueCalculator;
      if (thresholds_folder == null) {
        pvalueCalculator = new FindPvalueAPE<MotifType, BackgroundType>(motif, background, discretizer, max_hash_size);
      } else {
        pvalueCalculator = new FindPvalueBsearchBuilder(thresholds_folder).pvalueCalculator(motif);
      }
      pwmCollection.add(new ThresholdEvaluator(motif, pvalueCalculator, motif.getName()));
    }
  }

  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();
  protected String documentString() {
    return "Command-line format:\n" +
    DOC_run_string() + " <folder with pwms> <file with SNPs> [options]\n" +
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
    "  [-b <background probabilities] " + DOC_background_option() + "\n" +
    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
    "  [--transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
     DOC_additional_options() +
    "\n" +
    "Examples:\n" +
    "  " + DOC_run_string() + " ./hocomoco/pwms/ snp.txt --precalc ./collection_thresholds\n" +
    "  " + DOC_run_string() + " ./hocomoco/pcms/ snp.txt --pcm -d 10\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

  protected Discretizer discretizer;
  protected Integer max_hash_size;

  protected File path_to_collection_of_pwms;
  protected File path_to_file_w_snps;

  protected DataModel dataModel;
  protected double effectiveCount;
  protected File thresholds_folder;

  protected List<String> snp_list;
  protected List<ThresholdEvaluator> pwmCollection;

  protected double max_pvalue_cutoff;
  protected double min_fold_change_cutoff;

  protected BackgroundType background;
  protected boolean transpose;

  // Split by spaces and return last part
  // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  protected static String last_part_of_string(String s) {
    String[] string_parts = s.trim().replaceAll("\\s+", " ").split(" ");
    return string_parts[string_parts.length - 1];
  }

  // Output: "rs9929218"
  protected static String first_part_of_string(String s) {
    return s.replaceAll("\\s+", " ").split(" ")[0];
  }

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

  protected void initialize_defaults() {
    initialize_default_background();
    discretizer = new Discretizer(100.0);
    max_hash_size = 10000000;

    dataModel = DataModel.PWM;
    effectiveCount = 100;
    thresholds_folder = null;
    max_pvalue_cutoff = 0.0005;
    min_fold_change_cutoff = 5.0;
    transpose = false;
  }

  protected SNPScan() {
    initialize_defaults();
  }

  protected void setup_from_arglist(ArrayList<String> argv) {
    extract_path_to_collection_of_pwms(argv);
    extract_path_to_file_w_snps(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }
    load_collection_of_pwms_with_evaluators();
    load_snp_list();
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
    } else if(opt.equals("--transpose")) {
      transpose = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }

  protected void load_snp_list() {
    try {
      InputStream reader = new FileInputStream(path_to_file_w_snps);
      snp_list = InputExtensions.filter_empty_strings(InputExtensions.readLinesFromInputStream(reader));
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to load pack of SNPs", e);
    }
  }

  protected void process_snp(String snp_input) throws HashOverflowException {
    String snp_name = first_part_of_string(snp_input);
    SequenceWithSNP seq_w_snp = SequenceWithSNP.fromString(last_part_of_string(snp_input));

    for (ThresholdEvaluator motifEvaluator: pwmCollection) {
      ScoringModel pwm = motifEvaluator.pwm;
      if (seq_w_snp.length() >= pwm.length()) {
        SingleSNPScan.RegionAffinityInfos result;
        result = new SingleSNPScan(pwm, seq_w_snp, motifEvaluator.pvalueCalculator).affinityInfos();
        boolean pvalueSignificant = (result.getInfo_1().getPvalue() <= max_pvalue_cutoff ||
                                      result.getInfo_2().getPvalue() <= max_pvalue_cutoff);
        boolean foldChangeSignificant = (result.foldChange() >= min_fold_change_cutoff ||
                                          result.foldChange() <= 1.0/min_fold_change_cutoff);
        if (pvalueSignificant && foldChangeSignificant) {
          System.out.println(snp_name + "\t" + motifEvaluator.name + "\t" + result.toString());
        }
      } else {
        System.err.println("Can't scan sequence '" + seq_w_snp + "' (length " + seq_w_snp.length() + ") with motif of length " + pwm.length());
      }
    }
  }

  public void process() throws HashOverflowException {
    System.out.println("# SNP name\tmotif\tposition 1\torientation 1\tword 1\tposition 2\torientation 2\tword 2\tallele 1/allele 2\tP-value 1\tP-value 2\tFold change");
    for (String snp_input : snp_list) {
      process_snp(snp_input);
    }
  }

}