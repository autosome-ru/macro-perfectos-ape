package ru.autosome.macroape;

import java.util.HashMap;

public class PCM2PWMConverter {
  public static PWM convert(PCM pcm, HashMap<String, Object> parameters) {
    HashMap<String, Object> default_parameters = new HashMap<String, Object>();
    default_parameters.put("pseudocount", Math.log(pcm.count()));
    default_parameters.put("probability", pcm.probabilities());
    for (String key: parameters.keySet()) {
      default_parameters.put(key, parameters.get(key));
    }
    parameters = default_parameters;
    double probability[] = (double[]) parameters.get("probability");
    double pseudocount = (Double) parameters.get("pseudocount");

    double new_matrix[][] = new double[pcm.matrix.length][];
    for (int i = 0; i < pcm.matrix.length; ++i) {
      new_matrix[i] = new double[4];
      for (int j = 0; j < 4; ++j) {
        new_matrix[i][j] = Math.log((pcm.matrix[i][j] + probability[j] * pseudocount) / (probability[j]*(pcm.count() + pseudocount)) );
      }
    }
    return new PWM(new_matrix, pcm.background, pcm.name);
  }

}
