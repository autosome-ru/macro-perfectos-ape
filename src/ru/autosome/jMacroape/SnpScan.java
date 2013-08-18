package ru.autosome.jMacroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SnpScan {
  double[] background;
  double discretization;
  Integer max_hash_size;

  public SnpScan() {
    background = Helper.wordwise_background();
    discretization = 100;
    max_hash_size = 10000000;
  }

  public HashMap<String, Object> parameters() {
    HashMap<String, Object> result = new HashMap<String,Object>();
    result.put("discretization", discretization);
    result.put("background", background);
    result.put("max_hash_size", max_hash_size);
    return result;
  }


  // line should finish with sequence (which doesn't have spaces).
  // Example:
  // input:  "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // possible SNP formats: [a/g]; [ag]; a/g; a/g/c; [agc]; [a/g/c] and so on
  // output: ["GATTCAAAGGTTCTGAATTCCACAACaGCTTTCCTGTGTTTTTGCAGCCAGA",
  //          "GATTCAAAGGTTCTGAATTCCACAACgGCTTTCCTGTGTTTTTGCAGCCAGA"]
  public static String[] sequence_variants(String seq_w_snp) throws IllegalArgumentException {
    String[] seq_parts = seq_w_snp.split("\\[|\\]");  // split by [ or ]

    String left;
    String right;
    char[] mid;
    if (seq_parts.length == 1) { //   accT/Acca  or  accT/A/Gcca
      int left_separator = seq_w_snp.indexOf("/");
      int right_separator = seq_w_snp.lastIndexOf("/");
      left = seq_w_snp.substring(0,left_separator - 1);
      right = seq_w_snp.substring(right_separator + 2, seq_w_snp.length());
      mid = seq_w_snp.substring(left_separator - 1, right_separator + 2).replaceAll("/","").toCharArray();
    } else if (seq_parts.length == 3) { // acc[T/A]cca  or acc[TA]cca or  acc[T/A/G]cca  or acc[TAG]cca
      left = seq_parts[0];
      mid = seq_parts[1].replaceAll("/","").toCharArray();
      right = seq_parts[2];
    } else {
      throw new IllegalArgumentException("Can't parse sequence with SNPs: "+ seq_w_snp);
    }
    int num_cases = mid.length;

    String[] result = new String[num_cases];
    for (int i = 0; i < num_cases; ++i) {
      result[i] = left + mid[i] + right;
    }
    return result;
  }

  // Split by spaces and return last part
  // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  public static String last_part_of_string(String s) {
    String[] string_parts = s.replaceAll("\\s+"," ").split(" ");
    String result = string_parts[string_parts.length - 1];
    if (result.matches("[ACGT]+(/[ACGT]+)+") || result.matches("[ACGT]+\\[(/?[ACGT]+)+\\][ACGT]+")) {
      return result;
    } else {
      return string_parts[string_parts.length - 3] + "[" + string_parts[string_parts.length - 2].replaceAll("\\[|\\]","") + "]" + string_parts[string_parts.length - 1];
    }
  }
  // Output: "rs9929218"
  public static String first_part_of_string(String s) {
    return s.replaceAll("\\s+"," ").split(" ")[0];
  }

  // Not a whole string, only sequence
  public static int pos_of_snp(String seq_w_snp) {
    String[] seq_parts = seq_w_snp.split("\\[|\\]");  // split by [ or ]
    if (seq_parts.length == 1) { //   accT/Acca  or  accT/A/Gcca
      int left_separator = seq_w_snp.indexOf("/");
      return left_separator - 1;
    } else if (seq_parts.length == 3) { // acc[T/A]cca  or acc[TA]cca or  acc[T/A/G]cca  or acc[TAG]cca
      return seq_parts[0].length();
    } else {
      throw new IllegalArgumentException("Can't parse sequence with SNPs: " + seq_w_snp);
    }
  }

  public static int left_shift(int seq_length, int snp_position, int motif_length) {
    return Math.max(0, snp_position - motif_length + 1);
  }
  public static String trim_to_motif_length(String seq, int snp_position, int motif_length) {
    return seq.substring(Math.max(0, snp_position - motif_length + 1),
                         Math.min(seq.length(), snp_position + motif_length)); // end point not included
  }
  public static String[] trimmed_sequence_variants(String seq_w_snp, PM pm){
    int pos_of_snp = pos_of_snp(seq_w_snp);
    String[] sequence_variants = sequence_variants(seq_w_snp);
    String[] trimmed_sequence_variants = new String[sequence_variants.length];
    for(int i = 0; i < sequence_variants.length; ++i) {
      trimmed_sequence_variants[i] = trim_to_motif_length(sequence_variants[i], pos_of_snp, pm.length());
    }
    return trimmed_sequence_variants;
  }

  public static int length_of_sequence_w_snp(String seq_w_snp) {
    return sequence_variants(seq_w_snp)[0].length();
  }

  public String pwm_influence_infos(String seq_w_snp, PWM pwm) {
    String[] trimmed_sequence_variants = trimmed_sequence_variants(seq_w_snp, pwm);

    if(trimmed_sequence_variants.length != 2) return null; // Unable to process more than two variants(which fractions to return)

    String result = "";
    int pos_of_snp = pos_of_snp(seq_w_snp);
    int seq_len = length_of_sequence_w_snp(seq_w_snp);
    int left_shift = left_shift(seq_len, pos_of_snp, pwm.length());
    FindPvalue calculation = new FindPvalue(pwm);
    calculation.set_parameters(parameters());

    ScanSequence scan_seq_1 = new ScanSequence(trimmed_sequence_variants[0], pwm);
    double score_1 = scan_seq_1.best_score_on_sequence();
    double pvalue_1 = calculation.pvalue_by_threshold(score_1).pvalue;

    ScanSequence scan_seq_2 = new ScanSequence(trimmed_sequence_variants[1], pwm);
    double score_2 = scan_seq_2.best_score_on_sequence();
    double pvalue_2 = calculation.pvalue_by_threshold(score_2).pvalue;

    result = scan_seq_1.best_match_info_string(left_shift) + "\t" + pvalue_1 + "\t";
    result += scan_seq_2.best_match_info_string(left_shift) + "\t" + pvalue_2 + "\t";
    result += pvalue_2 / pvalue_1;
    return result;
  }

  public static ArrayList<PWM> load_collection_of_pwms(String dir_name, double[] background, boolean from_pcm) throws FileNotFoundException {
    ArrayList<PWM> result = new ArrayList<PWM>();
    java.io.File dir = new java.io.File(dir_name);
    for(File file: dir.listFiles()) {
      PWM pwm = PWM.new_from_file(file, background, from_pcm);
      if (pwm.name == null || pwm.name.isEmpty()) {
        pwm.name = file.getName();
      }
      result.add(pwm);
    }
    return result;
  }

  static String DOC =
          "Command-line format:\n" +
          "java ru.autosome.jMacroape.SnpScan <folder with pwms> <file with SNPs> <folder for results>\n"+
          "\n" +
          "Options:\n" +
          "  [-d <discretization level>]\n" +
          "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
          "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
          "\n" +
          "Example:\n" +
          "  java ru.autosome.jMacroape.SnpScan ./hocomoco/pwms/ snp.txt ./results\n" +
          "  java ru.autosome.jMacroape.SnpScan ./hocomoco/pcms/ snp.txt ./results --pcm -d 10\n";

  public static void main(String[] args) {
    try{
      ArrayList<String> argv = new ArrayList<String>();
      Collections.addAll(argv, args);

      if (argv.isEmpty()  || ArrayExtensions.contain(argv,"-h") || ArrayExtensions.contain(argv,"--h")
                          || ArrayExtensions.contain(argv,"-help") || ArrayExtensions.contain(argv,"--help")) {
        System.err.println(DOC);
        System.exit(1);
      }

      String path_to_collection_of_pwms = argv.remove(0);
      String path_to_file_w_snps = argv.remove(0);
      String path_to_results_folder = argv.remove(0);

      File results_folder = new File(path_to_results_folder);
      if (!results_folder.exists()) {
        results_folder.mkdir();
      }

      double[] background = {1.0, 1.0, 1.0, 1.0};
      SnpScan calculation = new SnpScan();

      String data_model = "pwm";

      while (argv.size() > 0) {
        String opt = argv.remove(0);
        if (opt.equals("-b")) {
          StringTokenizer parser = new StringTokenizer(argv.remove(0));
          for (int i = 0; i < 4; ++i) {
            background[i] = Double.valueOf(parser.nextToken(","));
          }
        } else if (opt.equals("--max-hash-size")) {
          calculation.max_hash_size = Integer.valueOf(argv.remove(0));
        } else if (opt.equals("-d")) {
          calculation.discretization = Double.valueOf(argv.remove(0));
        } else if (opt.equals("--pcm")) {
          data_model = "pcm";
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      calculation.background = background;
      ArrayList<PWM> collection = load_collection_of_pwms(path_to_collection_of_pwms, background, data_model.equals("pcm"));

      InputStream reader = new FileInputStream(path_to_file_w_snps);
      ArrayList<String> snp_list = InputExtensions.filter_empty_strings(InputExtensions.readLinesFromInputStream(reader));

      for(String snp_input: snp_list) {
        String snp_name = first_part_of_string(snp_input);
        String seq_w_snp = last_part_of_string(snp_input);
        FileWriter fw;
        String resulting_filename = (path_to_results_folder + File.separator + snp_name + ".txt");
        fw = new FileWriter(new java.io.File(resulting_filename));
        try{
          System.out.println(snp_name);
          fw.write(seq_w_snp + "\n");
          fw.write("PWM-name\t||Normal pos\torientation\tword\tpvalue\t||Changed pos\torientation\tword\tpvalue\t||changed_pvalue/normal_pvalue\n");

          for(PWM pwm: collection) {
            String infos = calculation.pwm_influence_infos(seq_w_snp, pwm);
            if (infos != null) {
              fw.write(pwm.name + "\t" + infos + "\n");
            }
          }
        } finally {
          fw.close();
        }
      }
    } catch(Exception err) {
      System.err.println("\n" + err + "\n");
      err.printStackTrace();
      System.err.println("\n\nUse --help option for help\n\n" + DOC);
    }
  }

}
