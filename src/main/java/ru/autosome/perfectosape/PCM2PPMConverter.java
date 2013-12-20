package ru.autosome.perfectosape;

public class PCM2PPMConverter {
  private final PCM pcm;

  public PCM2PPMConverter(PCM pcm) {
    this.pcm = pcm;
  }

  public PPM convert() {
    double new_matrix[][] = new double[pcm.matrix.length][];
    for (int pos = 0; pos < pcm.matrix.length; ++pos) {
      new_matrix[pos] = new double[pcm.ALPHABET_SIZE];

      // columns can have different counts for some PCMs
      double count = 0.0;
      for(double element: pcm.matrix[pos]) {
        count += element;
      }

      for (int letter = 0; letter < pcm.ALPHABET_SIZE; ++letter) {
        new_matrix[pos][letter] = pcm.matrix[pos][letter] / count;
      }
    }
    return new PPM(new_matrix, pcm.name);
  }
}
