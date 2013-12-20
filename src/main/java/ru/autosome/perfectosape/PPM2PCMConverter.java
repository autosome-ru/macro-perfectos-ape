package ru.autosome.perfectosape;

public class PPM2PCMConverter {
  private final PPM ppm;
  private final double count;

  public PPM2PCMConverter(PPM ppm, double count) {
    this.ppm = ppm;
    this.count = count;
  }

  public PCM convert() {
    double new_matrix[][] = new double[ppm.matrix.length][];
    for (int pos = 0; pos < ppm.matrix.length; ++pos) {
      new_matrix[pos] = new double[ppm.ALPHABET_SIZE];

      for (int letter = 0; letter < ppm.ALPHABET_SIZE; ++letter) {
        new_matrix[pos][letter] = ppm.matrix[pos][letter] * count;
      }
    }
    return new PCM(new_matrix, ppm.name);
  }
}
