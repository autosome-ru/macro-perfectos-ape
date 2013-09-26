package ru.autosome.macroape;

public class WordwiseBackground implements BackgroundModel {
    public WordwiseBackground() {
    }

    public double[] probability() {
        double[] result;
        result = new double[]{0.25, 0.25, 0.25, 0.25};
        return result;
    }

    public double probability(int index) {
        return 0.25;
    }

    public double volume() {
        return 4;
    }

    public String toString() {
        return "1,1,1,1";
    }

    public boolean is_wordwise() {
        return false;
    }

    public double mean_value(double[] values) {
        double sum = 0;
        for (int letter = 0; letter < 4; ++letter) {
            sum += values[letter];
        }
        return sum / 4.0;
    }

    public double mean_square_value(double[] values) {
        double sum_square = 0.0;
        for (int letter = 0; letter < 4; ++letter) {
            sum_square += values[letter] * values[letter];
        }
        return sum_square / 4.0;
    }
}
