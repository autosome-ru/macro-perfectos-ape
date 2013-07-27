/**
 * Created with IntelliJ IDEA.
 * User: MSI
 * Date: 7/23/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
package ru.autosome.jMacroape;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.ceil;

public class PM {
  double[][] matrix;
  double[] background;
  //private double[] probabilities;
  String name;

  public PM(PM pm) throws IllegalArgumentException {
    this.matrix = pm.matrix;
    this.background = pm.background;
    //this.probabilities = pm.probabilities;
  }
  public PM(double[][] matrix, double[] background, String name) throws IllegalArgumentException {
    for(double[] pos: matrix) {
      if (pos.length != 4) {
        throw new IllegalArgumentException("Matrix must have 4 elements in each position");
      }
    }
    if (background.length != 4) {
      throw new IllegalArgumentException("Background should contain exactly 4 nucleotides");
    }
    this.matrix = matrix;
    this.background = background;
    //this.probabilities = calculate_probabilities();
    this.name = name;
  }

  public int length() {
    return matrix.length;
  }

  public double[] probabilities() {
    double sum = ArrayExtensions.sum(background);
    double[] probabilities = new double[4];
    for (int i = 0; i < 4; ++i) {
      probabilities[i] = background[i] / sum;
    }
    return probabilities;
  }
  public String toString() {
    String result;
    result = name + "\n";
    for (double[] pos: matrix) {
      result = result + pos[0] + "\t" + pos[1] + "\t" + pos[2] + "\t" + pos[3] + "\n";
    }
    return result;
  }

  public PM reverse_complement() {
    double[][] mat_result;
    mat_result = new double[length()][];
    for (int i = 0; i < length(); ++i) {
      mat_result[i] = new double[4];
      for (int j = 0; j < 4; ++j) {
        mat_result[i][j] = matrix[length() - 1  - i][4 - 1 - j];
      }
    }
    return new PM(mat_result, background, name);
  }

  double[] zero_column() {
    double[] result = {0,0,0,0};
    return result;
  }

  public PM left_augment(int n) {
    double[][] mat_result;
    mat_result = new double[length() + n][];
    for (int i = 0; i < n; ++i) {
      mat_result[i] = zero_column();
    }
    for (int i = 0; i < length(); ++i) {
      mat_result[n + i] = matrix[i].clone();
    }
    return new PM(mat_result, background, name);
  }
  public PM right_augment(int n) {
    double[][] mat_result;
    mat_result = new double[length() + n][];
    for (int i = 0; i < length(); ++i) {
      mat_result[i] =  matrix[i].clone();
    }
    for (int i = 0; i < n; ++i) {
      mat_result[length() + i] = zero_column();
    }
    return new PM(mat_result, background, name);
  }
  public PM discrete(double rate) {
    double[][] mat_result;
    mat_result = new double[length()][];
    for (int i = 0; i < length(); ++i) {
      mat_result[i] = new double[4];
      for (int j = 0; j < 4; ++j){
        mat_result[i][j] = ceil(matrix[i][j] * rate);
      }
    }
    return new PM(mat_result, background, name);
  }

  public double vocabulary_volume() {
    return Math.pow(ArrayExtensions.sum(background), length());
  }

  public static HashMap<Character, Integer> index_by_letter() {
    HashMap<Character, Integer> result = new HashMap<Character,Integer>();
    result.put('A', 0);
    result.put('C', 1);
    result.put('G', 2);
    result.put('T', 3);
    return result;
  }
}