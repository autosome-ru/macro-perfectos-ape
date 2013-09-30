package ru.autosome.macroape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class PCM extends PM {
  private PCM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
  }

  public double count() {
    double[] pos = matrix[0];
    return pos[0] + pos[1] + pos[2] + pos[3];
  }

  public PWM to_pwm(BackgroundModel background) {
    PCM2PWMConverter converter = new PCM2PWMConverter(this);
    converter.background = background;
    return converter.convert();
  }

  private static PCM new_from_text(ArrayList<String> input_lines) {
    PMParser matrix_parser = new PMParser(input_lines);

    double[][] matrix = matrix_parser.matrix();
    String name = matrix_parser.name();

    return new PCM(matrix, name);
  }

  public static PCM new_from_file(String filename) {
    try {
      InputStream reader = new FileInputStream(filename);
      return new_from_text(InputExtensions.readLinesFromInputStream(reader));
    } catch (FileNotFoundException err) {
      return null;
    }
  }

  public static PCM new_from_file(File file) {
    try {
      InputStream reader = new FileInputStream(file);
      return new_from_text(InputExtensions.readLinesFromInputStream(reader));
    } catch (FileNotFoundException err) {
      return null;
    }
  }

  public static PCM new_from_file_or_stdin(String filename) {
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
}
