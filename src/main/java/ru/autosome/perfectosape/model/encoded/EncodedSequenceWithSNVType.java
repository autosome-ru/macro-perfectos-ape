package ru.autosome.perfectosape.model.encoded;

import ru.autosome.commons.motifModel.HasLength;

public interface EncodedSequenceWithSNVType<EncodedSequenceType> extends HasLength {
  EncodedSequenceType sequenceVariant(int alleleNumber);
}
