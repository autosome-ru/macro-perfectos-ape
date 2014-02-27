package ru.autosome.perfectosape.backgroundModels;

public interface DiBackgroundModel {
  public double[] probability();

  public double probability(int index);

  public double[] count();

  public double count(int index);

  public double volume(); // 1 for probability model, 4 for wordwise model

  @Override
  public String toString();

  public boolean is_wordwise();

  public double mean_value(double[] values);

  public double mean_square_value(double[] values);
}
