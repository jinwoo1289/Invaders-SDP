package screen;

import engine.*;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the game setting screen.
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

		public void perform(GameSettingScreen screen) {
			switch (this) {
				case MULTIPLAYER -> screen.handleMultiplayer();
				case DIFFICULTY -> screen.handleDifficulty();
				case START -> screen.handleStart();
			}
		}
	}

	private final RowAction[] rowActions = {RowAction.MULTIPLAYER, RowAction.DIFFICULTY, RowAction.START};

	public GameSettingScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();
		initializeKeyActions();
	}

	private void initializeKeyActions() {
		keyActions.put(KeyEvent.VK_UP, () -> navigateRows(-1));
		keyActions.put(KeyEvent.VK_DOWN, () -> navigateRows(1));
		keyActions.put(KeyEvent.VK_ESCAPE, this::exitToMainMenu);
	}

	public final int run() {
		super.run();
		return this.returnCode;
	}

	protected final void update() {
		super.update();
		draw();

		if (this.inputDelay.checkFinished() && this.selectionCooldown.checkFinished()) {
			keyActions.keySet().stream()
					.filter(inputManager::isKeyDown)
					.forEach(key -> keyActions.get(key).run());
			rowActions[selectedRow].perform(this);
		}
	}

	private void navigateRows(int direction) {
		this.selectedRow = (this.selectedRow + direction + rowActions.length) % rowActions.length;
		resetSelectionCooldownWithSound(Sound.MENU_MOVE);
	}

	private void handleMultiplayer() {
		handleKeyWithCondition(KeyEvent.VK_LEFT, () -> isMultiplayer = false);
		handleKeyWithCondition(KeyEvent.VK_RIGHT, () -> isMultiplayer = true);
		handleKeyWithCondition(KeyEvent.VK_BACK_SPACE, this::deleteName);
		handleNameInput();
	}

	private void handleDifficulty() {
		handleKeyWithCondition(KeyEvent.VK_LEFT, () -> difficultyLevel = Math.max(0, difficultyLevel - 1));
		handleKeyWithCondition(KeyEvent.VK_RIGHT, () -> difficultyLevel = Math.min(2, difficultyLevel + 1));
	}

	private void handleStart() {
		handleKeyWithCondition(KeyEvent.VK_SPACE, () -> {
			this.returnCode = isMultiplayer ? 8 : 2;
			this.isRunning = false;
			soundManager.playSound(Sound.MENU_CLICK);
		});
	}

	private void handleKeyWithCondition(int keyCode, Runnable action) {
		if (inputManager.isKeyDown(keyCode)) {
			action.run();
			resetSelectionCooldownWithSound(Sound.MENU_MOVE);
		}
	}

	private void deleteName() {
		String name = isMultiplayer ? name2 : name1;
		if (!name.isEmpty()) {
			if (isMultiplayer) name2 = name2.substring(0, name2.length() - 1);
			else name1 = name1.substring(0, name1.length() - 1);
			resetSelectionCooldownWithSound(Sound.MENU_TYPING);
		}
	}

	private void handleNameInput() {
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

	private void exitToMainMenu() {
		this.returnCode = 1;
		this.isRunning = false;
		soundManager.playSound(Sound.MENU_BACK);
	}

	private void resetSelectionCooldownWithSound(Sound sound) {
		this.selectionCooldown.reset();
		soundManager.playSound(sound);
	}

	private void draw() {
		drawManager.initDrawing(this);
		drawManager.drawGameSetting(this);
		drawManager.drawGameSettingRow(this, this.selectedRow);
		drawManager.drawGameSettingElements(this, this.selectedRow, isMultiplayer, name1, name2, this.difficultyLevel);
		drawManager.completeDrawing(this);
		Core.setLevelSetting(this.difficultyLevel);
	}

	public static GameSettingScreen getInstance() {
		if (instance == null) {
			instance = new GameSettingScreen(0, 0, 0);
		}
		return instance;
	}

	public static boolean getMultiPlay() {
		return isMultiplayer;
	}

	public static String getName(int playerNumber) {
		return playerNumber == 0 ? name1 : name2;
	}
}
