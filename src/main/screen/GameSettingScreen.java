package screen;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.InputManager;
import engine.*;

import java.awt.event.KeyEvent;

public class GameSettingScreen extends Screen {
	private static GameSettingScreen instance;

	private static final int SELECTION_TIME = 200;
	private static final int NAME_LIMIT = 4;

	private static String name1;
	private static String name2;
	private static boolean isMultiplayer = false;
	private int difficultyLevel;
	private int selectedRow;
	private final Cooldown selectionCooldown;

	private static final int TOTAL_ROWS = 3;

	private final SoundManager soundManager = SoundManager.getInstance();

	public GameSettingScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.name1 = "P1";
		this.name2 = "P2";
		this.isMultiplayer = false;
		this.difficultyLevel = 1;
		this.selectedRow = 0;

		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();
	}

	public final int run() {
		super.run();
		return this.returnCode;
	}

	protected final void update() {
		super.update();
		draw();

		if (this.inputDelay.checkFinished() && this.selectionCooldown.checkFinished()) {
			if (inputManager.isKeyDown(KeyEvent.VK_UP)) {
				this.selectedRow = (this.selectedRow - 1 + TOTAL_ROWS) % TOTAL_ROWS;
				this.selectionCooldown.reset();
				soundManager.playSound(Sound.MENU_MOVE);
			} else if (inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
				this.selectedRow = (this.selectedRow + 1) % TOTAL_ROWS;
				this.selectionCooldown.reset();
				soundManager.playSound(Sound.MENU_MOVE);
			}

			if (this.selectedRow == 0) {
				if (inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
					this.isMultiplayer = false;
					this.selectionCooldown.reset();
					soundManager.playSound(Sound.MENU_MOVE);
				} else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
					this.isMultiplayer = true;
					this.selectionCooldown.reset();
					soundManager.playSound(Sound.MENU_MOVE);
				} else if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)) {
					if (isMultiplayer) {
						if (!this.name2.isEmpty()) {
							this.name2 = this.name2.substring(0, this.name2.length() - 1);
							this.selectionCooldown.reset();
							soundManager.playSound(Sound.MENU_TYPING);
						}
					} else {
						if (!this.name1.isEmpty()) {
							this.name1 = this.name1.substring(0, this.name1.length() - 1);
							this.selectionCooldown.reset();
							soundManager.playSound(Sound.MENU_TYPING);
						}
					}
				}
				handleNameInput(inputManager);
			} else if (this.selectedRow == 1) {
				if (inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
					if (this.difficultyLevel != 0) {
						this.difficultyLevel--;
						this.selectionCooldown.reset();
						soundManager.playSound(Sound.MENU_MOVE);
					}
				} else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
					if (this.difficultyLevel != 2) {
						this.difficultyLevel++;
						this.selectionCooldown.reset();
						soundManager.playSound(Sound.MENU_MOVE);
					}
				}
			} else if (this.selectedRow == 2) {
				if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
					this.returnCode = isMultiplayer ? 8 : 2;
					this.isRunning = false;
					soundManager.playSound(Sound.MENU_CLICK);
				}
			}
			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				this.returnCode = 1;
				this.isRunning = false;
				soundManager.playSound(Sound.MENU_BACK);
			}
		}
	}

	private void handleNameInput(InputManager inputManager) {
		for (int keyCode = KeyEvent.VK_A; keyCode <= KeyEvent.VK_Z; keyCode++) {
			if (inputManager.isKeyDown(keyCode)) {
				if (isMultiplayer) {
					if (this.name2.length() < NAME_LIMIT) {
						this.name2 += (char) keyCode;
						this.selectionCooldown.reset();
						soundManager.playSound(Sound.MENU_TYPING);
					}
				} else {
					if (this.name1.length() < NAME_LIMIT) {
						this.name1 += (char) keyCode;
						this.selectionCooldown.reset();
						soundManager.playSound(Sound.MENU_TYPING);
					}
				}
			}
		}
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

	public void setMultiplayer(boolean multiplayer) {
		isMultiplayer = multiplayer;
	}

	public void setDifficultyLevel(int level) {
		this.difficultyLevel = level;
	}

	public Cooldown getSelectionCooldown() {
		return this.selectionCooldown;
	}

	public void setName(int playerNumber, String name) {
		if (playerNumber == 0) {
			name1 = name;
		} else if (playerNumber == 1) {
			name2 = name;
		}
	}

	public void setSelectedRow(int row) {
		this.selectedRow = row;
	}

	public Object getField(String fieldName) {
		return switch (fieldName.toLowerCase()) {
			case "difficulty" -> this.difficultyLevel;
			case "row" -> this.selectedRow;
			case "multiplayer" -> this.isMultiplayer;
			default -> null;
		};
	}

	private void draw() {
		drawManager.initDrawing(this);
		drawManager.drawGameSetting(this);
		drawManager.drawGameSettingRow(this, this.selectedRow);
		drawManager.drawGameSettingElements(this, this.selectedRow, isMultiplayer, name1, name2, this.difficultyLevel);
		drawManager.completeDrawing(this);
		Core.setLevelSetting(this.difficultyLevel);
	}
}
