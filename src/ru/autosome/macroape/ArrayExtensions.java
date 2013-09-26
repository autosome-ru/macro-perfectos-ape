package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ArrayExtensions {
    public static double max(double... array) throws IllegalArgumentException {
        if (array.length == 0) {
            throw new IllegalArgumentException("Can't calculate maximum of empty array");
        }
        double result = array[0];
        for (double pos : array) {
            result = Math.max(result, pos);
        }
        return result;
    }

    public static double min(double... array) throws IllegalArgumentException {
        if (array.length == 0) {
            throw new IllegalArgumentException("Can't calculate minimum of empty array");
        }
        double result = array[0];
        for (double pos : array) {
            result = Math.min(result, pos);
        }
        return result;
    }

    public static double sum(double... array) {
        double result = 0.0;
        for (double el : array) {
            result += el;
        }
        return result;
    }

    public static <T> T[] reverse(T[] array) {
        T[] result = array.clone();
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }
    public static double[] reverse(double[] array) {
        double[] result = array.clone();
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }

    public static double[] partial_sums(double array[], double initial) {
        double result[] = new double[array.length];
        double sums = initial;
        for (int i = 0; i < array.length; ++i) {
            sums += array[i];
            result[i] = sums;
        }
        return result;
    }

    public static <T extends Object> boolean contain(Iterable<T> list, T obj) {
        for (T el : list) {
            if (el == obj) return true;
        }
        return false;
    }

    public static <T extends Object> boolean intersect(Iterable<T> list_1, Iterable<T> list_2) {
        Set<T> set_1 = new HashSet<T>();
        for (T arg_1 : list_1) set_1.add(arg_1);

        for (T arg_2 : list_2) {
            if (set_1.contains(arg_2)) return true;
        }
        return false;
    }

    public static Integer indexOf(double el, double[] list) {
        for (int i = 0; i < list.length; ++i) {
            if (list[i] == el) return i;
        }
        return null;
    }

    public static double[] toPrimitiveArray(Double[] wrappedArray) {
        double[] array = new double[wrappedArray.length];
        for (int i = 0; i < wrappedArray.length; i++)
            array[i] = wrappedArray[i];
        return array;
    }

    public static double[] toPrimitiveArray(ArrayList<Double> wrappedArray) {
        double[] array = new double[wrappedArray.size()];
        for (int i = 0; i < wrappedArray.size(); i++)
            array[i] = wrappedArray.get(i);
        return array;
    }
}
