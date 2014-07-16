package ru.autosome.perfectosape.model;

import gnu.trove.impl.unmodifiable.TUnmodifiableCharCharMap;
import gnu.trove.map.TCharCharMap;
import gnu.trove.map.hash.TCharCharHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import ru.autosome.commons.model.Position;

import java.util.ArrayList;

public class Sequence {
  private static final TCharSet allowedLetters = new TCharHashSet(new char[]{'A','C','G','T','a','c','g','t', 'n', 'N'});
  private static final TCharCharMap complements =
   new TUnmodifiableCharCharMap( new TCharCharHashMap(new char[]{'A','C','G','T','a','c','g','t', 'n', 'N'},
                                                      new char[]{'T','G','C','A','t','g','c','a', 'n', 'N'}) );

  final public String sequence;

  public Sequence(String sequence) {
    for(int i = 0; i < sequence.length(); ++i) {
      if ( !allowedLetters.containsAll(sequence.toCharArray()) ) {
        throw new IllegalArgumentException("Sequence '" + sequence + "' contains unallowed character (only A,C,G,T,N letters are allowed).");
      }
    }
    this.sequence = sequence;
  }

  public int length() {
    return sequence.length();
  }

  public Sequence reverse() {
    return new Sequence(new StringBuilder(sequence).reverse().toString());
  }

  public Sequence complement() {
    StringBuilder result = new StringBuilder(length());
    for (int i = 0; i < sequence.length(); ++i) {
      result.append(complements.get(sequence.charAt(i)));
    }
    return new Sequence(result.toString());
  }

  // works on direct strand
  public Sequence substring(int beginIndex, int endIndex) {
    return new Sequence(sequence.substring(beginIndex, endIndex));
  }

  // returns subsequence of given length on according strand, starting from given left boundary (on positive strand)
  public Sequence substring(Position left_boundary, int substring_length) {
    Sequence unorientedWord = substring(left_boundary.position, Math.min(left_boundary.position + substring_length, length()));
    if (left_boundary.directStrand) {
      return unorientedWord ;
    } else {
      return unorientedWord.reverse().complement();
    }
  }

  // (upstream) positions at which subsequence of given length can start
  public ArrayList<Position> subsequence_positions(int subsequence_length) {
    return Position.positions_between(0, length(), subsequence_length);
  }

  @Override
  public String toString() {
    return sequence;
  }
}
