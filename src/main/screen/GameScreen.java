package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.Timer;
import java.util.TimerTask;


import engine.*;
import entity.*;


/**
 * Implements the game screen, where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen extends Screen implements Callable<GameState> {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40;

	/** Current game difficulty settings. */
	private GameSettings gameSettings;
	/** Current difficulty level number. */
	private int level;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Player's ship. */
	private Ship ship;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;
	private Cooldown shootingCooldown;
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets;
	/** Present score */
	private int score;
	/** tempScore records the score up to the previous level. */
	private int tempScore;
	/** Current ship type. */
	private Ship.ShipType shipType;
	/** Player lives left. */
	private int lives;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
	/** Number of consecutive hits.
	 * maxCombo records the maximum value of combos in that level. */
	private int combo;
	private int maxCombo;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Checks if a bonus life is received. **/
	private boolean bonusLife;
	/** Player number for two player mode **/
	private int playerNumber;
	/** list of highScores for find recode. */
	private List<Score>highScores;
	/** Elapsed time while playing this game.
	 * lapTime records the time to the previous level. */
	private int elapsedTime;
	private int lapTime;
	/** Keep previous timestamp. */
	private Integer prevTime;
	/** Alert Message when a special enemy appears. */
	private String alertMessage;
	/** checks if it's executed. */
  	private boolean isExecuted = false;
	/** timer.. */
	private Timer timer;
	private TimerTask timerTask;
	/** Spider webs restricting player movement */
	private List<Web> web;
	/**
	 * Obstacles preventing a player's bullet
	 */
	private Set<Block> blocks;

	private Wallet wallet;
	/** Singleton instance of SoundManager */
	private final SoundManager soundManager = SoundManager.getInstance();
	/** Singleton instance of ItemManager. */
	private ItemManager itemManager;
	/** Item boxes that dropped when kill enemy ships. */
	private Set<ItemBox> itemBoxes;
	/** Barriers appear in game screen. */
	private Set<Barrier> barriers;
	/** Sound balance for each player*/
	private float balance = 0.0f;

	private GameState gameState;

	private int hitBullets;

    /**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
			final GameSettings gameSettings, final boolean bonusLife,
			final int width, final int height, final int fps, final Wallet wallet) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.gameState = gameState;
		this.bonusLife = bonusLife;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.elapsedTime = gameState.getElapsedTime();
		this.alertMessage = gameState.getAlertMessage();
		this.shipType = gameState.getShipType();
		this.lives = gameState.getLivesRemaining();
		if (this.bonusLife)
			this.lives++;
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
		this.playerNumber = -1;
		this.maxCombo = gameState.getMaxCombo();
		this.lapTime = gameState.getPrevTime();
		this.tempScore = gameState.getPrevScore();
		this.alertMessage = "";
		this.hitBullets = gameState.getHitBullets();
		this.wallet = wallet;
	}

	/**
	 * Constructor, establishes the properties of the screen for two player mode.
	 *
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 * @param playerNumber
	 *            Player number for two player mode
	 */
	public GameScreen(final GameState gameState,
					  final GameSettings gameSettings, final boolean bonusLife,
					  final int width, final int height, final int fps, final Wallet wallet,
					  final int playerNumber) {
		this(gameState, gameSettings, bonusLife, width, height, fps, wallet);
		this.playerNumber = playerNumber;
		this.balance = switch (playerNumber) {
			case 0: yield -1.0f; // 1P
			case 1: yield 1.0f;  // 2P
			default: yield 0.0f; // default
		};
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */

		public final void initialize() {
			super.initialize();
			setupEnemyFormation();
			createShip();
			createSpiderWebs();
			createBlocks();
			setCooldown();
			setSoundManager();
			this.bullets = new HashSet<>();
			this.barriers = new HashSet<>();
			this.itemBoxes = new HashSet<>();
			this.itemManager = new ItemManager(this.ship, this.enemyShipFormation, this.barriers, this.height, this.width, this.balance);

		}

        // Method to set the enemy fleet
		private void setupEnemyFormation() {
			enemyShipFormation = new EnemyShipFormation(this.gameSettings, this.gameState);
			enemyShipFormation.attach(this);
		}

		// Method to create the player's spaceship
		private void createShip() {
			this.ship = ShipFactory.create(this.shipType, this.width / 2, this.height - 70);
			ship.applyItem(wallet);
		}


		// Method to create spider web
		private void createSpiderWebs() {
			int webCount = 1 + level / 3;
			web = new ArrayList<>();
			for (int i = 0; i < webCount; i++) {
				int positionX = (int) Math.max(0, Math.random() * width - 24);
				web.add(new Web(positionX, this.height - 70));
				this.logger.info("Spider web creation location : " + positionX);
			}
		}

	    // Method to create a block
		private void createBlocks() {
			int blockCount = level / 2;
			int playerTopYContainBarrier = this.height - 190;
			int enemyBottomY = 100 + (gameSettings.getFormationHeight() - 1) * 48;
			blocks = new HashSet<>();

			for (int i = 0; i < blockCount; i++) {
				Block newBlock;
				do {
					newBlock = createRandomBlock(playerTopYContainBarrier, enemyBottomY);
				} while (isOverlapping(newBlock));
				blocks.add(newBlock);
			}
		}


	    // Method to generate random blocks
		private Block createRandomBlock(int maxY, int minY) {
			int positionX = (int) (Math.random() * (this.width - 20*2));
			int positionY = (int) (Math.random() * (maxY - minY)) + minY;
			return new Block(positionX, positionY);
		}


	    // Method to check if a block overlaps another block
		private boolean isOverlapping(Block newBlock) {
			for (Block existingBlock : blocks) {
				if (checkCollision(newBlock, existingBlock)) {
					return true;
				}
			}
			return false;
		}



		// Appears each 10-30 seconds.
	private void setCooldown() {
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
	}


		// Special input delay / countdown.
	private void setSoundManager() {
		if (soundManager.isSoundPlaying(Sound.BGM_MAIN))
			soundManager.stopSound(Sound.BGM_MAIN);
		soundManager.playSound(Sound.COUNTDOWN);

		switch (this.level) {
			case 1:
				soundManager.loopSound(Sound.BGM_LV1);
				break;
			case 2:
				soundManager.loopSound(Sound.BGM_LV2);
				break;
			case 3:
				soundManager.loopSound(Sound.BGM_LV3);
				break;
			case 4:
				soundManager.loopSound(Sound.BGM_LV4);
				break;
			case 5:
				soundManager.loopSound(Sound.BGM_LV5);
				break;
			case 6:
				soundManager.loopSound(Sound.BGM_LV6);
				break;
			case 7:
				// From level 7 and above, it continues to play at BGM_LV7.
			default:
				soundManager.loopSound(Sound.BGM_LV7);
				break;
		}


	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		this.score += LIFE_SCORE * (this.lives - 1);
		if(this.lives == 0) this.score += 100;
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();


		if (this.inputDelay.checkFinished() && !this.levelFinished) {
			handlePlayerActions();
			updateShipStatus();
			updateElapsedTime();
			updateEnemiesAndBullets();
			handleEnemyShipSpecial();
		}


		    manageCollisions();
			cleanBullets();

			checkLevelCompletion();

			if (playerNumber >= 0)
				drawThread();
			else
				draw();
		}


	private void handleEnemyShipSpecial() {
		if (this.enemyShipSpecial != null) {
			if (!this.enemyShipSpecial.isDestroyed())
				this.enemyShipSpecial.move(2, 0);
			else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
				this.enemyShipSpecial = null;

		}
		if (this.enemyShipSpecial == null
				&& this.enemyShipSpecialCooldown.checkFinished()) {
			this.enemyShipSpecial = new EnemyShip();
			this.alertMessage = "";
			this.enemyShipSpecialCooldown.reset();
			soundManager.playSound(Sound.UFO_APPEAR, balance);
			this.logger.info("A special ship appears");
		}

		if (this.enemyShipSpecial != null
				&& this.enemyShipSpecial.getPositionX() > this.width) {
			this.enemyShipSpecial = null;
			this.logger.info("The special ship has escaped");
		}
		if(this.enemyShipSpecial == null
				&& this.enemyShipSpecialCooldown.checkAlert())
			handleAlertMessage();
	}

	private void updateEnemiesAndBullets() {
		if (!itemManager.isTimeStopActive()) {
			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets, this.level, balance);
		}

	}

	private void SpiderWebInteraction() {
		for (int i = 0; i < web.size(); i++) {
			//escape Spider Web
			if (ship.getPositionX() + 6 <= web.get(i).getPositionX() - 6
					|| web.get(i).getPositionX() + 6 <= ship.getPositionX() - 6) {
				this.ship.setThreadWeb(false);
			}
			//get caught in a spider's web
			else {
				this.ship.setThreadWeb(true);
				break;
			}
		}
	}

	private void handleShipMovement() {
		boolean moveRight = isMoveRight();
		boolean moveLeft = isMoveLeft();

		boolean isRightBorder = this.ship.getPositionX()
				+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
		boolean isLeftBorder = this.ship.getPositionX()
				- this.ship.getSpeed() < 1;

		if (moveRight && !isRightBorder) {
			if (playerNumber == -1)
				this.ship.moveRight();
			else
				this.ship.moveRight(balance);
		}
		if (moveLeft && !isLeftBorder) {
			if (playerNumber == -1)
				this.ship.moveLeft();
			else
				this.ship.moveLeft(balance);
		}
	}

		private boolean isMoveLeft() {
		switch (playerNumber) {
			case 0:
				return inputManager.isKeyDown(KeyEvent.VK_A);
			case 1:
				return inputManager.isKeyDown(KeyEvent.VK_LEFT);
			default:
				return inputManager.isKeyDown(KeyEvent.VK_LEFT)
						|| inputManager.isKeyDown(KeyEvent.VK_A);

		}
	}

	private boolean isMoveRight() {
		switch (playerNumber) {
			case 0:
				return inputManager.isKeyDown(KeyEvent.VK_D);
			case 1:
				return inputManager.isKeyDown(KeyEvent.VK_RIGHT);
			default:
				return inputManager.isKeyDown(KeyEvent.VK_RIGHT)
						|| inputManager.isKeyDown(KeyEvent.VK_D);
		}
	}

	private void updateElapsedTime() {
		/*Elapsed Time Update*/
		long currentTime = System.currentTimeMillis();

		if (this.prevTime != null)
			this.elapsedTime += (int) (currentTime - this.prevTime);

		this.prevTime = (int) currentTime;
	}

	private void shotIfPossible(int shotNum){
		if (this.ship.shoot(this.bullets, shotNum))
			this.bulletsShot += shotNum;
	}

	private void shotIfPossible(int shotNum, float balance){
		if (this.ship.shoot(this.bullets, shotNum, balance)) // Player 1 attack
			this.bulletsShot += this.itemManager.getShotNum();
	}

	private void handlePlayerActions() {
		boolean player1Attacking = inputManager.isKeyDown(KeyEvent.VK_SPACE);
		boolean player2Attacking = inputManager.isKeyDown(KeyEvent.VK_SHIFT);

		if (player1Attacking && player2Attacking) {
			// Both players are attacking
			shotIfPossible(this.itemManager.getShotNum());
		} else {
			switch (playerNumber) {
				case 1:
					if (player2Attacking) {
						shotIfPossible(this.itemManager.getShotNum(), 1.0f);
					}
					break;
				case 0:
				default:
					if (player1Attacking) {
						shotIfPossible(this.itemManager.getShotNum(), (playerNumber == 0) ? -1.0f : 0.0f);
					}
					break;
			}
		}
	}

	private void updateShipStatus() {
		this.ship.update();
		// If Time-stop is active, Stop updating enemy ships' move and their shoots.
		if (!itemManager.isTimeStopActive()) {
			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets, this.level, balance);
		}
		if(!itemManager.isGhostActive())
			this.ship.setColor(Color.GREEN);
		if (!this.ship.isDestroyed()){
			handleShipMovement();
			SpiderWebInteraction();
		}
	}

	private void handleAlertMessage() {

			switch (this.enemyShipSpecialCooldown.checkAlertAnimation()){
				case 1: this.alertMessage = "--! ALERT !--";
					break;

				case 2: this.alertMessage = "-!! ALERT !!-";
					break;

				case 3: this.alertMessage = "!!! ALERT !!!";
					break;

				default: this.alertMessage = "";
					break;
			}
	}

	private void checkLevelCompletion() {
		if ((this.enemyShipFormation.isEmpty() || this.lives <= 0)
				&& !this.levelFinished) {
			this.levelFinished = true;
			soundManager.stopSound(soundManager.getCurrentBGM());
			if (this.lives == 0)
				soundManager.playSound(Sound.GAME_END);
			this.screenFinishedCooldown.reset();
		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished()) {
			//Reset alert message when level is finished
			this.alertMessage = "";
			this.isRunning = false;
		}
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);
		drawManager.drawGameTitle(this);
		//draw entities.
		drawManager.drawEntity(this.ship, this.ship.getPositionX(), this.ship.getPositionY());

		drawManager.drawEntities(itemBoxes);
		drawManager.drawEntities(barriers);
		drawManager.drawEntities(bullets);
		drawManager.drawEntities(blocks);

		for (int i = 0; i < web.size(); i++) {
			drawManager.drawEntity(this.web.get(i), this.web.get(i).getPositionX(),
					this.web.get(i).getPositionY());
		}

		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();




		// Interface.
		drawManager.drawScore(this, this.score);
		drawManager.drawElapsedTime(this, this.elapsedTime);
		drawManager.drawAlertMessage(this, this.alertMessage);
		drawManager.drawLives(this, this.lives, this.shipType);
		drawManager.drawLevel(this, this.level);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
		drawManager.drawReloadTimer(this,this.ship,ship.getRemainingReloadTime());
		drawManager.drawCombo(this,this.combo);

        //handle countdown.
		handleCountDown();



		drawManager.completeDrawing(this);
	}

	private void handleCountDown() {
		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown, this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);

			//Intermediate aggregation
			if (this.level > 1){
				if (countdown == 0) {
					//Reset mac combo and edit temporary values
					this.lapTime = this.elapsedTime;
					this.tempScore = this.score;
					this.maxCombo = 0;
				} else {
					// Don't show it just before the game starts, i.e. when the countdown is zero.
					drawManager.interAggre(this, this.level - 1, this.maxCombo, this.elapsedTime, this.lapTime, this.score, this.tempScore);
				}
			}
		}


		drawManager.completeDrawing(this);
	}

	/**
	 * Draws the elements associated with the screen to thread buffer.
	 */
	private void drawThread() {
		drawManager.initThreadDrawing(this, playerNumber);
		drawManager.drawGameTitle(this, playerNumber);
		drawManager.drawLaunchTrajectory( this,this.ship.getPositionX(), playerNumber);
		//draw entities.
		drawManager.drawEntity(this.ship, this.ship.getPositionX(),
				this.ship.getPositionY(), playerNumber);

		drawManager.drawEntities(itemBoxes, playerNumber);
		drawManager.drawEntities(barriers, playerNumber);
		drawManager.drawEntities(bullets, playerNumber);
		drawManager.drawEntities(blocks, playerNumber);

		for (int i = 0; i < web.size(); i++) {
			drawManager.drawEntity(this.web.get(i), this.web.get(i).getPositionX(),
					this.web.get(i).getPositionY(), playerNumber);
		}

		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY(), playerNumber);

		enemyShipFormation.draw(playerNumber);

		// handle thread game over.
		handleThreadGameOver();

		// handle thread countdown.
		handleThreadCountDown();


		// Interface.
		drawManager.drawScore(this, this.score, playerNumber);
		drawManager.drawElapsedTime(this, this.elapsedTime, playerNumber);
		drawManager.drawAlertMessage(this, this.alertMessage, playerNumber);
		drawManager.drawLives(this, this.lives, this.shipType, playerNumber);
		drawManager.drawLevel(this, this.level, playerNumber);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1, playerNumber);
		drawManager.drawReloadTimer(this, this.ship, ship.getRemainingReloadTime(), playerNumber);
		drawManager.drawCombo(this, this.combo, playerNumber);


		//add drawRecord method for drawing
		drawManager.drawRecord(this, playerNumber);


		drawManager.flushBuffer(this, playerNumber);


	}

	// 게임 오버 처리
	private void handleThreadGameOver() {
		if (this.levelFinished && this.screenFinishedCooldown.checkFinished() && this.lives <= 0) {
			drawManager.drawInGameOver(this, this.height, playerNumber);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12, playerNumber);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12, playerNumber);
		}
	}

	// 카운트다운 처리
	private void handleThreadCountDown() {
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown, this.bonusLife, playerNumber);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12, playerNumber);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12, playerNumber);

			if (this.level > 1) {
				if (countdown == 0) {
					this.lapTime = this.elapsedTime;
					this.tempScore = this.score;
					this.maxCombo = 0;
				} else {
					drawManager.interAggre(this, this.level - 1, this.maxCombo, this.elapsedTime, this.lapTime, this.score, this.tempScore, playerNumber);
				}
			}
		}

		//add drawRecord method for drawing
		drawManager.drawRecord(this, playerNumber);
		drawManager.flushBuffer(this, playerNumber);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		if (isExecuted == false){
			isExecuted = true;
			timer = new Timer();
			timerTask = new TimerTask() {
				public void run() {
					combo = 0;
				}
			};
			timer.schedule(timerTask, 3000);
		}


		for (Bullet bullet : this.bullets) {

			// Enemy ship's bullets
			if (bullet.getSpeed() > 0) {
				shipCollision(recyclable, bullet);

				if (this.barriers != null) {
					barrierCollision(recyclable, bullet);
				}

			} else {	// Player ship's bullets
				for (EnemyShip enemyShip : this.enemyShipFormation)
					enemyShipCollision(recyclable, bullet, enemyShip);
				enemyShipSpecialCollision(recyclable, bullet);


				if (this.itemManager.getShotNum() == 1 && bullet.getPositionY() < getTopEnemyY()) {
					this.combo = 0;
					isExecuted = true;
				}

				itemBoxCollision(recyclable, bullet);

				bulletBlockCollision(recyclable, bullet);
			}
		}
		Set<Block> removableBlocks = new HashSet<>();

		enemyShipBlockCollision(removableBlocks);


		// remove crashed obstacle
		blocks.removeAll(removableBlocks);
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	private void shipCollision(Set<Bullet> recyclable, Bullet bullet) {
		if (checkCollision(bullet, this.ship) && !this.levelFinished && !itemManager.isGhostActive()) {
			recyclable.add(bullet);
			if (!this.ship.isDestroyed()) {
				this.ship.destroy(balance);
				lvdamage();
				this.logger.info("Hit on player ship, " + this.lives + " lives remaining.");
			}
		}
	}

	private void enemyShipCollision(Set<Bullet> recyclable, Bullet bullet, EnemyShip enemyShip) {
		if (enemyShip != null && !enemyShip.isDestroyed()
				&& checkCollision(bullet, enemyShip)) {
			// Decide whether to destroy according to physical strength
			this.enemyShipFormation.HealthManageDestroy(enemyShip, balance);
			// If the enemy doesn't die, the combo increases;
			// if the enemy dies, both the combo and score increase.
			this.score += Score.comboScore(this.enemyShipFormation.getPoint(), this.combo);
			this.shipsDestroyed += this.enemyShipFormation.getDistroyedship();
			this.combo++;
			this.hitBullets++;
			if (this.combo > this.maxCombo) this.maxCombo = this.combo;
			timer.cancel();
			isExecuted = false;
			recyclable.add(bullet);

			if (enemyShip.getHealth() < 0 && itemManager.dropItem()) {
				this.itemBoxes.add(new ItemBox(enemyShip.getPositionX() + 6, enemyShip.getPositionY() + 1, balance));
				logger.info("Item box dropped");
			}
		}
	}

	private void enemyShipSpecialCollision(Set<Bullet> recyclable, Bullet bullet) {
		if (this.enemyShipSpecial != null
				&& !this.enemyShipSpecial.isDestroyed()
				&& checkCollision(bullet, this.enemyShipSpecial)) {
			this.score += Score.comboScore(this.enemyShipSpecial.getPointValue(), this.combo);
			this.shipsDestroyed++;
			this.combo++;
			this.hitBullets++;
			if (this.combo > this.maxCombo) this.maxCombo = this.combo;
			this.enemyShipSpecial.destroy(balance);
			this.enemyShipSpecialExplosionCooldown.reset();
			timer.cancel();
			isExecuted = false;

			recyclable.add(bullet);
		}
	}

	private void barrierCollision(Set<Bullet> recyclable, Bullet bullet) {
		Iterator<Barrier> barrierIterator = this.barriers.iterator();
		while (barrierIterator.hasNext()) {
			Barrier barrier = barrierIterator.next();
			if (checkCollision(bullet, barrier)) {
				recyclable.add(bullet);
				barrier.reduceHealth(balance);
				if (barrier.isDestroyed()) {
					barrierIterator.remove();
				}
			}
		}
	}

	private void itemBoxCollision(Set<Bullet> recyclable, Bullet bullet) {
		Iterator<ItemBox> itemBoxIterator = this.itemBoxes.iterator();
		while (itemBoxIterator.hasNext()) {
			ItemBox itemBox = itemBoxIterator.next();
			if (checkCollision(bullet, itemBox) && !itemBox.isDroppedRightNow()) {
				this.hitBullets++;
				itemBoxIterator.remove();
				recyclable.add(bullet);
				Entry<Integer, Integer> itemResult = this.itemManager.useItem();

				if (itemResult != null) {
					this.score += itemResult.getKey();
					this.shipsDestroyed += itemResult.getValue();
				}
			}
		}
	}
	private void bulletBlockCollision(Set<Bullet> recyclable, Bullet bullet) {
		for (Block block : this.blocks) {
			if (checkCollision(bullet, block)) {
				recyclable.add(bullet);
				soundManager.playSound(Sound.BULLET_BLOCKING, balance);
				break;
			}
		}
	}
	private void enemyShipBlockCollision(Set<Block> removableBlocks) {
		//check the collision between the obstacle and the enemyship
		for (EnemyShip enemyShip : this.enemyShipFormation) {
			if (enemyShip != null && !enemyShip.isDestroyed()) {
				for (Block block : blocks) {
					if (checkCollision(enemyShip, block)) {
						removableBlocks.add(block);
					}
				}
			}
		}
	}
	private int getTopEnemyY() {
		int topEnemyY = Integer.MAX_VALUE;
		for (EnemyShip enemyShip : this.enemyShipFormation) {
			if (enemyShip != null && !enemyShip.isDestroyed() && enemyShip.getPositionY() < topEnemyY) {
				topEnemyY = enemyShip.getPositionY();
			}
		}
		if (this.enemyShipSpecial != null && !this.enemyShipSpecial.isDestroyed() && this.enemyShipSpecial.getPositionY() < topEnemyY) {
			topEnemyY = this.enemyShipSpecial.getPositionY();
		}
		return topEnemyY;
	}


	/**
	 * Checks if two entities are colliding.
	 * 
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 * 
	 * @return Current game state.
	 */
	public final GameState getGameState() {
		return new GameState(this.level, this.score, this.shipType, this.lives,
				this.bulletsShot, this.shipsDestroyed, this.elapsedTime, this.alertMessage, 0, this.maxCombo, this.lapTime, this.tempScore, this.hitBullets);
	}


	/**
	 * Start the action for two player mode
	 *
	 * @return Current game state.
	 */
	@Override
	public final GameState call() {
		run();
		return getGameState();
	}
	//Enemy bullet damage increases depending on stage level
	public void lvdamage(){
		for(int i=0; i<=level/3;i++){
			this.lives--;
		}
		if(this.lives < 0){
			this.lives = 0;
		}
	}
}
