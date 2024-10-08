package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Iterator;


import engine.*;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

/**
 * Implements the game screen, where the action happens.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameScreen extends Screen {

	/**
	 * Milliseconds until the screen accepts user input.
	 */
	private static final int INPUT_DELAY = 6000;
	/**
	 * Bonus score for each life remaining at the end of the level.
	 */
	private static final int LIFE_SCORE = 100;
	/**
	 * Minimum time between bonus ship's appearances.
	 */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/**
	 * Maximum variance in the time between bonus ship's appearances.
	 */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/**
	 * Time until bonus ship explosion disappears.
	 */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/**
	 * Time from finishing the level to screen change.
	 */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/**
	 * Height of the interface separation line.
	 */
	private static final int SEPARATION_LINE_HEIGHT = 40;

	/**
	 * Current game difficulty settings.
	 */
	private GameSettings gameSettings;
	/**
	 * Current difficulty level number.
	 */
	private int level;
	/**
	 * Formation of enemy ships.
	 */
	private EnemyShipFormation enemyShipFormation;
	/**
	 * Player's ship.
	 */
	private Ship ship;
	/**
	 * Bonus enemy ship that appears sometimes.
	 */
	private EnemyShip enemyShipSpecial;
	/**
	 * Minimum time between bonus ship appearances.
	 */
	private Cooldown enemyShipSpecialCooldown;
	/**
	 * Time until bonus ship explosion disappears.
	 */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/**
	 * Time from finishing the level to screen change.
	 */
	private Cooldown screenFinishedCooldown;
	private Cooldown shootingCooldown;
	/**
	 * Set of all bullets fired by on screen ships.
	 */
	private Set<Bullet> bullets;
	/**
	 * Current score.
	 */
	private int score;
	/**
	 * Player lives left.
	 */
	private int lives;
	/**
	 * Total bullets shot by the player.
	 */
	private int bulletsShot;
	/**
	 * Total ships destroyed by the player.
	 */
	private int shipsDestroyed;
	/**
	 * Total ships destroyed consecutive by the player.
	 */
	private int combo = 0;
	/**
	 * Moment the game starts.
	 */
	private long gameStartTime;
	/**
	 * Checks if the level is finished.
	 */
	private boolean levelFinished;
	/**
	 * Checks if a bonus life is received.
	 */
	private boolean bonusLife;
	/**
	 * list of highScores for find recode.
	 */
	private List<Score> highScores;
	/**
	 * Elapsed time while playing this game.
	 */
	private int elapsedTime;
	/**
	 * Alert Message when a special enemy appears.
	 */
	private String alertMessage;
	/**
	 * Checks if the player can continue combo.
	 */
	private boolean cancombo = true;


	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param gameState    Current game state.
	 * @param gameSettings Current game settings.
	 * @param bonusLife    Checks if a bonus life is awarded this level.
	 * @param width        Screen width.
	 * @param height       Screen height.
	 * @param fps          Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
					  final GameSettings gameSettings, final boolean bonusLife,
					  final int width, final int height, final int fps) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.elapsedTime = gameState.getElapsedTime();
		this.alertMessage = gameState.getAlertMessage();
		this.lives = gameState.getLivesRemaining();
		if (this.bonusLife)
			this.lives++;
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();

		try {
			this.highScores = Core.getFileManager().loadHighScores();

		} catch (IOException e) {
			logger.warning("Couldn't load high scores!");
		}

	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

		enemyShipFormation = new EnemyShipFormation(this.gameSettings);
		enemyShipFormation.attach(this);
		this.ship = new Ship(this.width / 2, this.height - 30);
		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.bullets = new HashSet<Bullet>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		this.score += LIFE_SCORE * (this.lives - 1);
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();
		if (this.inputDelay.checkFinished() && !this.levelFinished) {

			/*Elapsed Time Update*/
			this.elapsedTime++;

			if (!this.ship.isDestroyed()) {
				boolean moveRight = inputManager.isKeyDown(KeyEvent.VK_RIGHT)
						|| inputManager.isKeyDown(KeyEvent.VK_D);
				boolean moveLeft = inputManager.isKeyDown(KeyEvent.VK_LEFT)
						|| inputManager.isKeyDown(KeyEvent.VK_A);

				boolean isRightBorder = this.ship.getPositionX()
						+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
				boolean isLeftBorder = this.ship.getPositionX()
						- this.ship.getSpeed() < 1;

				if (moveRight && !isRightBorder) {
					this.ship.moveRight();
				}
				if (moveLeft && !isLeftBorder) {
					this.ship.moveLeft();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
					if (this.ship.shoot(this.bullets))
						this.bulletsShot++;
			}

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
				this.logger.info("A special ship appears");
			}
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkAlert()) {
				if (this.enemyShipSpecialCooldown.checkAlertAnimation() == 3) {
					this.alertMessage = "!!! ALERT !!!";
				} else if (this.enemyShipSpecialCooldown.checkAlertAnimation() == 2) {
					this.alertMessage = "-!! ALERT !!-";
				} else if (this.enemyShipSpecialCooldown.checkAlertAnimation() == 1) {
					this.alertMessage = "--! ALERT !--";
				} else {
					this.alertMessage = "";
				}
			}
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.width) {
				this.enemyShipSpecial = null;
				this.logger.info("The special ship has escaped");
			}

			this.ship.update();
			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets);
		}

		manageCollisions();
		cleanBullets();
		draw();

		if ((this.enemyShipFormation.isEmpty() || this.lives == 0)
				&& !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();
		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
			this.isRunning = false;

	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);
		drawManager.drawGameTitle(this);

		drawManager.drawLaunchTrajectory(this, this.ship.getPositionX());

		drawManager.drawEntity(this.ship, this.ship.getPositionX(), this.ship.getPositionY());
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());


		drawManager.drawScore(this, this.score);
		drawManager.drawElapsedTime(this, this.elapsedTime);
		drawManager.drawAlertMessage(this, this.alertMessage);
		drawManager.drawLives(this, this.lives);
		drawManager.drawLevel(this, this.level);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
		drawManager.drawReloadTimer(this, this.ship, ship.getRemainingReloadTime());
		drawManager.drawCombo(this, this.combo);


		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown, this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);
		}

		//add drawRecode method for drawing
		drawManager.drawRecord(highScores, this);

		drawManager.completeDrawing(this);
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
		// 총알을 추적하기 위한 Iterator 생성
		Iterator<Bullet> iterator = this.bullets.iterator();

		// 적 함대 중 가장 위에 있는 적의 y 위치 찾기
		int topEnemyY = Integer.MAX_VALUE;
		for (EnemyShip enemyShip : this.enemyShipFormation) {
			if (!enemyShip.isDestroyed() && enemyShip.getPositionY() < topEnemyY) {
				topEnemyY = enemyShip.getPositionY();
			}
		}
		if (this.enemyShipSpecial != null && !this.enemyShipSpecial.isDestroyed() && this.enemyShipSpecial.getPositionY() < topEnemyY) {
			topEnemyY = this.enemyShipSpecial.getPositionY();
		}

		// 모든 총알을 순회
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();

			// 플레이어가 발사한 총알 (양수 속도)
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.ship) && !this.levelFinished) {
					if (!this.ship.isDestroyed()) {
						this.ship.destroy(); // 플레이어 배 파괴
						this.lives--; // 플레이어의 남은 목숨 감소
						this.combo = 0; // 콤보 초기화 (플레이어가 맞으면 콤보 리셋)
						this.logger.info("Hit on player ship, " + this.lives + " lives remaining.");
					}
					iterator.remove(); // 안전하게 총알 제거
					cancombo = false; // 플레이어가 맞으면 콤보 불가 설정
				}
			} else { // 적이 발사한 총알 (음수 속도)
				boolean hitEnemy = false; // 적이 맞았는지 여부를 추적하는 변수

				// 적 함대의 각 함선과 충돌 여부 확인
				for (EnemyShip enemyShip : this.enemyShipFormation) {
					if (!enemyShip.isDestroyed() && checkCollision(bullet, enemyShip)) {
						this.score += enemyShip.getPointValue(); // 점수 증가
						if (cancombo) {
							this.combo++; // 콤보 증가
						} else {
							this.combo = 1; // 콤보 초기화 후 첫 콤보 설정
							cancombo = true;
						}
						this.shipsDestroyed++; // 파괴된 적 수 증가
						this.enemyShipFormation.destroy(enemyShip); // 적 함선 파괴
						iterator.remove(); // 안전하게 총알 제거
						hitEnemy = true; // 적을 맞췄음을 표시
						break; // 적을 맞췄으면 반복 종료
					}
				}

				// 특별한 적 함선과의 충돌 여부 확인
				if (!hitEnemy && this.enemyShipSpecial != null && !this.enemyShipSpecial.isDestroyed() && checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += enemyShipSpecial.getPointValue(); // 점수 증가
					if (cancombo) {
						this.combo++; // 콤보 증가
					} else {
						this.combo = 1; // 콤보 초기화 후 첫 콤보 설정
						cancombo = true;
					}
					this.shipsDestroyed++; // 파괴된 적 수 증가
					this.enemyShipSpecial.destroy(); // 특별 적 함선 파괴
					this.enemyShipSpecialExplosionCooldown.reset(); // 특별 적 폭발 쿨다운 초기화
					iterator.remove(); // 안전하게 총알 제거
					hitEnemy = true; // 적을 맞췄음을 표시
				}

				// 총알이 가장 위에 있는 적의 y 좌표를 지나쳤지만 적을 맞추지 못한 경우 콤보를 리셋
				if (!hitEnemy && bullet.getPositionY() < topEnemyY) {
					this.combo = 0;
					this.cancombo = false;
				}
			}
		}
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
		return new GameState(this.level, this.score, this.lives,
				this.bulletsShot, this.shipsDestroyed, this.elapsedTime, this.alertMessage, 0);
	}
}