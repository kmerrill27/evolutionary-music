package evolmusic;

import java.io.File;
import java.io.IOException;

import org.jfugue.Pattern;
import org.jfugue.Player;

/**
 * 
 * Plays a melody specified as a String in JFugue format.
 * 
 * @author Kim Merrill
 *
 */
public class MelodyPlayer {

	Player player;

	public MelodyPlayer() {
		player = new Player();
	}

	/**
	 * Plays given melody.
	 * 
	 * @pre melody is a valid JFugue string
	 * 
	 * @param melody JFugue string representing melody
	 */
	public void play(String melody) {
		Pattern pattern = new Pattern(melody);
		player.play(pattern);
	}

	/**
	 * Saves melody to midi file.
	 * 
	 * @param melody JFugue string representing melody
	 * @param filename midi file to save to (.mid extension)
	 * @return true if save was successful
	 */
	public boolean save(String melody, String filename) {
		File midiFile = new File(filename);
		try {
			player.saveMidi(melody, midiFile);
			return true;
		} catch (IOException e) {
			System.out.println("Error writing melody to file.");
			return false;
		}
	}
}
