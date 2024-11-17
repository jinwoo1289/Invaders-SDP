package engine;

/**
 * Implements an object that stores a single game's difficulty settings.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameSettings {
	private int difficulty;
	/** Width of the level's enemy formation. */
	private int formationWidth;
	/** Height of the level's enemy formation. */
	private int formationHeight;
	/** Speed of the enemies, function of the remaining number. */
	private int baseSpeed;
	/** Frequency of enemy shootings, +/- 30%. */
	private int shootingFrecuency;

	/**
	 * Constructor.
	 * 
	 * @param formationWidth
	 *            Width of the level's enemy formation.
	 * @param formationHeight
	 *            Height of the level's enemy formation.
	 * @param baseSpeed
	 *            Speed of the enemies.
	 * @param shootingFrequency
	 *            Frecuen
	 *            cy of enemy shootings, +/- 30%.
	 */
	public GameSettings(final int formationWidth, final int formationHeight,
			final int baseSpeed, final int shootingFrequency) { // fix typo
		this.formationWidth = formationWidth;
		this.formationHeight = formationHeight;
		this.baseSpeed = baseSpeed;
		this.shootingFrecuency = shootingFrequency;
	}

	public GameSettings(GameSettings gameSettings) { // fix typo
		this.formationWidth = gameSettings.formationWidth;
		this.formationHeight = gameSettings.formationHeight;
		this.baseSpeed = gameSettings.baseSpeed;
		this.shootingFrecuency = gameSettings.shootingFrecuency;
	}

	/**
	 * @return the formationWidth
	 */
	public final int getFormationWidth() {
		return formationWidth;
	}

	/**
	 * @return the formationHeight
	 */
	public final int getFormationHeight() {
		return formationHeight;
	}

	/**
	 * @return the baseSpeed
	 */
	public final int getBaseSpeed() {
		return baseSpeed;
	}

	/**
	 * @return the shootingFrecuency
	 */
	public final int getShootingFrecuency() {
		return shootingFrecuency;
	}

	/**
	 *
	 * @param formationWidth control Enemy width
	 * @param formationHeight control Enemy height
	 * @param baseSpeed control Enemy speed
	 * @param shootingFrecuency control Enemy shooting Frequency
	 * @param level Level
	 * @param difficulty set difficulty
	 * @return return type GameSettings
	 */
	public GameSettings LevelSettings(int formationWidth, int formationHeight,
									  int baseSpeed, int shootingFrecuency, int level, int difficulty) {
		this.difficulty = difficulty;

		// Adjust formation dimensions based on level and difficulty
		if ((difficulty == 0 && level % 3 == 0 && level < 5) || (level % 2 == 0 && level >= 5)) {
			int increment = (level >= 5 && difficulty == 2) ? 2 : 1;
			if (formationWidth == formationHeight) {
				formationWidth = Math.min(14, formationWidth + increment);
			} else {
				formationHeight = Math.min(10, formationHeight + increment);
			}
		}

		// Adjust base speed based on level and difficulty
		int speedDecrement = switch (difficulty) {
			case 0 -> 10; // Easy
			case 1 -> (level >= 5 ? 20 : 10); // Medium
			case 2 -> 20; // Hard
			default -> 0; // This case should not occur, but set to 0 for safety
		};
		baseSpeed = Math.max(-150, baseSpeed - speedDecrement);

		// Adjust shooting frequency based on level and difficulty
		int frequencyDecrement = switch (difficulty) {
			case 0 -> 100; // Easy
			case 1 -> (level >= 5 ? 300 : 200); // Medium
			case 2 -> (level >= 5 ? 400 : 300); // Hard
			default -> 0; // This case should not occur, but set to 0 for safety
		};
		shootingFrecuency = Math.max(100, shootingFrecuency - frequencyDecrement);

		// Return the adjusted game settings
		return new GameSettings(formationWidth, formationHeight, baseSpeed, shootingFrecuency);
	}

	/**
	 * @return difficulty
	 */
	public int getDifficulty() {
		return difficulty;
	}

}
