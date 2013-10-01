package ru.autosome.macroape.CLI;

import ru.autosome.macroape.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class SNPScan {
  private BackgroundModel background;
  private Double discretization;
  private Integer max_hash_size;

  private File path_to_collection_of_pwms;
  private String path_to_file_w_snps;
  private String path_to_results_folder;

  private SNPScan calculation;
  private String data_model;
  private String thresholds_folder;

  private ArrayList<PwmWithFilename> collection;
  private ArrayList<String> snp_list;


  // Split by spaces and return last part
  // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  private static String last_part_of_string(String s) {
    String[] string_parts = s.replaceAll("\\s+", " ").split(" ");
    String result = string_parts[string_parts.length - 1];
    if (result.matches("[ACGT]+(/[ACGT]+)+") || result.matches("[ACGT]+\\[(/?[ACGT]+)+\\][ACGT]+")) {
      return result;
    } else {
      return string_parts[string_parts.length - 3] + "[" + string_parts[string_parts.length - 2].replaceAll("\\[|\\]", "") + "]" + string_parts[string_parts.length - 1];
    }
  }

  // Output: "rs9929218"
  private static String first_part_of_string(String s) {
    return s.replaceAll("\\s+", " ").split(" ")[0];
  }

  String pwm_influence_infos(SequenceWithSNP seq_w_snp, PwmWithFilename pwm) {
    Sequence[] trimmed_sequence_variants = seq_w_snp.trimmed_sequence_variants(pwm.pwm);

    if (seq_w_snp.num_cases() != 2)
      return null; // Unable to process more than two variants(which fractions to return)

    String result;

    ScanSequence scan_seq_1 = new ScanSequence(trimmed_sequence_variants[0], pwm.pwm);
    double score_1 = scan_seq_1.best_score_on_sequence();

    double[] thresholds_1 = {score_1};
    double pvalue_1 = find_pvalue_calculator(pwm, thresholds_1).pvalues_by_thresholds().get(0).pvalue;

    ScanSequence scan_seq_2 = new ScanSequence(trimmed_sequence_variants[1], pwm.pwm);
    double score_2 = scan_seq_2.best_score_on_sequence();
    double[] thresholds_2 = {score_2};
    double pvalue_2 = find_pvalue_calculator(pwm, thresholds_2).pvalues_by_thresholds().get(0).pvalue;
    // We print position from the start of seq, not from the start of overlapping region, thus should calculate the shift
    int left_shift = seq_w_snp.left_shift(pwm.pwm.length());
    result = scan_seq_1.best_match_info_string(left_shift) + "\t" + pvalue_1 + "\t";
    result += scan_seq_2.best_match_info_string(left_shift) + "\t" + pvalue_2 + "\t";
    result += pvalue_2 / pvalue_1;
    return result;
  }

  private ArrayList<PwmWithFilename> load_collection_of_pwms() {
    ArrayList<PwmWithFilename> result = new ArrayList<PwmWithFilename>();
    for (File file : path_to_collection_of_pwms.listFiles()) {
      result.add(new PwmWithFilename(load_pwm(file), file.getPath()));
    }
    return result;
  }

  private static final String DOC =
          "Command-line format:\n" +
                  "java ru.autosome.macroape.CLI.SNPScan <folder with pwms> <file with SNPs> <folder for results>\n" +
                  "\n" +
                  "Options:\n" +
                  "  [-d <discretization level>]\n" +
                  "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                  "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                  "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
                  "\n" +
                  "Example:\n" +
                  "  java ru.autosome.macroape.CLI.SNPScan ./hocomoco/pwms/ snp.txt ./results --precalc ./collection_thresholds\n" +
                  "  java ru.autosome.macroape.CLI.SNPScan ./hocomoco/pcms/ snp.txt ./results --pcm -d 10\n";

  void extract_path_to_collection_of_pwms(ArrayList<String> argv) {
    try {
      path_to_collection_of_pwms = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  void extract_path_to_file_w_snps(ArrayList<String> argv) {
    try {
      path_to_file_w_snps = argv.remove(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify file with SNPs", e);
    }
  }

  void extract_path_to_results_folder(ArrayList<String> argv) {
    try {
      path_to_results_folder = argv.remove(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify output folder", e);
    }
  }

  private void initialize_defaults() {
    background = new WordwiseBackground();
    discretization = 100.0;
    max_hash_size = 10000000;

    calculation = new SNPScan();
    data_model = "pwm";
    thresholds_folder = null;
  }

  private SNPScan() {
    initialize_defaults();
  }

  private static SNPScan from_arglist(ArrayList<String> argv) {
    SNPScan result = new SNPScan();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static SNPScan from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  void setup_from_arglist(ArrayList<String> argv) {
    extract_path_to_collection_of_pwms(argv);
    extract_path_to_file_w_snps(argv);
    extract_path_to_results_folder(argv);
    setup_output_folder();

    while (argv.size() > 0) {
      extract_option(argv);
    }
    load_collection();
    setup_pvalue_calculation();
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
      data_model = "pcm";
    } else if (opt.equals("--precalc")) {
      thresholds_folder = argv.remove(0);
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  PWM load_pwm(File file) {
    PWM pwm;
    if (data_model.equals("pcm")) {
      pwm = PCM.new_from_file(file).to_pwm(background);
    } else {
      pwm = PWM.new_from_file(file);
    }
    if (pwm.name == null || pwm.name.isEmpty()) {
      pwm.name = file.getName();
    }
    return pwm;
  }

  private void load_collection() {
    try {
      collection = load_collection_of_pwms();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to load collection of PWMs", e);
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

  private void setup_output_folder() {
    File results_folder = new File(path_to_results_folder);
    if (!results_folder.exists()) {
      results_folder.mkdir();
    }
  }

  private void setup_pvalue_calculation() {
    for (PwmWithFilename pwm_w_filename: collection) {
      if (thresholds_folder != null) {
        String filename = thresholds_folder + File.separator + "thresholds_" + (new File(pwm_w_filename.filename)).getName();
        pwm_w_filename.bsearchList = PvalueBsearchList.load_from_file(filename);
      }
    }
  }

  CanFindPvalue find_pvalue_calculator(PwmWithFilename pwm_w_filename, double[] thresholds) {
    if (pwm_w_filename.bsearchList == null) {
      FindPvalueAPEParameters parameters = new FindPvalueAPEParameters(pwm_w_filename.pwm,
                                                                       thresholds,
                                                                       discretization,
                                                                       background,
                                                                       max_hash_size);
      return new FindPvalueAPE(parameters);
    } else {
      FindPvalueBsearchParameters parameters = new FindPvalueBsearchParameters(pwm_w_filename.pwm,
                                                                               thresholds, background,
                                                                               pwm_w_filename.bsearchList);
      return new FindPvalueBsearch(parameters);
    }
  }

  void process_snp(String snp_input) throws IOException {
    String snp_name = first_part_of_string(snp_input);
    SequenceWithSNP seq_w_snp = SequenceWithSNP.fromString(last_part_of_string(snp_input));
    FileWriter fw;
    String resulting_filename = (path_to_results_folder + File.separator + snp_name + ".txt");
    fw = new FileWriter(new File(resulting_filename));
    try {
      System.out.println(snp_name);
      fw.write(seq_w_snp + "\n");
      fw.write("PWM-name\t||Normal pos\torientation\tword\tpvalue\t||Changed pos\torientation\tword\tpvalue\t||changed_pvalue/normal_pvalue\n");

      for (PwmWithFilename pwm : collection) {
        String infos = calculation.pwm_influence_infos(seq_w_snp, pwm);
        if (infos != null) {
          fw.write(pwm.pwm.name + "\t" + infos + "\n");
        }
      }
    } finally {
      fw.close();
    }
  }

  void process() {
    for (String snp_input : snp_list) {
      try {
        process_snp(snp_input);
      } catch (IOException e) {
        System.err.println("SNP " + snp_input + "wasn't processed due to IO-error");
      }
    }
  }

  public static void main(String[] args) {
    try {
      SNPScan calculation = SNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);

    }
  }

}
