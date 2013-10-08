package ru.autosome.macroape.CLI;

import ru.autosome.macroape.*;
import ru.autosome.macroape.Calculations.FindPvalueAPE;
import ru.autosome.macroape.Calculations.FindPvalueBsearch;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MultiSNPScan {
  private BackgroundModel background;
  private Double discretization;
  private Integer max_hash_size;

  private File path_to_collection_of_pwms;
  private File path_to_file_w_snps;
  private File path_to_results_folder;

  private String data_model;
  private File thresholds_folder;

  private ArrayList<String> snp_list;
  Map<File, PWM> collection_of_pwms;
  Map<File, CanFindPvalue> pvalue_calculators;


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

  private void load_collection_of_pwms() {
    File[] files = path_to_collection_of_pwms.listFiles();
    if (files == null)
      return;
    for (File file : files) {
      collection_of_pwms.put(file, load_pwm(file));
    }
  }

  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.macroape.CLI.MultiSNPScan <folder with pwms> <file with SNPs> <folder for results>\n" +
    "\n" +
    "Options:\n" +
    "  [-d <discretization level>]\n" +
    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
    "\n" +
    "Example:\n" +
    "  java ru.autosome.macroape.CLI.MultiSNPScan ./hocomoco/pwms/ snp.txt ./results --precalc ./collection_thresholds\n" +
    "  java ru.autosome.macroape.CLI.MultiSNPScan ./hocomoco/pcms/ snp.txt ./results --pcm -d 10\n";

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

  void extract_path_to_results_folder(ArrayList<String> argv) {
    try {
      path_to_results_folder = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify output folder", e);
    }
  }

  private void initialize_defaults() {
    background = new WordwiseBackground();
    discretization = 100.0;
    max_hash_size = 10000000;

    data_model = "pwm";
    thresholds_folder = null;
    collection_of_pwms = new HashMap<File, PWM>();
    pvalue_calculators = new HashMap<File, CanFindPvalue>();
  }

  private MultiSNPScan() {
    initialize_defaults();
  }

  private static MultiSNPScan from_arglist(ArrayList<String> argv) {
    MultiSNPScan result = new MultiSNPScan();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static MultiSNPScan from_arglist(String[] args) {
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
    load_collection_of_pwms();
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
      thresholds_folder = new File(argv.remove(0));
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

  private void load_snp_list() {
    try {
      InputStream reader = new FileInputStream(path_to_file_w_snps);
      snp_list = InputExtensions.filter_empty_strings(InputExtensions.readLinesFromInputStream(reader));
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to load pack of SNPs", e);
    }
  }

  private void setup_output_folder() {
    if (!path_to_results_folder.exists()) {
      path_to_results_folder.mkdir();
    }
  }

  private void setup_pvalue_calculation() {
    collection_of_pwms.forEach(new BiConsumer<File, PWM>() {
      public void accept(File file, PWM pwm) {
        if (thresholds_folder != null) {
          File thresholds_file = new File(thresholds_folder, "thresholds_" + file.getName());
          PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file.getAbsolutePath());
          pvalue_calculators.put(file,
                                 new FindPvalueBsearch(new FindPvalueBsearch.Parameters(pwm, background, pvalueBsearchList)));
        } else {
          pvalue_calculators.put(file,
                                 new FindPvalueAPE(new FindPvalueAPE.Parameters(pwm, discretization, background, max_hash_size)));
        }
      }
    });
  }

  private void process_snp(String snp_input) throws IOException {
    String snp_name = first_part_of_string(snp_input);
    SequenceWithSNP seq_w_snp = SequenceWithSNP.fromString(last_part_of_string(snp_input));
    FileWriter fw;
    File resulting_file = new File(path_to_results_folder, snp_name + ".txt");
    fw = new FileWriter(resulting_file);
    try {
      System.out.println(snp_name);
      fw.write(seq_w_snp + "\n");
      fw.write("SNP name\tmotif\tposition1\torientation1\tword1\tP-value1\tposition2\torientation2\tword2\tP-value2\tFold change\n");

      for (File file : collection_of_pwms.keySet()) {
        PWM pwm = collection_of_pwms.get(file);
        CanFindPvalue canFindPvalue = pvalue_calculators.get(file);
        String infos = new SnpScan(pwm, seq_w_snp, canFindPvalue).pwm_influence_infos();
        if (infos != null) {
          fw.write(snp_name +"\t" + pwm.name + "\t" + infos + "\n");
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
