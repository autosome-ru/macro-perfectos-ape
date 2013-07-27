package ru.autosome.jMacroape;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: MSI
 * Date: 7/24/13
 * Time: 9:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
  public static void main(String args[]) {
    double mat[][] = {{1,2,3,4},{5,6,7,8}};
    double background[] = {1,1,1,1};
    PM pm = new PM(mat,background,"My matrix");
    PWM pwm = new PWM(pm);
    String str = "12,34, 56.7,8.90";
    StringTokenizer parser = new StringTokenizer(str);
    try{
      while(true){
        String token = parser.nextToken(",");
        System.out.println(Float.valueOf(token));
      }
    } catch (NoSuchElementException e) {
      System.out.println("Exception: "+e);
    }
  }
}
