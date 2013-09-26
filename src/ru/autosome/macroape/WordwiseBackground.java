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
}
