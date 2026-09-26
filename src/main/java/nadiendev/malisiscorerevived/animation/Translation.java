package nadiendev.malisiscorerevived.animation;

/**
 * Translates the transformable from a position to another.
 *
 * @author Ordinastie
 */
public class Translation extends Transformation<Translation>
{
	protected float fromX, fromY, fromZ;
	protected float toX, toY, toZ;

	public Translation(float x, float y, float z)
	{
		to(x, y, z);
	}

	public Translation(float fromX, float fromY, float fromZ, float toX, float toY, float toZ)
	{
		from(fromX, fromY, fromZ);
		to(toX, toY, toZ);
	}

	@Override
	public Translation self()
	{
		return this;
	}

	public Translation from(float x, float y, float z)
	{
		fromX = x;
		fromY = y;
		fromZ = z;
		return this;
	}

	public Translation to(float x, float y, float z)
	{
		toX = x;
		toY = y;
		toZ = z;
		return this;
	}

	@Override
	protected void doTransform(Transformable transformable, float comp)
	{
		if (reversed)
			comp = 1 - comp;
		transformable.translate(interpolate(fromX, toX, comp), interpolate(fromY, toY, comp), interpolate(fromZ, toZ, comp));
	}
}
