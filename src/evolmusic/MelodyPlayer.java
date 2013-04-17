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

	private String melody;

	public MelodyPlayer(String melody) {
		this.melody = melody;
	}

	public void play() {
		Player player = new Player();
		Pattern pattern = new Pattern(melody);
		player.play(pattern);
	}

	public static void main(String args[]) {
		MelodyPlayer test = new MelodyPlayer("C6i D6h Rw E5h F4h G5w A5q B6w");
		test.play();
	}
}
