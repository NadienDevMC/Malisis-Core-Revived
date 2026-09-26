package nadiendev.malisiscorerevived.animation;

/**
 * Rotates the transformable around an axis.<br>
 * The axis goes through the center of the block, offset by {@link #offset(float, float, float)}.
 *
 * @author Ordinastie
 */
public class Rotation extends Transformation<Rotation>
{
	protected float fromAngle;
	protected float toAngle;
	protected float axisX, axisY, axisZ;
	protected float offsetX, offsetY, offsetZ;

	public Rotation(float angle)
	{
		to(angle);
	}

	public Rotation(float fromAngle, float toAngle)
	{
		from(fromAngle);
		to(toAngle);
	}

	public Rotation(float angle, float axisX, float axisY, float axisZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
	}

	public Rotation(float angle, float axisX, float axisY, float axisZ, float offsetX, float offsetY, float offsetZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
		offset(offsetX, offsetY, offsetZ);
	}

	@Override
	public Rotation self()
	{
		return this;
	}

	public Rotation from(float angle)
	{
		fromAngle = angle;
		return this;
	}

	public Rotation to(float angle)
	{
		toAngle = angle;
		return this;
	}

	public Rotation aroundAxis(float x, float y, float z)
	{
		axisX = x;
		axisY = y;
		axisZ = z;
		return this;
	}

	public Rotation offset(float x, float y, float z)
	{
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		return this;
	}

	@Override
	protected void doTransform(Transformable transformable, float comp)
	{
		if (reversed)
			comp = 1 - comp;
		transformable.rotate(interpolate(fromAngle, toAngle, comp), axisX, axisY, axisZ, offsetX, offsetY, offsetZ);
	}
}
