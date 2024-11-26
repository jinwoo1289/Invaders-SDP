package entity;

import java.awt.Color;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;
import engine.GameState;
import engine.Sound;
import engine.SoundManager;

/**
 * Implements a enemy ship, to be destroyed by the player.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class EnemyShip extends Entity {

	/** Point value of a type A enemy. */
	private static final int A_TYPE_POINTS = 10;
	/** Point value of a type B enemy. */
	private static final int B_TYPE_POINTS = 20;
	/** Point value of a type C enemy. */
	private static final int C_TYPE_POINTS = 30;
	/** Point value of a type D enemy. */
	private static final int D_TYPE_POINTS = 40;
	/** Point value of a type E enemy. */
	private static final int E_TYPE_POINTS = 50;
	/** Point value of a type F enemy*/
	private static final int F_TYPE_POINTS = 60;
	/** Point value of a bonus enemy. */
	private static final int BONUS_TYPE_POINTS = 100;

	/** Cooldown between sprite changes. */
	private Cooldown animationCooldown;
	/** Checks if the ship has been hit by a bullet. */
	private boolean isDestroyed;
	/** Values of the ship, in points, when destroyed. */
	private int pointValue;

	/** Singleton instance of SoundManager */
	private final SoundManager soundManager = SoundManager.getInstance();

	private int health;

	/**
	 * Constructor, establishes the ship's properties.
	 *
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 * @param spriteType
	 *            Sprite type, image corresponding to the ship.
	 */

	public EnemyShip(final int positionX, final int positionY, final SpriteType spriteType, final GameState gameState) {
		super(positionX, positionY, 24, 16, getDefaultColor(spriteType));

		this.spriteType = spriteType;
		this.animationCooldown = Core.getCooldown(500);
		this.isDestroyed = false;

		this.health = Math.max(1, gameState.getLevel() / 3); // 최소 1 이상의 헬스 설정
		this.pointValue = calculatePointValue(spriteType, gameState);
	}
	/**
	 * Constructor, establishes the ship's properties for a special ship, with
	 * known starting properties.
	 */
	public EnemyShip() {
		super(-32, 60, 32, 14, Color.RED);
		this.spriteType = SpriteType.EnemyShipSpecial;
		this.isDestroyed = false;
		this.pointValue = BONUS_TYPE_POINTS;
	}

	private int calculatePointValue(SpriteType spriteType, GameState gameState) {
		int baseValue = switch (spriteType) {
			case EnemyShipA1, EnemyShipA2 -> A_TYPE_POINTS;
			case EnemyShipB1, EnemyShipB2 -> B_TYPE_POINTS;
			case EnemyShipC1, EnemyShipC2 -> C_TYPE_POINTS;
			case EnemyShipD1, EnemyShipD2 -> D_TYPE_POINTS;
			case EnemyShipE1, EnemyShipE2 -> E_TYPE_POINTS;
			case EnemyShipF1 -> F_TYPE_POINTS;
			default -> 0;
		};
		return baseValue + (int) (gameState.getLevel() * 0.1) + Core.getLevelSetting();
	}

	public static Color getDefaultColor(SpriteType spriteType) {
		return switch (spriteType) {
			case EnemyShipA1, EnemyShipA2 -> Color.RED;
			case EnemyShipB1, EnemyShipB2 -> Color.GREEN;
			case EnemyShipC1, EnemyShipC2 -> Color.BLUE;
			case EnemyShipD1, EnemyShipD2 -> Color.YELLOW;
			case EnemyShipE1, EnemyShipE2 -> Color.ORANGE;
			default -> Color.WHITE;
		};
	}
	/**
	 * Getter for the score bonus if this ship is destroyed.
	 *
	 * @return Value of the ship.
	 */
	public final int getPointValue() {
		return this.pointValue;
	}

	/**
	 * Moves the ship the specified distance.
	 *
	 * @param distanceX
	 *            Distance to move in the X axis.
	 * @param distanceY
	 *            Distance to move in the Y axis.
	 */
	public final void move(final int distanceX, final int distanceY) {
		this.positionX += distanceX;
		this.positionY += distanceY;
	}

	/**
	 * Updates attributes, mainly used for animation purposes.
	 */
	public final void update() {
		if (this.animationCooldown.checkFinished()) {
			this.animationCooldown.reset();
			toggleSprite();
		}
	}

	private void toggleSprite() {
		this.spriteType = switch (this.spriteType) {
			case EnemyShipA1 -> SpriteType.EnemyShipA2;
			case EnemyShipA2 -> SpriteType.EnemyShipA1;
			case EnemyShipB1 -> SpriteType.EnemyShipB2;
			case EnemyShipB2 -> SpriteType.EnemyShipB1;
			case EnemyShipC1 -> SpriteType.EnemyShipC2;
			case EnemyShipC2 -> SpriteType.EnemyShipC1;
			case EnemyShipD1 -> SpriteType.EnemyShipD2;
			case EnemyShipD2 -> SpriteType.EnemyShipD1;
			case EnemyShipE1 -> SpriteType.EnemyShipE2;
			case EnemyShipE2 -> SpriteType.EnemyShipE1;
			default -> this.spriteType;
		};
	}

	/**
	 * Destroys the ship, causing an explosion.
	 *
	 * @param balance 1p -1.0, 2p 1.0, both 0.0
	 */
	public final void destroy(final float balance) {
		this.isDestroyed = true;
		this.spriteType = SpriteType.Explosion;
		soundManager.playSound(Sound.ALIEN_HIT, balance);
	}

	public final void HealthManageDestroy(final float balance) { //Determine whether to destroy the enemy ship based on its health
		if(this.health <= 0){
			this.isDestroyed = true;
			this.spriteType = SpriteType.Explosion;
		}else{
			this.health--;
		}
		soundManager.playSound(Sound.ALIEN_HIT, balance);
	}

	public int getHealth() {
		return this.health;
	}
	public void setHealth(int health) {
		this.health = health;
	}

	/**
	 * Checks if the ship has been destroyed.
	 *
	 * @return True if the ship has been destroyed.
	 */
	public final boolean isDestroyed() {
		return this.isDestroyed;
	}
}