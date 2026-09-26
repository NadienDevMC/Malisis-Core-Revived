package nadiendev.malisiscorerevived.animation;

/**
 * Scales the transformable.<br>
 * The scale is centered on the center of the block, offset by {@link #offset(float, float, float)}.
 *
 * @author Ordinastie
 */
public class Scale extends Transformation<Scale>
{
	protected float fromX = 1, fromY = 1, fromZ = 1;
	protected float toX = 1, toY = 1, toZ = 1;
	protected float offsetX = 0, offsetY = 0, offsetZ = 0;

	public Scale()
	{}

	public Scale(float x, float y, float z)
	{
		to(x, y, z);
	}

	public Scale(float fromX, float fromY, float fromZ, float toX, float toY, float toZ)
	{
		from(fromX, fromY, fromZ);
		to(toX, toY, toZ);
	}

	@Override
	public Scale self()
	{
		return this;
	}

	public Scale from(float x, float y, float z)
	{
		fromX = x;
		fromY = y;
		fromZ = z;
		return this;
	}

	public Scale to(float x, float y, float z)
	{
		toX = x;
		toY = y;
		toZ = z;
		return this;
	}

	public Scale offset(float x, float y, float z)
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
		transformable.scale(interpolate(fromX, toX, comp),
							interpolate(fromY, toY, comp),
							interpolate(fromZ, toZ, comp),
							offsetX,
							offsetY,
							offsetZ);
	}
}
