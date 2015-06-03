package ru.autosome.commons.motifModel.types;

import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPPM;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.mono.PWM;

public enum DataModelExpanded {
  PCM {
    @Override
    public ru.autosome.commons.motifModel.mono.PCM createModel(double[][] matrix) { return new PCM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.mono.PCM> createModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.mono.PCM>(createModel(namedMatrix.getObject()), namedMatrix.getName());
    }

  },

  PPM {
    @Override
    public ru.autosome.commons.motifModel.mono.PPM createModel(double[][] matrix) { return new PPM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.mono.PPM> createModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.mono.PPM>(createModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  },

  PWM {
    @Override
    public ru.autosome.commons.motifModel.mono.PWM createModel(double[][] matrix) { return new PWM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.mono.PWM> createModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.mono.PWM>(createModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  },

  // Dinucleotide
  DiPCM {
    @Override
    public ru.autosome.commons.motifModel.di.DiPCM createModel(double[][] matrix) { return new DiPCM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.di.DiPCM> createModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.di.DiPCM>(createModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  },

  DiPPM {
    @Override
    public ru.autosome.commons.motifModel.di.DiPPM createModel(double[][] matrix) { return new DiPPM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.di.DiPPM> createModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.di.DiPPM>(createModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  },

  DiPWM {
    @Override
    public ru.autosome.commons.motifModel.di.DiPWM createModel(double[][] matrix) { return new DiPWM(matrix); }

    @Override
    public Named<ru.autosome.commons.motifModel.di.DiPWM> createModel(Named<double[][]> namedMatrix) {
      return new Named<ru.autosome.commons.motifModel.di.DiPWM>(createModel(namedMatrix.getObject()), namedMatrix.getName());
    }
  };

  public abstract Object createModel(double[][] matrix);
  public abstract Named createModel(Named<double[][]> namedMatrix);
}

