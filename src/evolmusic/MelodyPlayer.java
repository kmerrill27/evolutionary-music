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

	public static void main(String args[]) {
		MelodyPlayer test = new MelodyPlayer();
		test.play("C6i D6h Rw E5h F4h G5w A5q B6w");
	}
}
