package entity;

import java.util.*;
import java.util.logging.Logger;

import engine.*;
import engine.DrawManager.SpriteType;
import screen.Screen;

/**
 * Groups enemy ships into a formation that moves together.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class EnemyShipFormation implements Iterable<EnemyShip> {

	/** Initial position in the x-axis. */
	private static final int INIT_POS_X = 20;
	/** Initial position in the y-axis. */
	private static final int INIT_POS_Y = 100;
	/** Distance between ships. */
	private static final int SEPARATION_DISTANCE = 40;
	/** Proportion of E-type ships. */
	private static final double PROPORTION_E = 0.1;
	/** Proportion of D-type ships. */
	private static final double PROPORTION_D = 0.1;
	/** Proportion of C-type ships. */
	private static final double PROPORTION_C = 0.1;
	/** Proportion of B-type ships. */
	private static final double PROPORTION_B = 0.2;
	/** Lateral speed of the formation. */
	private static final int X_SPEED = 8;
	/** Downwards speed of the formation. */
	private static final int Y_SPEED = 4;
	/** Speed of the bullets shot by the members. */
	private static final int BULLET_SPEED = 4;
	/** Proportion of differences between shooting times. */
	private static final double SHOOTING_VARIANCE = .2;
	/** Margin on the sides of the screen. */
	private static final int SIDE_MARGIN = 20;
	/** Margin on the bottom of the screen. */
	private static final int BOTTOM_MARGIN = 80;
	/** Distance to go down each pass. */
	private static final int DESCENT_DISTANCE = 20;
	/** Minimum speed allowed. */
	private static final int MINIMUM_SPEED = 10;

	/** DrawManager instance. */
	private DrawManager drawManager;
	/** Application logger. */
	private Logger logger;
	/** Screen to draw ships on. */
	private Screen screen;
	/** Singleton instance of SoundManager */
	private final SoundManager soundManager = SoundManager.getInstance();

	/** List of enemy ships forming the formation. */
	private List<List<EnemyShip>> enemyShips;
	/** Minimum time between shots. */
	private Cooldown shootingCooldown;
	/** Number of ships in the formation - horizontally. */
	private int nShipsWide;
	/** Number of ships in the formation - vertically. */
	private int nShipsHigh;
	/** Time between shots. */
	private int shootingInterval;
	/** Variance in the time between shots. */
	private int shootingVariance;
	/** Initial ship speed. */
	private int baseSpeed;
	/** Speed of the ships. */
	private int movementSpeed;
	/** Current direction the formation is moving on. */
	private Direction currentDirection;
	/** Direction the formation was moving previously. */
	private Direction previousDirection;
	/** Interval between movements, in frames. */
	private int movementInterval;
	/** Total width of the formation. */
	private int width;
	/** Total height of the formation. */
	private int height;
	/** Position in the x-axis of the upper left corner of the formation. */
	private int positionX;
	/** Position in the y-axis of the upper left corner of the formation. */
	private int positionY;
	/** Width of one ship. */
	private int shipWidth;
	/** Height of one ship. */
	private int shipHeight;
	/** List of ships that are able to shoot. */
	private List<EnemyShip> shooters;
	/** Number of not destroyed ships. */
	private int shipCount;

	private int point = 0;

	private int distroyedship = 0;

	private GameState gameState;

	/** Directions the formation can move. */
	private enum Direction {
		/** Movement to the right side of the screen. */
		RIGHT,
		/** Movement to the left side of the screen. */
		LEFT,
		/** Movement to the bottom of the screen. */
		DOWN
	};

	/**
	 * Constructor, sets the initial conditions.
	 * 
	 * @param gameSettings
	 *            Current game settings.
	 */
	public EnemyShipFormation(final GameSettings gameSettings, final GameState gameState) {
		this.drawManager = Core.getDrawManager();
		this.logger = Core.getLogger();
		this.enemyShips = new ArrayList<>();
		this.currentDirection = Direction.RIGHT;
		this.movementInterval = 0;
		this.nShipsWide = gameSettings.getFormationWidth();
		this.nShipsHigh = gameSettings.getFormationHeight();
		this.shootingInterval = gameSettings.getShootingFrecuency();
		this.shootingVariance = (int) (gameSettings.getShootingFrecuency() * SHOOTING_VARIANCE);
		this.baseSpeed = gameSettings.getBaseSpeed();
		this.movementSpeed = this.baseSpeed;
		this.positionX = INIT_POS_X;
		this.positionY = INIT_POS_Y;
		this.shooters = new ArrayList<>();
		this.gameState = gameState;

		this.logger.info("Initializing " + nShipsWide + "x" + nShipsHigh
				+ " ship formation in (" + positionX + "," + positionY + ")");

		// 적 우주선 초기화
		initializeEnemyShips();

		// 형상 크기 계산
		calculateFormationDimensions();

		// 슈터 리스트 초기화
		initializeShooters();
	}

	// 적 우주선 초기화 메서드
	private void initializeEnemyShips() {
		for (int i = 0; i < this.nShipsWide; i++) {
			this.enemyShips.add(new ArrayList<>());
		}

		for (List<EnemyShip> column : this.enemyShips) {
			for (int i = 0; i < this.nShipsHigh; i++) {
				SpriteType spriteType = determineSpriteType(i);
				column.add(new EnemyShip(
						(SEPARATION_DISTANCE * this.enemyShips.indexOf(column)) + positionX,
						(SEPARATION_DISTANCE * i) + positionY,
						spriteType, gameState));
				this.shipCount++;
			}
		}
	}

	// 적 우주선의 SpriteType 결정 메서드
	private SpriteType determineSpriteType(int index) {
		float proportion = index / (float) this.nShipsHigh;
		if (proportion < PROPORTION_E) {
			return SpriteType.EnemyShipE1;
		} else if (proportion < PROPORTION_E + PROPORTION_D) {
			return SpriteType.EnemyShipD1;
		} else if (proportion < PROPORTION_E + PROPORTION_D + PROPORTION_C) {
			return SpriteType.EnemyShipC1;
		} else if (proportion < PROPORTION_E + PROPORTION_D + PROPORTION_C + PROPORTION_B) {
			return SpriteType.EnemyShipB1;
		} else {
			return SpriteType.EnemyShipA1;
		}
	}

	// 형상 크기 계산 메서드
	private void calculateFormationDimensions() {
		this.shipWidth = this.enemyShips.get(0).get(0).getWidth();
		this.shipHeight = this.enemyShips.get(0).get(0).getHeight();
		this.width = (this.nShipsWide - 1) * SEPARATION_DISTANCE + this.shipWidth;
		this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE + this.shipHeight;
	}

	// 슈터 리스트 초기화 메서드
	private void initializeShooters() {
		for (List<EnemyShip> column : this.enemyShips) {
			this.shooters.add(column.get(column.size() - 1));
		}
	}


	/**
	 * Associates the formation to a given screen.
	 * 
	 * @param newScreen
	 *            Screen to attach.
	 */
	public final void attach(final Screen newScreen) {
		screen = newScreen;
	}

	/**
	 * Draws every individual component of the formation.
	 */
	public final void draw() {
		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				if (enemyShip != null)
				    drawManager.drawEntity(enemyShip, enemyShip.getPositionX(), enemyShip.getPositionY());
	}

	/**
	 * Draws every individual component of the formation for two player mode.
	 */
	public final void draw(final int playerNumber) {
		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				if (enemyShip != null)
					drawManager.drawEntity(enemyShip, enemyShip.getPositionX(),
							enemyShip.getPositionY(), playerNumber);
	}

	/**
	 * Updates the position of the ships.
	 */
	public final void update() {
		initializeShootingCooldown();
		adjustFormationBounds();

		if (shouldMoveFormation()) {
			updateMovement();
			clearDestroyedShips();
			updateEnemyShips();
		}
	}

	// 사격 쿨다운 초기화 메서드
	private void initializeShootingCooldown() {
		if (this.shootingCooldown == null) {
			this.shootingCooldown = Core.getVariableCooldown(shootingInterval, shootingVariance);
			this.shootingCooldown.reset();
		}
	}

	// 움직임이 필요한지 확인하는 메서드
	private boolean shouldMoveFormation() {
		movementInterval++;
		return movementInterval >= calculateMovementSpeed();
	}

	// 현재 대형의 속도 계산
	private int calculateMovementSpeed() {
		double remainingProportion = (double) this.shipCount / (this.nShipsHigh * this.nShipsWide);
		this.movementSpeed = this.baseSpeed + MINIMUM_SPEED;
		return this.movementSpeed;
	}

	// 대형 움직임 업데이트 메서드
	private void updateMovement() {
		movementInterval = 0;

		Direction nextDirection = determineNextDirection();
		applyMovement(nextDirection);
	}

	// 다음 이동 방향 결정
	private Direction determineNextDirection() {
		if (isMovingDown() && isAtHorizontalAltitude()) {
			return getNextHorizontalDirection();
		} else if (isAtLeftSide() && currentDirection == Direction.LEFT) {
			return isAtBottom() ? Direction.RIGHT : Direction.DOWN;
		} else if (isAtRightSide() && currentDirection == Direction.RIGHT) {
			return isAtBottom() ? Direction.LEFT : Direction.DOWN;
		}
		return currentDirection;
	}

	// 대형이 아래로 이동 중인지 확인
	private boolean isMovingDown() {
		return currentDirection == Direction.DOWN;
	}

	// 화면 하단에 도달했는지 확인
	private boolean isAtBottom() {
		return positionY + this.height > screen.getHeight() - BOTTOM_MARGIN;
	}

	// 오른쪽 경계에 도달했는지 확인
	private boolean isAtRightSide() {
		return positionX + this.width >= screen.getWidth() - SIDE_MARGIN;
	}

	// 왼쪽 경계에 도달했는지 확인
	private boolean isAtLeftSide() {
		return positionX <= SIDE_MARGIN;
	}

	// 수평적으로 정확히 하강 중인지 확인
	private boolean isAtHorizontalAltitude() {
		return positionY % DESCENT_DISTANCE == 0;
	}

	// 이전 방향에 따라 다음 수평 방향 결정
	private Direction getNextHorizontalDirection() {
		return (previousDirection == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
	}


	// 움직임 적용
	private void applyMovement(Direction nextDirection) {
		int movementX = 0;
		int movementY = 0;

		if (nextDirection == Direction.RIGHT) {
			movementX = X_SPEED;
		} else if (nextDirection == Direction.LEFT) {
			movementX = -X_SPEED;
		} else if (nextDirection == Direction.DOWN) {
			movementY = Y_SPEED;
		}

		positionX += movementX;
		positionY += movementY;
		currentDirection = nextDirection;

		logger.info("Formation now moving " + currentDirection);
	}

	// 파괴된 적 우주선 제거 메서드
	private void clearDestroyedShips() {
		for (int i = 0; i < this.enemyShips.size(); i++) {
			for (int j = 0; j < this.enemyShips.get(i).size(); j++) {
				EnemyShip ship = this.enemyShips.get(i).get(j);
				if (ship != null && ship.isDestroyed()) {
					logger.info("Removed enemy " + j + " from column " + i);
					this.enemyShips.get(i).set(j, null);
				}
			}
		}
	}

	// 개별 적 우주선 업데이트 메서드
	private void updateEnemyShips() {
		for (List<EnemyShip> column : this.enemyShips) {
			for (EnemyShip enemyShip : column) {
				if (enemyShip != null) {
					enemyShip.move(getMovementX(), getMovementY());
					enemyShip.update();
				}
			}
		}
	}

	// 현재 X축 움직임 반환
	private int getMovementX() {
		return (currentDirection == Direction.RIGHT) ? X_SPEED
				: (currentDirection == Direction.LEFT) ? -X_SPEED : 0;
	}

	// 현재 Y축 움직임 반환
	private int getMovementY() {
		return (currentDirection == Direction.DOWN) ? Y_SPEED : 0;
	}

	/**
	 * Adjusts the width and height of the formation.
	 */
	private void adjustFormationBounds() {
		int leftMostPoint = Integer.MAX_VALUE;
		int rightMostPoint = Integer.MIN_VALUE;
		int minPositionY = Integer.MAX_VALUE;
		int maxColumnHeight = 0;

		// 적 대형의 모든 열을 한 번에 처리
		for (List<EnemyShip> column : this.enemyShips) {
			// 열에서 경계값 계산
			Bounds bounds = calculateColumnBounds(column);

			if (bounds != null) {
				leftMostPoint = Math.min(leftMostPoint, bounds.leftX);
				rightMostPoint = Math.max(rightMostPoint, bounds.rightX);
				minPositionY = Math.min(minPositionY, bounds.topY);
				maxColumnHeight = Math.max(maxColumnHeight, bounds.height);
			}
		}

		// 대형의 경계 업데이트
		this.width = rightMostPoint - leftMostPoint + this.shipWidth;
		this.height = maxColumnHeight;
		this.positionX = leftMostPoint;
		this.positionY = minPositionY;
	}

	// 열의 경계값 계산 메서드
	private Bounds calculateColumnBounds(List<EnemyShip> column) {
		// 열이 비어있거나 모든 우주선이 null이면 null 반환
		if (column == null || column.stream().allMatch(Objects::isNull)) {
			return null;
		}

		EnemyShip firstNonNull = null;
		EnemyShip lastNonNull = null;

		// 첫 번째와 마지막 유효 우주선을 찾음
		for (EnemyShip ship : column) {
			if (ship != null) {
				if (firstNonNull == null) {
					firstNonNull = ship;
				}
				lastNonNull = ship;
			}
		}

		// 경계값 계산 및 반환
		int leftX = firstNonNull.getPositionX();
		int rightX = lastNonNull.getPositionX();
		int topY = firstNonNull.getPositionY();
		int height = lastNonNull.getPositionY() - this.positionY + this.shipHeight;

		return new Bounds(leftX, rightX, topY, height);
	}

	// 경계값을 담는 클래스
	private static class Bounds {
		int leftX, rightX, topY, height;

		Bounds(int leftX, int rightX, int topY, int height) {
			this.leftX = leftX;
			this.rightX = rightX;
			this.topY = topY;
			this.height = height;
		}
	}


	/**
	 * Shoots a bullet downwards.
	 * 
	 * @param bullets
	 *            Bullets set to add the bullet being shot.
	 */
	public final void shoot(final Set<Bullet> bullets, int level, float balance) {
		// Increasing the number of projectiles per level 3 (levels 1 to 3, 4 to 6, 2, 7 to 9, etc.)
		int numberOfShooters = Math.min((level / 3) + 1, this.shooters.size());
		int numberOfBullets = (level / 3) + 1;

		// Randomly select enemy to fire in proportion to the level
		List<EnemyShip> selectedShooters = new ArrayList<>();
		for (int i = 0; i < numberOfShooters; i++) {
			int index = (int) (Math.random() * this.shooters.size());
			selectedShooters.add(this.shooters.get(index));
		}

		// Fire when the cool down is over
		if (this.shootingCooldown.checkFinished()) {
			this.shootingCooldown.reset();

			// Each selected enemy fires a bullet
			for (EnemyShip shooter : selectedShooters) {
				// One shot at the base
				bullets.add(BulletPool.getBullet(shooter.getPositionX()
						+ shooter.width / 2 + 10, shooter.getPositionY(), BULLET_SPEED));

				// Additional launches based on levels (more launches based on each level)
				for (int i = 1; i < numberOfBullets; i++) {
					bullets.add(BulletPool.getBullet(shooter.getPositionX()
							+ shooter.width / 2 + (10 * (i + 1)), shooter.getPositionY(), BULLET_SPEED));
				}
				soundManager.playSound(Sound.ALIEN_LASER, balance);
			}
		}
	}

	/**
	 * Destroys a ship.
	 * 
	 * @param destroyedShip
	 *            Ship to be destroyed.
	 * @param balance
	 *            1p -1.0, 2p 1.0, both 0.0
	 */
	public final void destroy(final EnemyShip destroyedShip, final float balance) {
		for (List<EnemyShip> column : this.enemyShips)
			for (int i = 0; i < column.size(); i++)
				if (column.get(i) != null && column.get(i).equals(destroyedShip)) {
					column.get(i).destroy(balance);
					this.logger.info("Destroyed ship in ("
							+ this.enemyShips.indexOf(column) + "," + i + ")");
				}

		// Updates the list of ships that can shoot the player.
		if (this.shooters.contains(destroyedShip)) {
			int destroyedShipIndex = this.shooters.indexOf(destroyedShip);
			int destroyedShipColumnIndex = -1;

			for (List<EnemyShip> column : this.enemyShips)
				if (column.contains(destroyedShip)) {
					destroyedShipColumnIndex = this.enemyShips.indexOf(column);
					break;
				}

			EnemyShip nextShooter = getNextShooter(this.enemyShips
					.get(destroyedShipColumnIndex));

			if (nextShooter != null)
				this.shooters.set(destroyedShipIndex, nextShooter);
			else {
				this.shooters.remove(destroyedShipIndex);
				this.logger.info("Shooters list reduced to "
						+ this.shooters.size() + " members.");
			}
		}

		this.shipCount--;
	}

	public final void HealthManageDestroy(final EnemyShip destroyedShip, final float balance) {
		for (List<EnemyShip> column : this.enemyShips)
			for (int i = 0; i < column.size(); i++)
				if (column.get(i) != null && column.get(i).equals(destroyedShip)) {
					//If health is 0, number of remaining enemy ships--, score awarded, number of destroyed ships++
					if(destroyedShip.getHealth() <= 0){
						this.shipCount--;
						this.logger.info("Destroyed ship in ("
								+ this.enemyShips.indexOf(column) + "," + i + ")");
						point = destroyedShip.getPointValue();
						distroyedship = 1;
						destroyedShip.setHealth(destroyedShip.getHealth() - 1);
					}else{
						point = 0;
						distroyedship = 0;
					}
					column.get(i).HealthManageDestroy(balance);
				}

		// Updates the list of ships that can shoot the player.
		if (this.shooters.contains(destroyedShip)) {
			int destroyedShipIndex = this.shooters.indexOf(destroyedShip);
			int destroyedShipColumnIndex = -1;

			for (List<EnemyShip> column : this.enemyShips)
				if (column.contains(destroyedShip)) {
					destroyedShipColumnIndex = this.enemyShips.indexOf(column);
					break;
				}

			EnemyShip nextShooter = getNextShooter(this.enemyShips
					.get(destroyedShipColumnIndex));

			if (nextShooter != null)
				this.shooters.set(destroyedShipIndex, nextShooter);
			else {
				this.shooters.remove(destroyedShipIndex);
				this.logger.info("Shooters list reduced to "
						+ this.shooters.size() + " members.");
			}
		}
	}

	/**
	 * Gets the ship on a given column that will be in charge of shooting.
	 * 
	 * @param column
	 *            Column to search.
	 * @return New shooter ship.
	 */
	public final EnemyShip getNextShooter(final List<EnemyShip> column) {
		Iterator<EnemyShip> iterator = column.iterator();
		EnemyShip nextShooter = null;
		while (iterator.hasNext()) {
			EnemyShip checkShip = iterator.next();
			if (checkShip != null && !checkShip.isDestroyed())
				nextShooter = checkShip;
		}

		return nextShooter;
	}

	/**
	 * Returns an iterator over the ships in the formation.
	 * 
	 * @return Iterator over the enemy ships.
	 */
	@Override
	public final Iterator<EnemyShip> iterator() {
		Set<EnemyShip> enemyShipsList = new HashSet<EnemyShip>();

		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				enemyShipsList.add(enemyShip);

		return enemyShipsList.iterator();
	}

	/**
	 * Checks if there are any ships remaining.
	 * 
	 * @return True when all ships have been destroyed.
	 */
	public final boolean isEmpty() {
		return this.shipCount <= 0;
	}


	public int getPoint(){return point; }

	public int getDistroyedship(){return distroyedship; }

	public List<List<EnemyShip>> getEnemyShips() {return enemyShips; }
}
