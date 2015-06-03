package ru.autosome.commons.motifModel.types;

import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPM;
import ru.autosome.commons.motifModel.di.DiPPM;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PM;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.mono.PWM;

public enum DataModel {
  PCM {
    @Override
    public ru.autosome.commons.motifModel.mono.PCM createMonoModel(double[][] matrix) { return new PCM(matrix); }
    @Override
    public ru.autosome.commons.motifModel.di.DiPCM createDiModel(double[][] matrix) { return new DiPCM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.mono.PCM> createMonoModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.mono.PCM>(createMonoModel(namedMatrix.getObject()), namedMatrix.getName());
    }
    @Override
    public Named<ru.autosome.commons.motifModel.di.DiPCM> createDiModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.di.DiPCM>(createDiModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  },

  PPM {
    @Override
    public ru.autosome.commons.motifModel.mono.PPM createMonoModel(double[][] matrix) { return new PPM(matrix); }
    @Override
    public ru.autosome.commons.motifModel.di.DiPPM createDiModel(double[][] matrix) { return new DiPPM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.mono.PPM> createMonoModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.mono.PPM>(createMonoModel(namedMatrix.getObject()), namedMatrix.getName());
    }
    @Override
    public Named<ru.autosome.commons.motifModel.di.DiPPM> createDiModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.di.DiPPM>(createDiModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  },

  PWM {
    @Override
    public ru.autosome.commons.motifModel.mono.PWM createMonoModel(double[][] matrix) { return new PWM(matrix); }
    @Override
    public ru.autosome.commons.motifModel.di.DiPWM createDiModel(double[][] matrix) { return new DiPWM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.mono.PWM> createMonoModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.mono.PWM>(createMonoModel(namedMatrix.getObject()), namedMatrix.getName());
    }
    @Override
    public Named<ru.autosome.commons.motifModel.di.DiPWM> createDiModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.di.DiPWM>(createDiModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  };

  public abstract PM createMonoModel(double[][] matrix);
  public abstract DiPM createDiModel(double[][] matrix);
  public abstract Named<? extends PM> createMonoModel(Named<double[][]> namedMatrix);
  public abstract Named<? extends DiPM> createDiModel(Named<double[][]> namedMatrix);


  public static DataModel fromString(String s) {
    if (s.toUpperCase().equals("PWM")) {
      return PWM;
    } else if (s.toUpperCase().equals("PCM")) {
      return PCM;
    } else if (s.toUpperCase().equals("PPM")) {
      return PPM;
    } else {
      throw new IllegalArgumentException("Unknown data model");
    }
  }

}
