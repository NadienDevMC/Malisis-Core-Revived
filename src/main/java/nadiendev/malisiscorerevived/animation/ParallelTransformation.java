package nadiendev.malisiscorerevived.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a list of transformations at the same time.
 *
 * @author Ordinastie
 */
public class ParallelTransformation extends Transformation<ParallelTransformation>
{
	protected final List<Transformation<?>> listTransformations = new ArrayList<>();

	public ParallelTransformation(Transformation<?>... transformations)
	{
		addTransformations(transformations);
	}

	@Override
	public ParallelTransformation self()
	{
		return this;
	}

	public ParallelTransformation addTransformations(Transformation<?>... transformations)
	{
		for (Transformation<?> transformation : transformations)
		{
			duration = Math.max(duration, transformation.totalDuration());
			listTransformations.add(transformation);
		}
		return this;
	}

	@Override
	protected float completion(float elapsedTime)
	{
		float comp = super.completion(elapsedTime);
		if (comp >= 1 && elapsedTimeCurrentLoop < duration)
			elapsedTimeCurrentLoop = duration;
		return comp;
	}

	@Override
	protected void doTransform(Transformable transformable, float comp)
	{
		for (Transformation<?> transformation : listTransformations)
			transformation.transform(transformable, elapsedTimeCurrentLoop);
	}

	/**
	 * Reverses all the transformations of this parallel transformation.
	 */
	@Override
	public ParallelTransformation reversed(boolean reversed)
	{
		if (!reversed)
			return this;
		for (Transformation<?> transformation : listTransformations)
			transformation.reversed(true);
		return this;
	}
}
