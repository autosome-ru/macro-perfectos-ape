package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.Collections;

public class FindThreshold {
    BackgroundModel background;
    Double discretization;
    String pvalue_boundary;
    Integer max_hash_size; // not int because it can be null

    PWM pwm;
    double[] pvalues;

    String pm_filename; // temporary variable
    String data_model; // temporary variable

    public void initialize_defaults() {
        background = new WordwiseBackground();
        discretization = 10000.0;
        pvalue_boundary = "lower";
        max_hash_size = 10000000;

        data_model = "pwm";

        pvalues = new double[1];
        pvalues[0] = 0.0005;
    }

    public FindThreshold() {
        initialize_defaults();
    }

    public static FindThreshold from_arglist(ArrayList<String> argv) {
        FindThreshold result = new FindThreshold();
        Helper.print_help_if_requested(argv, DOC);
        result.setup_from_arglist(argv);
        return result;
    }

    public static FindThreshold from_arglist(String[] args) {
        ArrayList<String> argv = new ArrayList<String>();
        Collections.addAll(argv, args);
        return from_arglist(argv);
    }

    public void setup_from_arglist(ArrayList<String> argv) {
        extract_pm_filename(argv);
        extract_pvalue_list(argv);
        while (argv.size() > 0) {
            extract_option(argv);
        }
        load_pwm();
    }

    private void extract_option(ArrayList<String> argv) {
        String opt = argv.remove(0);
        if (opt.equals("-b")) {
            background = Background.fromString(argv.remove(0));
        } else if (opt.equals("--max-hash-size")) {
            max_hash_size = Integer.valueOf(argv.remove(0));
        } else if (opt.equals("-d")) {
            discretization = Double.valueOf(argv.remove(0));
        } else if (opt.equals("--boundary")) {
            pvalue_boundary = argv.remove(0);
            if (!pvalue_boundary.equalsIgnoreCase("lower") &&
                    !pvalue_boundary.equalsIgnoreCase("upper")) {
                throw new IllegalArgumentException("boundary should be either lower or upper");
            }
        } else if (opt.equals("--pcm")) {
            data_model = "pcm";
        } else {
            throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
    }

    private void extract_pm_filename(ArrayList<String> argv) {
        if (argv.isEmpty()) {
            throw new IllegalArgumentException("No input. You should specify input file");
        }
        pm_filename = argv.remove(0);
    }

    private void extract_pvalue_list(ArrayList<String> argv) {
        ArrayList<Double> pvalues_tmp = new ArrayList<Double>();

        try {
            while (!argv.isEmpty()) {
                pvalues_tmp.add(Double.valueOf(argv.get(0)));
                argv.remove(0);
            }
        } catch (NumberFormatException e) {
        }
        if (pvalues_tmp.size() != 0) {
            pvalues = ArrayExtensions.toPrimitiveArray(pvalues_tmp);
        }
    }

    private void load_pwm() {
        if (data_model.equals("pcm")) {
            pwm = PCM.new_from_file_or_stdin(pm_filename).to_pwm(background);
        } else {
            pwm = PWM.new_from_file_or_stdin(pm_filename);
        }
    }

    public ArrayList<ThresholdInfo> find_thresholds_by_pvalues() {
        if (discretization != null) {
            pwm = pwm.discrete(discretization);
        }
        CountingPWM countingPWM = new CountingPWM(pwm, background);
        countingPWM.max_hash_size = max_hash_size;

        ArrayList<ThresholdInfo> threshold_infos;
        if (pvalue_boundary.equals("lower")) {
            threshold_infos = countingPWM.thresholds(pvalues);
        } else {
            threshold_infos = countingPWM.weak_thresholds(pvalues);
        }

        if (discretization == null) {
            return threshold_infos;
        } else {
            ArrayList<ThresholdInfo> downscaled_infos = new ArrayList<ThresholdInfo>();
            for (ThresholdInfo info : threshold_infos) {
                downscaled_infos.add(info.downscale(discretization));
            }
            return downscaled_infos;
        }

    }

    public OutputInformation report_table_layout() {
        OutputInformation infos = new OutputInformation();

        infos.add_parameter("V", "discretization value", discretization);
        infos.add_parameter("PB", "P-value boundary", pvalue_boundary);

        infos.background_parameter("B", "background", background);

        infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
        infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

        if (background.is_wordwise()) {
            infos.add_table_parameter("W", "number of recognized words", "recognized_words");
        }
        infos.add_table_parameter("T", "threshold", "threshold");

        return infos;
    }

    static String DOC =
            "Command-line format:\n" +
                    "java ru.autosome.macroape.FindThreshold <pat-file> [<list of P-values>...] [options]\n" +
                    "\n" +
                    "Options:\n" +
                    "  [-d <discretization level>]\n" +
                    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                    "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
                    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                    "\n" +
                    "Example:\n" +
                    "  java ru.autosome.macroape.FindThreshold motifs/KLF4_f2.pat\n" +
                    "  java ru.autosome.macroape.FindThreshold  motifs/KLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";

    public static void main(String args[]) {
        try {
            FindThreshold calculation = FindThreshold.from_arglist(args);
            ArrayList<ThresholdInfo> results = calculation.find_thresholds_by_pvalues();
            OutputInformation report_table = calculation.report_table_layout();
            report_table.data = results;
            System.out.println(report_table.result());
        } catch (Exception err) {
            System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
            err.printStackTrace();
            System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
            System.exit(1);
        }
    }
}
