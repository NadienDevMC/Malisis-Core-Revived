package nadiendev.malisiscorerevived.animation;

/**
 * Base class for all the transformations.<br>
 * A transformation interpolates a value over a duration (in ticks) and applies it to a {@link Transformable}.<br>
 * Times are expressed in ticks as floats so that partial ticks can be used for smooth rendering.
 *
 * @author Ordinastie
 * @param <T> the actual type of transformation
 */
public abstract class Transformation<T extends Transformation<T>>
{
	public static final int LINEAR = 0, SINUSOIDAL = 1;

	protected int movement = LINEAR;
	protected float duration = 0;
	protected float delay = 0;
	protected int loops = 1;
	protected float loopStartDelay = 0, loopResetDelay = 0;
	protected float elapsedTimeCurrentLoop;
	protected boolean reversed = false;

	public T movement(int movement)
	{
		this.movement = movement;
		return self();
	}

	public T delay(int delay)
	{
		this.delay = delay;
		return self();
	}

	public T forTicks(int duration)
	{
		this.duration = duration;
		return self();
	}

	public T forTicks(int duration, int delay)
	{
		this.duration = duration;
		this.delay = delay;
		return self();
	}

	public float getDuration()
	{
		return duration;
	}

	public float getDelay()
	{
		return delay;
	}

	public int getLoops()
	{
		return loops;
	}

	public float totalDuration()
	{
		if (loops == -1)
			return Integer.MAX_VALUE;
		return delay + loops * getLoopDuration();
	}

	public float getLoopDuration()
	{
		return duration + loopStartDelay + loopResetDelay;
	}

	public T loop(int loops)
	{
		return loop(loops, 0, 0);
	}

	public T loop(int loops, int startDelay, int resetDelay)
	{
		if (loops == 0)
			return self();

		this.loops = loops;
		this.loopStartDelay = startDelay;
		this.loopResetDelay = resetDelay;
		return self();
	}

	public T reversed(boolean reversed)
	{
		this.reversed = reversed;
		return self();
	}

	public boolean isReversed()
	{
		return reversed;
	}

	/**
	 * Applies this transformation to the transformable for the specified elapsed time.
	 *
	 * @param transformable the transformable
	 * @param elapsedTime the elapsed time in ticks (partial ticks included)
	 */
	public void transform(Transformable transformable, float elapsedTime)
	{
		doTransform(transformable, completion(Math.max(0, elapsedTime)));
	}

	/**
	 * Calculates the completion (0 to 1) of this transformation for the elapsed time.
	 *
	 * @param elapsedTime the elapsed time in ticks
	 * @return the completion
	 */
	protected float completion(float elapsedTime)
	{
		elapsedTimeCurrentLoop = elapsedTime - delay;

		if (duration == 0)
			return elapsedTimeCurrentLoop >= 0 ? 1 : 0;

		float loopDuration = getLoopDuration();
		if (loops != -1 && elapsedTimeCurrentLoop > loops * loopDuration)
			return 1;

		if (loops != 1)
		{
			elapsedTimeCurrentLoop %= loopDuration;
			if (elapsedTimeCurrentLoop < loopStartDelay)
				return 0;
			if (elapsedTimeCurrentLoop + loopResetDelay > loopDuration)
				return 1;
			elapsedTimeCurrentLoop -= loopStartDelay;
		}

		float comp = Math.min(elapsedTimeCurrentLoop / duration, 1);
		comp = Math.max(0, Math.min(1, comp));
		if (movement == SINUSOIDAL)
			comp = (float) (1 - Math.cos(comp * Math.PI)) / 2;

		return comp;
	}

	protected static float interpolate(float from, float to, float comp)
	{
		return from + (to - from) * comp;
	}

	public abstract T self();

	protected abstract void doTransform(Transformable transformable, float comp);
}
