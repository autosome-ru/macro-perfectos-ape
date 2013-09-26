package ru.autosome.macroape;

interface BackgroundModel {
    public double[] probability();

    public double probability(int index);

    public double volume(); // 1 for probability model, 4 for wordwise model

    public String toString();

    public boolean is_wordwise();
}
