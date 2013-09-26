package ru.autosome.macroape;

import java.util.HashMap;

// TODO: extract interface for converters
public class PCM2PWMConverter {
    public BackgroundModel background;
    public double pseudocount;
    PCM pcm;

    public PCM2PWMConverter(PCM pcm) {
        this.pcm = pcm;
        this.background = new WordwiseBackground();
        this.pseudocount = Math.log(pcm.count());
    }

    public PWM convert() {
        double new_matrix[][] = new double[pcm.matrix.length][];
        for (int pos = 0; pos < pcm.matrix.length; ++pos) {
            new_matrix[pos] = new double[4];
            for (int letter = 0; letter < 4; ++letter) {
                double numerator = pcm.matrix[pos][letter] + background.probability(letter) * pseudocount;
                double denominator = background.probability(letter) * (pcm.count() + pseudocount);
                new_matrix[pos][letter] = Math.log(numerator / denominator);
            }
        }
        return new PWM(new_matrix, pcm.background, pcm.name);
    }
}
