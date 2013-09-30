package ru.autosome.macroape;

class MathExtensions {
  static double inverf(double x) {
    int sign = x < 0 ? -1 : 1;
    x = Math.abs(x);
    double a = 8 / (3 * Math.PI) * (Math.PI - 3) / (4 - Math.PI);
    double tmp = (2 / (Math.PI * a) + (Math.log(1 - x * x)) / 2);
    double part0 = tmp * tmp;
    double part = -2 / (Math.PI * a) - Math.log(1 - x * x) / 2 + Math.sqrt(-1 / a * Math.log(1 - x * x) + part0);
    return sign * Math.sqrt(part);
  }
}