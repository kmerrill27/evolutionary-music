package evolmusic;

import org.jfugue.*; 

/**
 * 
 * Plays a melody specified as a String in JFugue format.
 * 
 * @author Kim Merrill
 *
 */
public class MelodyPlayer {

	public MelodyPlayer() {
	}

	public void play(String melody) {
		Player player = new Player();
		Pattern pattern = new Pattern(melody);
		player.play(pattern);
	}
}
