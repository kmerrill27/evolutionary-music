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
	
	private static String[] pitches = {"A", "B", "C", "D", "E", "F", "G", REST};
	private static String[] octaves = {"4", "5", "6"};
	private static String[] durations = {"w", "h", "q", "i", "s"};
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
	 * Notes may be whole, half, quarter, eighth, or sixteenth.
	 */
	private void populateDurations() {
		durationMap.put("w", 1.0);
		durationMap.put("h", .5);
		durationMap.put("q", .25);
		durationMap.put("i", .125);
		durationMap.put("s", .0625);
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
			tempMelody += generateMeasure(bpm);
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
		while (bpm != 0) {
			dur = durations[random.nextInt(durations.length)];
			// If selected duration will exceed the length of the measure, try again.
			if (bpm - durationMap.get(dur) < 0) {
				continue;
			} else {
				bpm -= durationMap.get(dur);
				pitch = pitches[random.nextInt(pitches.length)];
				// If rest is selected, octave is irrelevant.
				if (pitch != REST) {
					pitch += octaves[random.nextInt(octaves.length)];
				}
				tempMeasure += pitch + dur + " ";
			}
		}
		return tempMeasure;
	}

	public static void main(String args[]) {
		RandomMelody melody = new RandomMelody(4, 4);
		MelodyPlayer player = new MelodyPlayer(melody.getMelodyString());
		System.out.println(melody.getMelodyString());
		player.play();
	}
}
