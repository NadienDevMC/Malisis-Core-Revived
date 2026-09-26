package nadiendev.malisiscorerevived.util;

import net.minecraft.core.Direction;

/**
 * Helpers to deal with horizontal {@link Direction directions}.<br>
 * By convention, Malisis models and bounding boxes are defined facing {@link Direction#SOUTH} and rotated to the actual direction.
 *
 * @author Ordinastie
 */
public class DirectionUtils
{
	/**
	 * Gets the number of 90 degrees rotations (right-handed, around the Y axis) needed to go from {@link Direction#SOUTH} to the
	 * direction.
	 *
	 * @param direction the direction
	 * @return the rotation count (0 to 3)
	 */
	public static int getRotationCount(Direction direction)
	{
		return switch (direction)
		{
			case EAST -> 1;
			case NORTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
	}

	/**
	 * Gets the angle in degrees (right-handed, around the Y axis) needed to go from {@link Direction#SOUTH} to the direction.
	 */
	public static float getRotationAngle(Direction direction)
	{
		return getRotationCount(direction) * 90;
	}

	/**
	 * Gets the direction obtained after rotating the direction count times by 90 degrees (right-handed, around the Y axis).
	 */
	public static Direction rotate(Direction direction, int count)
	{
		if (direction.getAxis() == Direction.Axis.Y)
			return direction;
		count = Math.floorMod(count, 4);
		for (int i = 0; i < count; i++)
			direction = direction.getCounterClockWise();
		return direction;
	}
}
