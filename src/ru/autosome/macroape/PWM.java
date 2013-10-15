package ru.autosome.macroape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.ceil;

public class PWM extends PM {
  public PWM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
  }

  // instead of from_pcm argument one should create new_from_* family of methods for PCM
  private static PWM new_from_text(ArrayList<String> input_lines) {
    PMParser matrix_parser = new PMParser(input_lines);

    double[][] matrix = matrix_parser.matrix();
    String name = matrix_parser.name();

    return new PWM(matrix, name);
  }

  public static PWM new_from_file(File file) {
    try {
      InputStream reader = new FileInputStream(file);
      return new_from_text(InputExtensions.readLinesFromInputStream(reader));
    } catch (FileNotFoundException err) {
      return null;
    }
  }

  public static PWM new_from_file_or_stdin(String filename) {
    try {
      InputStream reader;
      if (filename.equals(".stdin")) {
        reader = System.in;
      } else {
        if (!(new File(filename).exists())) {
          throw new RuntimeException("Error! File #{filename} doesn't exist");
        }
        reader = new FileInputStream(filename);
      }
      return new_from_text(InputExtensions.readLinesFromInputStream(reader));
    } catch (FileNotFoundException err) {
      return null;
    }
  }

  double score(String word) throws IllegalArgumentException {
    return score(word, new WordwiseBackground());
  }

  double score(String word, BackgroundModel background) throws IllegalArgumentException {
    word = word.toUpperCase();
    if (word.length() != length()) {
      throw new IllegalArgumentException("word in PWM#score(word) should have the same length as matrix");
    }
    double sum = 0.0;
    for (int pos_index = 0; pos_index < length(); ++pos_index) {
      char letter = word.charAt(pos_index);
      Integer letter_index = indexByLetter.get(letter);
      if (letter_index != null) {
        sum += matrix[pos_index][letter_index];
      } else if (letter == 'N') {
        sum += background.mean_value(matrix[pos_index]);
      } else {
        throw new IllegalArgumentException("word in PWM#score(#{word}) should have only ACGT or N letters, but have '" + letter + "' letter");
      }
    }
    return sum;
  }

  public double score(Sequence word, BackgroundModel background) throws IllegalArgumentException {
    return score(word.sequence, background);
  }

  public double score(Sequence word) throws IllegalArgumentException {
    return score(word.sequence);
  }

  public double best_score() {
    return best_suffix(0);
  }

  public double worst_score() {
    return worst_suffix(0);
  }

  // best score of suffix s[i..l]
  public double best_suffix(int i) {
    double result = 0.0;
    for (int pos_index = i; pos_index < length(); ++pos_index) {
      result += ArrayExtensions.max(matrix[pos_index]);
    }
    return result;
  }

  double worst_suffix(int i) {
    double result = 0.0;
    for (int pos_index = i; pos_index < length(); ++pos_index) {
      result += ArrayExtensions.min(matrix[pos_index]);
    }
    return result;
  }

  public PWM discrete(Double rate) {
    if (rate == null) {
      return this;
    }
    double[][] mat_result;
    mat_result = new double[length()][];
    for (int i = 0; i < length(); ++i) {
      mat_result[i] = new double[4];
      for (int j = 0; j < 4; ++j) {
        mat_result[i][j] = ceil(matrix[i][j] * rate);
      }
    }
    return new PWM(mat_result, name);
  }
}
