package ru.autosome.perfectosape.cli.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearch;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.BackgroundAppliable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.scoringModel.SequenceScoringModel;
import ru.autosome.perfectosape.calculation.SingleSNVScan;
import ru.autosome.perfectosape.model.RegionAffinityInfos;
import ru.autosome.perfectosape.model.SequenceWithSNV;
import ru.autosome.perfectosape.model.ThresholdEvaluator;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceType;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceWithSNVType;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class SNPScan<SequenceType extends EncodedSequenceType & HasLength,
                              SequenceWithSNVType extends EncodedSequenceWithSNVType<SequenceType>,
                              MotifType extends HasLength & Discretable<MotifType> & ScoreDistribution<BackgroundType> & BackgroundAppliable<BackgroundType, ModelType>,
                              ModelType extends SequenceScoringModel<SequenceType>,
                              BackgroundType extends GeneralizedBackgroundModel> {

  protected abstract void initialize_default_background();
  protected abstract void extract_background(String s);
  protected abstract List<Named<MotifType>> load_collection_of_pwms();

  protected void load_collection_of_pwms_with_evaluators() {
    List<Named<MotifType>> motifList = load_collection_of_pwms();

    pwmCollection = new ArrayList<>();
    for (Named<MotifType> motif: motifList) {
      CanFindPvalue pvalueCalculator;
      if (thresholds_folder == null) {
        pvalueCalculator = new FindPvalueAPE<>(motif.getObject(), background, discretizer);
      } else {
        File thresholds_file = new File(thresholds_folder, motif.getName() + ".thr");
        try {
          pvalueCalculator = new FindPvalueBsearch(thresholds_file);
        } catch (FileNotFoundException e) {
          System.err.println("Thresholds file `" + thresholds_file + "` not found. Fallback to APE-calculation of P-value");
          pvalueCalculator = new FindPvalueAPE<>(motif.getObject(), background, discretizer);
        }
      }
      pwmCollection.add(new ThresholdEvaluator<>(motif.getObject().onBackground(background), pvalueCalculator, motif.getName()));
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
    "  [--fold-change-cutoff <minimal fold change to be considered>] or [-F] - drop results having fold change (both 1st pvalue to 2nd, 2nd to 1st)\n" +
    "                                                                 less than given (default: 4 in linear scale or 2 in log-scale)\n" +
    "        In order to get all fold changes - set both pvalue-cutoff and fold-change-cutoff to 1.0.\n" +
    "  [--discretization <discretization level>] or [-d]\n" +
    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
    "  [--transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
    "  [--expand-region <length>] - expand the region to scan for PWM hits by <length> positions\n" +
    "                               from each side allowing PWM to be located nearby but not necessarily\n"+
    "                               overlap the nucleotide substitution position.\n" +
    "  [--compact] - use compact output format.\n" +
    "  [--log-fold-change] - use logarithmic (log2) fold change scale (both in output and in cutoff setup).\n" +
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
  protected int expand_region_length;

  protected File path_to_collection_of_pwms;
  protected File path_to_file_w_snps;

  protected DataModel dataModel;
  protected double effectiveCount;
  protected PseudocountCalculator pseudocount;
  protected File thresholds_folder;

  protected List<ThresholdEvaluator<SequenceType, ModelType>> pwmCollection;

  protected double max_pvalue_cutoff;
  protected Double min_fold_change_cutoff; // It can be either linear of logarithmic cutoff (to be refactored later)

  protected BackgroundType background;
  protected boolean transpose;

  protected boolean shortFormat;
  protected boolean useLogFoldChange;

  void extract_path_to_collection_of_pwms(List<String> argv) {
    try {
      path_to_collection_of_pwms = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  void extract_path_to_file_w_snps(List<String> argv) {
    try {
      path_to_file_w_snps = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify file with SNPs", e);
    }
  }

  protected void initialize_defaults() {
    initialize_default_background();
    discretizer = new Discretizer(100.0);
    expand_region_length = 0;

    dataModel = DataModel.PWM;
    effectiveCount = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    thresholds_folder = null;
    max_pvalue_cutoff = 0.0005;
    min_fold_change_cutoff = null;
    transpose = false;
    shortFormat = false;
    useLogFoldChange = false;
  }

  protected SNPScan() {
    initialize_defaults();
  }

  protected void setup_from_arglist(String[] args) throws FileNotFoundException {
    ArrayList<String> argv = new ArrayList<>();
    Collections.addAll(argv, args);
    setup_from_arglist(argv);
  }

  protected void setup_from_arglist(List<String> argv) throws FileNotFoundException {
    Helper.print_help_if_requested(argv, documentString());
    extract_path_to_collection_of_pwms(argv);
    extract_path_to_file_w_snps(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }

    if (useLogFoldChange) {
      if (min_fold_change_cutoff == null) {
        min_fold_change_cutoff = 2.0; // Default
      } else {
        min_fold_change_cutoff = Math.abs(min_fold_change_cutoff);
      }
    } else {
      if (min_fold_change_cutoff == null) {
        min_fold_change_cutoff = 4.0; // Default
      } else {
        if (min_fold_change_cutoff < 1.0) {
          min_fold_change_cutoff = 1.0 / min_fold_change_cutoff;
        }
      }
    }

    load_collection_of_pwms_with_evaluators();
  }

  protected void extract_option(List<String> argv) throws FileNotFoundException {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      extract_background(argv.remove(0));
    } else if (opt.equals("-d") || opt.equals("--discretization")) {
      discretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      dataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      pseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
      if (!thresholds_folder.exists()) {
        throw new FileNotFoundException("Specified folder with thresholds `" + thresholds_folder + "` not exists");
      } else if (!thresholds_folder.isDirectory()) {
        throw new FileNotFoundException("`" + thresholds_folder + "` is not a directory");
      }
    } else if(opt.equals("--pvalue-cutoff") || opt.equals("-P")) {
      max_pvalue_cutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--fold-change-cutoff") || opt.equals("-F")) {
      min_fold_change_cutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--transpose")) {
      transpose = true;
    } else if (opt.equals("--expand-region")) {
      expand_region_length = Integer.valueOf(argv.remove(0));
    } else if(opt.equals("--compact")) {
      shortFormat = true;
    }  else if(opt.equals("--log-fold-change")) {
      useLogFoldChange = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }

  protected abstract SequenceWithSNVType encodeSequenceWithSNV(SequenceWithSNV sequenceWithSNV);

  private boolean pvalueSignificant(RegionAffinityInfos affinityInfos) {
    return affinityInfos.hasSiteOnAnyAllele(max_pvalue_cutoff);
  }

  private boolean foldChangeSignificant(RegionAffinityInfos affinityInfos) {
    if (useLogFoldChange) {
      double logFoldChange = affinityInfos.logFoldChange();
      return (logFoldChange >= min_fold_change_cutoff) || (logFoldChange <= - min_fold_change_cutoff);
    } else {
      double foldChange = affinityInfos.foldChange();
      return (foldChange >= min_fold_change_cutoff) || (foldChange <= 1.0 / min_fold_change_cutoff);
    }
  }

  private boolean affinityChangeSignificant(RegionAffinityInfos affinityInfos) {
    return pvalueSignificant(affinityInfos) && foldChangeSignificant(affinityInfos);
  }

  protected void process_snp(String snp_name, SequenceWithSNV seq_w_snp, SequenceWithSNVType encodedSequenceWithSNP) {
    for (ThresholdEvaluator<SequenceType, ModelType> motifEvaluator: pwmCollection) {
      ModelType pwm = motifEvaluator.pwm;

      if (seq_w_snp.length() >= pwm.length()) {
        RegionAffinityInfos affinityInfos;
        affinityInfos = new SingleSNVScan<>(pwm, seq_w_snp, encodedSequenceWithSNP, motifEvaluator.pvalueCalculator, expand_region_length).affinityInfos();
        if (affinityChangeSignificant(affinityInfos)) {
          if (shortFormat) {
            System.out.println(snp_name + "\t" + motifEvaluator.name + "\t" + affinityInfos.toStringShort());
          } else {
            System.out.println(snp_name + "\t" + motifEvaluator.name + "\t" + affinityInfos.toString(useLogFoldChange));
          }
        }
      } else {
        System.err.println("Can't scan sequence '" + seq_w_snp + "' (length " + seq_w_snp.length() + ") with motif of length " + pwm.length());
      }
    }
  }

  public void process() throws IOException {
    if (shortFormat) {
      System.out.println("# SNP name\tmotif\tP-value 1\tP-value 2\tposition 1\torientation 1\tposition 2\torientation 2");
    } else {
      if (useLogFoldChange) {
        System.out.println("# SNP name\tmotif\tposition 1\torientation 1\tword 1\tposition 2\torientation 2\tword 2\tallele 1/allele 2\tP-value 1\tP-value 2\tFold change (log2 scale)");
      } else {
        System.out.println("# SNP name\tmotif\tposition 1\torientation 1\tword 1\tposition 2\torientation 2\tword 2\tallele 1/allele 2\tP-value 1\tP-value 2\tFold change");
      }
    }

    final int necessaryLength = necessaryFlankLength();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path_to_file_w_snps)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty() || line.charAt(0) == '#') continue;
        String[] input_parts = line.split("\\s+", 3);
        String snp_name = input_parts[0];
        SequenceWithSNV seq_w_snp = SequenceWithSNV.fromString(input_parts[1]);
        SequenceWithSNV seq_extended = seq_w_snp.expandFlanksUpTo(necessaryLength);
        process_snp(snp_name,
            seq_extended,
            encodeSequenceWithSNV(seq_extended));
      }
    }
  }

  int necessaryFlankLength() {
    int maxMotifLength = 1;
    for (ThresholdEvaluator<SequenceType, ModelType> evaluator : pwmCollection) {
      maxMotifLength = Math.max(maxMotifLength, evaluator.pwm.length());
    }
    return maxMotifLength + expand_region_length;
  }

}
