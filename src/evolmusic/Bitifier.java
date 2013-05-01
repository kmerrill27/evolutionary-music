package evolmusic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Converts a JFugue-compatible melody string to a 1-hot encoding
 * of a melody.
 * 
 * @author Kim Merrill
 *
 */
public class Bitifier implements Translator {

	private static final int CHORD_LENGTH = 12; // number of bits in chord
	private static final String USER_RATING = "0.0 "; // default (unknown) user rating of song
	private static final ArrayList<String> PITCHES = new ArrayList<String>(Arrays.asList(NOTES));

	public Bitifier() {		
	}

	@Override
	public String translate(String song) {
		String[] measures = song.split(SPACE);
		String[] measure1 = splitChord(measures[0]);
		String[] measure2 = splitChord(measures[1]);
		return USER_RATING + formatChord(measure1[0]) + formatChord(measure2[0])
				+ formatMelody(measure1[1]) + formatMelody(measure2[1]);
	}

	private String[] splitChord(String measure) {
		String chord, melody;
		int index = measure.lastIndexOf(PLUS);
		if (index < 0) {
			chord = "";
			melody = measure;
		} else {
			chord = measure.substring(0, index+1);
			melody = measure.substring(index+1, measure.length());
		}
		return new String[]{chord, melody};
	}

	private String formatChord(String chordString) {
		chordString = chordString.replaceAll("5w", "");
		String[] notes = chordString.split("\\+");
		return bitifyChord(notes);
	}

	private String formatMelody(String melodyString) {
		String bitMelody = "";
		String[] notes = melodyString.split("_");
		for (String note : notes) {
			bitMelody += bitifyNote(note);
		}
		return bitMelody;
	}

	private String bitifyChord(String[] notes) {
		BitSet bitVector = new BitSet(CHORD_LENGTH);
		for (String note : notes) {
			// Check for no chord case
			if (!note.isEmpty()) {
				bitVector.set(PITCHES.indexOf(note));
			}
		}
		return makeBitString(bitVector, CHORD_LENGTH);
	}

	private String bitifyNote(String note) {
		int offset = 1;
		BitSet bitVector = new BitSet(NOTE_BITS);

		if(note.substring(0,1).equals(REST)) {
			return bitifyRest(note);
		}

		int durIndex = note.indexOf("i");
		String duration = note.substring(durIndex, note.length());
		String octave = note.substring(durIndex-1, durIndex);
		String pitch = note.substring(0, durIndex-1);

		if (octave.equals(OCTAVES[1])) {
			offset += 12;
		}
		bitVector.set(PITCHES.indexOf(pitch) + offset);
		String bitString = makeBitString(bitVector, NOTE_BITS);
		
		return bitString + bitifyTiedOver(bitString, duration.length());
	}

	private String bitifyTiedOver(String noteBitString, int duration) {
		String bitString = "";
		noteBitString = "1" + noteBitString.substring(1, noteBitString.length());
		for (int i = 1; i < duration; i++) {
			bitString += noteBitString;
		}
		return bitString;
	}

	private String bitifyRest(String note) {
		String duration = note.substring(1, note.length());
		String bitString = makeBitString(new BitSet(NOTE_BITS), NOTE_BITS);
		return bitString + bitifyTiedOver(bitString, duration.length());
	}

	private String makeBitString(BitSet bitVector, int length) {
		String bitString = "";
		for (int i = 0; i < length; i++) {
			boolean bit = bitVector.get(i);
			bitString +=  bit ? 1 : 0;
		}
		return bitString.trim();
	}

	public static void main(String[] args) {
		MelodyPlayer player = new MelodyPlayer();
		Notationizer notationizer = new Notationizer();
		Bitifier bitifier = new Bitifier();

		//String bitString = "0.9 1 0 1 0 0 1 0 0 0 1 0 0 0 0 1 0 0 1 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.6 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.1 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0";
		String bitString = "0.1 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0";

		// Test given melody
		String melody = notationizer.translate(bitString);
		//player.play(melody);
		bitString = bitifier.translate(melody);
		melody = notationizer.translate(bitString);
		//player.play(melody);

		// Test random melody
		melody = new RandomMelody(2, 4).getMelodyString();
		System.out.println(melody);
		player.play(melody);
		bitString = bitifier.translate(melody);
		melody = notationizer.translate(bitString);
		System.out.println(melody);
		player.play(melody);
	}

}
