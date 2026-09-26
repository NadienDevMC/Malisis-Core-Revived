package nadiendev.malisiscorerevived.animation;

/**
 * Changes the alpha (0-255) used to render the transformable.
 *
 * @author Ordinastie
 */
public class AlphaTransform extends Transformation<AlphaTransform>
{
	protected int fromAlpha;
	protected int toAlpha;

	public AlphaTransform(int fromAlpha, int toAlpha)
	{
		this.fromAlpha = fromAlpha;
		this.toAlpha = toAlpha;
	}

	@Override
	public AlphaTransform self()
	{
		return this;
	}

	public AlphaTransform from(int alpha)
	{
		this.fromAlpha = alpha;
		return this;
	}

	public AlphaTransform to(int alpha)
	{
		this.toAlpha = alpha;
		return this;
	}

	@Override
	protected void doTransform(Transformable transformable, float comp)
	{
		if (reversed)
			comp = 1 - comp;
		transformable.setAlpha((int) interpolate(fromAlpha, toAlpha, comp));
	}
}
