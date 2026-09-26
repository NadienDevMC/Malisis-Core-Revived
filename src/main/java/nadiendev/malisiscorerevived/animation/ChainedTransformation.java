package nadiendev.malisiscorerevived.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a list of transformations one after the other.
 *
 * @author Ordinastie
 */
public class ChainedTransformation extends Transformation<ChainedTransformation>
{
	protected final List<Transformation<?>> listTransformations = new ArrayList<>();

	public ChainedTransformation(Transformation<?>... transformations)
	{
		addTransformations(transformations);
	}

	@Override
	public ChainedTransformation self()
	{
		return this;
	}

	public ChainedTransformation addTransformations(Transformation<?>... transformations)
	{
		for (Transformation<?> transformation : transformations)
		{
			duration += transformation.totalDuration();
			listTransformations.add(transformation);
		}
		return this;
	}

	@Override
	protected float completion(float elapsedTime)
	{
		float comp = super.completion(elapsedTime);
		//when the chain is complete, make sure every transformation is complete too
		if (comp >= 1 && elapsedTimeCurrentLoop < duration)
			elapsedTimeCurrentLoop = duration;
		return comp;
	}

	@Override
	protected void doTransform(Transformable transformable, float comp)
	{
		if (listTransformations.isEmpty())
			return;

		float elapsed = elapsedTimeCurrentLoop;
		if (reversed)
			elapsed = Math.max(0, duration - elapsed);

		for (Transformation<?> transformation : listTransformations)
		{
			transformation.transform(transformable, elapsed);
			elapsed -= transformation.totalDuration();
		}
	}
}
