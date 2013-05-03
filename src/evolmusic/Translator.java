package evolmusic;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Interface for converting between bit strings and JFugue-compatible strings.
 * 
 * @author Kim Merrill
 * 
 *  * JFugue string:
 * "+" indicates that notes should be played together and "_" indicates
 * that notes should be played in sequence.
 * 
 * C5w+D5w+F5w+A5w+A6i_B6i_G6i_A6i_F6i_D6i_C6i_B5i
 * D5w+F5w+G5w+B5w+G#5i_B5i_G5i_F5i_G5i_G#5i_E5i_C5i
 * 
 * 
 * Bit string (dimension 424):
 * The first number is a user-added rating. 0.0 if unknown.
 * The next 12 bits are a 1-hot encoding of the first measure's chord, followed
 * by 12 bits for the second measure's chord (harmonic scale starting with C).
 * The last 400 bits are a 1-hot encoding of the melody. Each 25-bits
 * represents a note with the first bit indicating whether the note is
 * tied over and the remaining 24 bits a 2-octave harmonic scale.
 * 
 * 0.9 1 0 1 0 0 1 0 0 0 1 0 0 0 0 1 0 0 1 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0
 *
 */
public interface Translator {

	public static final String SPACE = " ";
	public static final String PLUS = "+";
	public static final String REST = "R";
	public static final String EIGHTH_NOTE = "i";
	public static final String WHOLE_NOTE = "w";
	public static final int NOTE_BITS = 25; // number of bits representing a note
	public static final String[] OCTAVES = {"5", "6"}; // possible octaves
	public static final ArrayList<String> NOTES = new ArrayList<String>(Arrays.asList(
			new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}));

	/**
	 * Translates between bit strings and JFugue-compatible melody strings.
	 * 
	 * @param originalRepr original song representation
	 * @return string representing song translation to opposite format
	 */
	public String translate(String originalRepr);
}
