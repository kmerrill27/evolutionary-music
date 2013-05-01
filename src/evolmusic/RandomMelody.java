package evolmusic;

import java.util.HashMap;
import java.util.Random;

/**
 * 
 * Generates a simple random melody by randomly selecting pitch, octave, and duration
 * for its contained notes.
 * 
 * @author Kim Merrill
 *
 */
public class RandomMelody {

	private static final String REST = "R";
	private static final int MAX_CHORD_NOTES = 7;
	
	private static String[] pitches = {"C", "C#", "D", "D#", "E", "F", "F#", "G",
		"G#", "A", "A#", "B"};
	private static final String[] OCTAVES = {"5", "6"};
	private static String[] DURATIONS = {"i", "ii", "iii", "iiii", "iiiii", "iiiiii",
		"iiiiiii", "iiiiiiii"};
	private static HashMap<String, Double> durationMap = new HashMap<String, Double>();
	private static Random random = new Random();

	private String melody;

	public RandomMelody(int numMeasures, int bpm) {
		this.populateDurations();
		this.melody = generateMelody(numMeasures, bpm);
	}

	public String getMelodyString() {
		return this.melody;
	}

	/**
	 * Note durations must be divisible into eighth notes.
	 */
	private void populateDurations() {
		durationMap.put("iiiiiiii", 1.0);
		durationMap.put("iiiiiii", .875);
		durationMap.put("iiiiii", .75);
		durationMap.put("iiiii", .625);
		durationMap.put("iiii", .5);
		durationMap.put("iii", .375);
		durationMap.put("ii", .25);
		durationMap.put("i", .125);
	}

	/**
	 * A melody specifies pitch, octave, and duration of each note.
	 * 
	 * @param numMeasures the number of measures in the melody
	 * @param bpm the number of beats per measure
	 * @return a string representation of a melody
	 */
	private String generateMelody(int numMeasures, int bpm) {
		String tempMelody = "";
		for (int i=0; i < numMeasures; i++) {
			tempMelody += generateChord() + generateMeasure(bpm) + " ";
		}
		return tempMelody.trim();
	}

	/**
	 * A measure is a component of a melody that specifies
	 * the pitch, octave, and duration of each of its contained notes.
	 * 
	 * @param bpm the number of beats per measure
	 * @return a string representation of a single measure
	 */
	private String generateMeasure(double bpm) {
		String tempMeasure = "";
		String dur, pitch;
		double beatsLeft = bpm;
		while (beatsLeft != 0) {
			dur = DURATIONS[random.nextInt(DURATIONS.length)];
			// If selected duration will exceed the length of the measure, try again.
			if (beatsLeft - durationMap.get(dur)*bpm < 0) {
				continue;
			} else {
				beatsLeft -= durationMap.get(dur)*bpm;
				pitch = pitches[random.nextInt(pitches.length)];
				// If rest is selected, octave is irrelevant.
				if (pitch != REST) {
					pitch += OCTAVES[random.nextInt(OCTAVES.length)];
				}
				tempMeasure += pitch + dur + "_";
			}
		}
		// Remove trailing "_"
		return tempMeasure.substring(0, tempMeasure.length()-1);
	}

	/**
	 * A chord is a grouping of 0-4 notes, played for the duration
	 * of a measure.
	 * 
	 * @return a string representation of a chord
	 */
	private String generateChord() {
		int numNotes = random.nextInt(MAX_CHORD_NOTES);
		String tempChord = "";
		if (numNotes == 0) {
			return "";
		}
		for (int i=0; i < numNotes; i++) {
			tempChord += pitches[random.nextInt(pitches.length)] + OCTAVES[0] + "w" + "+";
		}
		return tempChord;
	}

	public static void main(String args[]) {
		RandomMelody melody = new RandomMelody(2, 4);
		MelodyPlayer player = new MelodyPlayer();
		System.out.println(melody.getMelodyString());
		player.play(melody.getMelodyString());
	}
}
