package ru.autosome.perfectosape;

public class PPM2PWMConverter {
  public BackgroundModel background;
  private final PPM ppm;

  public PPM2PWMConverter(PPM ppm) {
    this.ppm = ppm;
    this.background = new WordwiseBackground();
  }

  public PWM convert() {
    double new_matrix[][] = new double[ppm.matrix.length][];
    for (int pos = 0; pos < ppm.matrix.length; ++pos) {
      new_matrix[pos] = new double[ppm.ALPHABET_SIZE];

      for (int letter = 0; letter < ppm.ALPHABET_SIZE; ++letter) {
        new_matrix[pos][letter] = Math.log(ppm.matrix[pos][letter] / background.probability(letter));
      }
    }
    return new PWM(new_matrix, ppm.name);
  }
}
