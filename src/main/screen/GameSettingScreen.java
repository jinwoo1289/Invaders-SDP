package screen;

import engine.*;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the game setting screen with all methods public.
 */
public class GameSettingScreen extends Screen {
	private static GameSettingScreen instance;

	private static final int SELECTION_TIME = 200;
	private static final int NAME_LIMIT = 4;

	private static String name1 = "P1";
	private static String name2 = "P2";
	private static boolean isMultiplayer = false;

	private int difficultyLevel = 1;
	private int selectedRow = 0;

	private final Cooldown selectionCooldown;
	private final SoundManager soundManager = SoundManager.getInstance();
	private final Map<Integer, Runnable> keyActions = new HashMap<>();

	private enum RowAction {
		MULTIPLAYER, DIFFICULTY, START;

		/**
		 * Performs the associated action for the selected row.
		 *
		 * @param screen The instance of the GameSettingScreen.
		 */
		public void perform(GameSettingScreen screen) {
			switch (this) {
				case MULTIPLAYER -> screen.handleMultiplayer();
				case DIFFICULTY -> screen.handleDifficulty();
				case START -> screen.handleStart();
			}
		}
	}

	private final RowAction[] rowActions = {RowAction.MULTIPLAYER, RowAction.DIFFICULTY, RowAction.START};

	/**
	 * Constructs a new GameSettingScreen with the specified dimensions and frame rate.
	 *
	 * @param width  The width of the screen.
	 * @param height The height of the screen.
	 * @param fps    The frame rate of the screen.
	 */
	public GameSettingScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();
		initializeKeyActions();
	}

	/**
	 * Initializes key actions for navigation and exiting.
	 */
	public void initializeKeyActions() {
		keyActions.put(KeyEvent.VK_UP, () -> navigateRows(-1));
		keyActions.put(KeyEvent.VK_DOWN, () -> navigateRows(1));
		keyActions.put(KeyEvent.VK_ESCAPE, this::exitToMainMenu);
	}

	/**
	 * Runs the screen logic until completion.
	 *
	 * @return The return code for the screen.
	 */
	public final int run() {
		super.run();
		return this.returnCode;
	}

	/**
	 * Updates the game setting screen elements and processes input.
	 */
	public void update() {
		super.update();
		draw();

		if (this.inputDelay.checkFinished() && this.selectionCooldown.checkFinished()) {
			keyActions.keySet().stream()
					.filter(inputManager::isKeyDown)
					.forEach(key -> keyActions.get(key).run());
			rowActions[selectedRow].perform(this);
		}
	}

	/**
	 * Navigates the selection rows in the specified direction.
	 *
	 * @param direction The direction to navigate (-1 for up, 1 for down).
	 */
	public void navigateRows(int direction) {
		this.selectedRow = (this.selectedRow + direction + rowActions.length) % rowActions.length;
		resetSelectionCooldownWithSound(Sound.MENU_MOVE);
	}

	/**
	 * Handles the multiplayer configuration, including toggling and name input.
	 */
	public void handleMultiplayer() {
		handleKeyWithCondition(KeyEvent.VK_LEFT, () -> isMultiplayer = false);
		handleKeyWithCondition(KeyEvent.VK_RIGHT, () -> isMultiplayer = true);
		handleKeyWithCondition(KeyEvent.VK_BACK_SPACE, this::deleteName);
		handleNameInput();
	}

	/**
	 * Handles difficulty level adjustments.
	 */
	public void handleDifficulty() {
		handleKeyWithCondition(KeyEvent.VK_LEFT, () -> difficultyLevel = Math.max(0, difficultyLevel - 1));
		handleKeyWithCondition(KeyEvent.VK_RIGHT, () -> difficultyLevel = Math.min(2, difficultyLevel + 1));
	}

	/**
	 * Handles the start action, initiating the game based on the current settings.
	 */
	public void handleStart() {
		handleKeyWithCondition(KeyEvent.VK_SPACE, () -> {
			this.returnCode = isMultiplayer ? 8 : 2;
			this.isRunning = false;
			soundManager.playSound(Sound.MENU_CLICK);
		});
	}

	/**
	 * Executes the specified action if the key is pressed.
	 *
	 * @param keyCode The key code to check.
	 * @param action  The action to perform if the key is pressed.
	 */
	public void handleKeyWithCondition(int keyCode, Runnable action) {
		if (inputManager.isKeyDown(keyCode)) {
			action.run();
			resetSelectionCooldownWithSound(Sound.MENU_MOVE);
		}
	}

	/**
	 * Deletes the last character of the player's name based on the current mode.
	 */
	public void deleteName() {
		String name = isMultiplayer ? name2 : name1;
		if (!name.isEmpty()) {
			if (isMultiplayer) name2 = name2.substring(0, name2.length() - 1);
			else name1 = name1.substring(0, name1.length() - 1);
			resetSelectionCooldownWithSound(Sound.MENU_TYPING);
		}
	}

	/**
	 * Handles name input for the players.
	 */
	public void handleNameInput() {
		for (int keyCode = KeyEvent.VK_A; keyCode <= KeyEvent.VK_Z; keyCode++) {
			if (inputManager.isKeyDown(keyCode)) {
				String currentName = isMultiplayer ? name2 : name1;
				if (currentName.length() < NAME_LIMIT) {
					if (isMultiplayer) name2 += (char) keyCode;
					else name1 += (char) keyCode;
					resetSelectionCooldownWithSound(Sound.MENU_TYPING);
				}
			}
		}
	}

	/**
	 * Exits to the main menu.
	 */
	public void exitToMainMenu() {
		this.returnCode = 1;
		this.isRunning = false;
		soundManager.playSound(Sound.MENU_BACK);
	}

	/**
	 * Resets the selection cooldown and plays the specified sound.
	 *
	 * @param sound The sound to play.
	 */
	public void resetSelectionCooldownWithSound(Sound sound) {
		this.selectionCooldown.reset();
		soundManager.playSound(sound);
	}

	/**
	 * Draws the game setting screen elements.
	 */
	public void draw() {
		drawManager.initDrawing(this);
		drawManager.drawGameSetting(this);
		drawManager.drawGameSettingRow(this, this.selectedRow);
		drawManager.drawGameSettingElements(this, this.selectedRow, isMultiplayer, name1, name2, this.difficultyLevel);
		drawManager.completeDrawing(this);
		Core.setLevelSetting(this.difficultyLevel);
	}

	/**
	 * Retrieves the singleton instance of GameSettingScreen.
	 *
	 * @return The singleton instance.
	 */
	public static GameSettingScreen getInstance() {
		if (instance == null) {
			instance = new GameSettingScreen(0, 0, 0);
		}
		return instance;
	}

	/**
	 * Checks if multiplayer mode is enabled.
	 *
	 * @return true if multiplayer mode is enabled, false otherwise.
	 */
	public static boolean getMultiPlay() {
		return isMultiplayer;
	}

	/**
	 * Retrieves the name of the specified player.
	 *
	 * @param playerNumber The player number (0 for Player 1, 1 for Player 2).
	 * @return The name of the specified player.
	 */
	public static String getName(int playerNumber) {
		return playerNumber == 0 ? name1 : name2;
	}
}
