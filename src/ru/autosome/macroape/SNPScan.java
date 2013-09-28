package ru.autosome.macroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SNPScan {
    BackgroundModel background;
    double discretization;
    Integer max_hash_size;

    String path_to_collection_of_pwms;
    String path_to_file_w_snps;
    String path_to_results_folder;

    SNPScan calculation;
    String data_model;
    String thresholds_folder;

    ArrayList<PwmWithFilename> collection;
    ArrayList<String> snp_list;


    // Split by spaces and return last part
    // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
    // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
    public static String last_part_of_string(String s) {
        String[] string_parts = s.replaceAll("\\s+", " ").split(" ");
        String result = string_parts[string_parts.length - 1];
        if (result.matches("[ACGT]+(/[ACGT]+)+") || result.matches("[ACGT]+\\[(/?[ACGT]+)+\\][ACGT]+")) {
            return result;
        } else {
            return string_parts[string_parts.length - 3] + "[" + string_parts[string_parts.length - 2].replaceAll("\\[|\\]", "") + "]" + string_parts[string_parts.length - 1];
        }
    }

    // Output: "rs9929218"
    public static String first_part_of_string(String s) {
        return s.replaceAll("\\s+", " ").split(" ")[0];
    }

    public String pwm_influence_infos(SequenceWithSNP seq_w_snp, PWM pwm, CanFindPvalue pvalue_calculation) {
        Sequence[] trimmed_sequence_variants = seq_w_snp.trimmed_sequence_variants(pwm);

        if (seq_w_snp.num_cases() != 2)
            return null; // Unable to process more than two variants(which fractions to return)

        String result = "";

        ScanSequence scan_seq_1 = new ScanSequence(trimmed_sequence_variants[0], pwm);
        double score_1 = scan_seq_1.best_score_on_sequence();
        double pvalue_1 = pvalue_calculation.pvalue_by_threshold(score_1).pvalue;

        ScanSequence scan_seq_2 = new ScanSequence(trimmed_sequence_variants[1], pwm);
        double score_2 = scan_seq_2.best_score_on_sequence();
        double pvalue_2 = pvalue_calculation.pvalue_by_threshold(score_2).pvalue;
        // We print position from the start of seq, not from the start of overlapping region, thus should calculate the shift
        int left_shift = seq_w_snp.left_shift(pwm.length());
        result = scan_seq_1.best_match_info_string(left_shift) + "\t" + pvalue_1 + "\t";
        result += scan_seq_2.best_match_info_string(left_shift) + "\t" + pvalue_2 + "\t";
        result += pvalue_2 / pvalue_1;
        return result;
    }

    public static ArrayList<PwmWithFilename> load_collection_of_pwms(String dir_name) throws FileNotFoundException {
        ArrayList<PwmWithFilename> result = new ArrayList<PwmWithFilename>();
        java.io.File dir = new java.io.File(dir_name);
        for (File file : dir.listFiles()) {
            PWM pwm = PWM.new_from_file(file);
            if (pwm.name == null || pwm.name.isEmpty()) {
                pwm.name = file.getName();
            }
            result.add(new PwmWithFilename(pwm, file.getPath()));
        }
        return result;
    }

    public static ArrayList<PwmWithFilename> load_collection_of_pwms_from_pcms(String dir_name, BackgroundModel background) throws FileNotFoundException {
        ArrayList<PwmWithFilename> result = new ArrayList<PwmWithFilename>();
        java.io.File dir = new java.io.File(dir_name);
        for (File file : dir.listFiles()) {
            PWM pwm = PCM.new_from_file(file).to_pwm(background);
            if (pwm.name == null || pwm.name.isEmpty()) {
                pwm.name = file.getName();
            }
            result.add(new PwmWithFilename(pwm, file.getPath()));
        }
        return result;
    }

    static String DOC =
            "Command-line format:\n" +
                    "java ru.autosome.macroape.SNPScan <folder with pwms> <file with SNPs> <folder for results>\n" +
                    "\n" +
                    "Options:\n" +
                    "  [-d <discretization level>]\n" +
                    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
                    "\n" +
                    "Example:\n" +
                    "  java ru.autosome.macroape.SNPScan ./hocomoco/pwms/ snp.txt ./results --precalc ./collection_thresholds\n" +
                    "  java ru.autosome.macroape.SNPScan ./hocomoco/pcms/ snp.txt ./results --pcm -d 10\n";

    public void extract_path_to_collection_of_pwms(ArrayList<String> argv) {
        try {
            path_to_collection_of_pwms = argv.remove(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Specify PWM-collection folder", e);
        }
    }

    public void extract_path_to_file_w_snps(ArrayList<String> argv) {
        try {
            path_to_file_w_snps = argv.remove(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Specify file with SNPs", e);
        }
    }

    public void extract_path_to_results_folder(ArrayList<String> argv) {
        try {
            path_to_results_folder = argv.remove(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Specify output folder", e);
        }
    }

    private void initialize_defaults() {
        background = new WordwiseBackground();
        discretization = 100;
        max_hash_size = 10000000;

        calculation = new SNPScan();
        data_model = "pwm";
        thresholds_folder = null;
    }

    public SNPScan() {
        initialize_defaults();
    }

    public static SNPScan from_arglist(ArrayList<String> argv) {
        SNPScan result = new SNPScan();
        Helper.print_help_if_requested(argv, DOC);
        result.setup_from_arglist(argv);
        return result;
    }

    public static SNPScan from_arglist(String[] args) {
        ArrayList<String> argv = new ArrayList<String>();
        Collections.addAll(argv, args);
        return from_arglist(argv);
    }

    public void setup_from_arglist(ArrayList<String> argv) {
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

    private void load_collection() {
        try {
            if (data_model.equals("pcm")) {
                collection = load_collection_of_pwms_from_pcms(path_to_collection_of_pwms, background);
            } else {
                collection = load_collection_of_pwms(path_to_collection_of_pwms);
            }
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
        for (PwmWithFilename pwm : collection) {
            pwm.pvalue_calculation = find_pvalue_calculator(pwm);
        }
    }

    private CanFindPvalue find_pvalue_calculator(PwmWithFilename pwm_w_filename) {
        if (thresholds_folder == null) {
            FindPvalueAPE pvalue_calculation = new FindPvalueAPE(pwm_w_filename.pwm);
            pvalue_calculation.background = calculation.background;
            pvalue_calculation.discretization = calculation.discretization;
            pvalue_calculation.max_hash_size = calculation.max_hash_size;
            return pvalue_calculation;
        } else {
            String filename = thresholds_folder + File.separator + "thresholds_" + (new File(pwm_w_filename.filename)).getName();
            FindPvalueBsearch pvalue_calculation = FindPvalueBsearch.load_from_file(pwm_w_filename.pwm, filename);
            pvalue_calculation.background = calculation.background;
            return pvalue_calculation;
        }
    }

    void process_snp(String snp_input) throws IOException {
        String snp_name = first_part_of_string(snp_input);
        SequenceWithSNP seq_w_snp = new SequenceWithSNP(last_part_of_string(snp_input));
        FileWriter fw;
        String resulting_filename = (path_to_results_folder + File.separator + snp_name + ".txt");
        fw = new FileWriter(new java.io.File(resulting_filename));
        try {
            System.out.println(snp_name);
            fw.write(seq_w_snp + "\n");
            fw.write("PWM-name\t||Normal pos\torientation\tword\tpvalue\t||Changed pos\torientation\tword\tpvalue\t||changed_pvalue/normal_pvalue\n");

            for (PwmWithFilename pwm : collection) {
                String infos = calculation.pwm_influence_infos(seq_w_snp, pwm.pwm, pwm.pvalue_calculation);
                if (infos != null) {
                    fw.write(pwm.pwm.name + "\t" + infos + "\n");
                }
            }
        } finally {
            fw.close();
        }
    }

    public void process() {
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
