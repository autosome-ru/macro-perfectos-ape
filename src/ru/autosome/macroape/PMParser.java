package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.StringTokenizer;

// Usual parser of 4-column matrix with or without name
class PMParser {
    private final ArrayList<String> inp_strings;
    private double[][] matrix;
    private String name;

    PMParser(ArrayList<String> input) {
        inp_strings = input;
        parse();
    }

    void parse() {
        ArrayList<double[]> matrix_as_list = new ArrayList<double[]>();
        name = "";
        int i = 0;
        try {
            Double.valueOf(inp_strings.get(0).replaceAll("\\s+", " ").split(" ")[0]);
        } catch (NumberFormatException e) {
            name = inp_strings.get(0).trim();
            while (name.charAt(0) == '>' || name.charAt(0) == ' ' || name.charAt(0) == '\t') {
                name = name.substring(1, name.length());
            }
            i++;
        }

        for (; i < inp_strings.size(); ++i) {
            double[] tmp = new double[4];
            StringTokenizer parser = new StringTokenizer(inp_strings.get(i).replaceAll("\\s+", " "));
            for (int j = 0; j < 4; ++j) {
                tmp[j] = Double.valueOf(parser.nextToken(" "));
            }
            matrix_as_list.add(tmp);
        }
        matrix = new double[matrix_as_list.size()][];
        for (int j = 0; j < matrix_as_list.size(); ++j) {
            matrix[j] = matrix_as_list.get(j);
        }
    }

    double[][] matrix() {
        return matrix;
    }

    String name() {
        return name;
    }
}
