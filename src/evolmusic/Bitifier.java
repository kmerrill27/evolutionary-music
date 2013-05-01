package evolmusic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;


public class Bitifier {

	private static final int CHORD_LENGTH = 12;
	private static final int NOTE_LENGTH = 25;
	private static final String SPACE = " ";
	private static final String PLUS = "+";
	private static final String USER_RATING = "0.0 ";
	private static final String[] OCTAVES = {"5", "6"};
	private static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G",
		"G#", "A", "A#", "B"};
	private static final ArrayList<String> PITCHES = new ArrayList<String>(Arrays.asList(NOTES));

	public Bitifier() {		
	}

	public String bitify(String song) {
		String[] measures = song.split(SPACE);
		String[] measure1 = splitChord(measures[0]);
		String[] measure2 = splitChord(measures[1]);
		return USER_RATING + formatChord(measure1[0]) + formatChord(measure2[0])
				+ formatMelody(measure1[1]) + formatMelody(measure2[1]);
	}

	private String[] splitChord(String measure) {
		int index = measure.lastIndexOf(PLUS);
		String chord = measure.substring(0, index+1);
		String melody = measure.substring(index+1, measure.length());
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
			bitVector.set(PITCHES.indexOf(note));
		}
		return makeBitString(bitVector, CHORD_LENGTH);
	}

	private String bitifyNote(String note) {
		int offset = 1;
		BitSet bitVector = new BitSet(NOTE_LENGTH);

		int durIndex = note.indexOf("i");
		String duration = note.substring(durIndex, note.length());
		String octave = note.substring(durIndex-1, durIndex);
		String pitch = note.substring(0, durIndex-1);

		if (octave.equals(OCTAVES[1])) {
			offset += 12;
		}
		bitVector.set(PITCHES.indexOf(pitch) + offset);
		String noteBitString = makeBitString(bitVector, NOTE_LENGTH);
		String bitString = noteBitString;
		for (int i = 1; i < duration.length(); i++) {
			bitString += "1" + noteBitString.substring(1, noteBitString.length());
		}
		return bitString;
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
		String melody = "C5w+D5w+E5w+F5w+A5w+E6i_B5i_A#5iii_G5i_A5i_D#5i C#5w+D5w+F5w+G5w+G#5w+B5w+D5i_F5i_C5ii_C#5ii_G5i_A5i";
		System.out.println(melody);
		Bitifier bitifier = new Bitifier();
		String bitString = bitifier.bitify(melody);
		Translator translator = new Translator();
		melody = translator.translate(bitString);
		System.out.println(melody);
		MelodyPlayer player = new MelodyPlayer();
		player.play(melody);
	}
}
