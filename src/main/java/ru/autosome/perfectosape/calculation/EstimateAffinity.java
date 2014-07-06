package ru.autosome.perfectosape.calculation;

import ru.autosome.ape.model.exception.HashOverflowException;

interface EstimateAffinity {
  double affinity() throws HashOverflowException;
}