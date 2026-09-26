package nadiendev.malisiscorerevived.util;

import net.minecraft.world.level.Level;

/**
 * Tick based timer.<br>
 * The timer uses the game time of the level it's used in, so it pauses with the game and stays consistent between client and server.
 *
 * @author Ordinastie
 */
public class Timer
{
	private long start;

	public Timer()
	{
		this(0);
	}

	public Timer(long start)
	{
		this.start = start;
	}

	/**
	 * Starts the timer at the current game time of the level.
	 */
	public void start(Level level)
	{
		start = level != null ? level.getGameTime() : 0;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	/**
	 * Sets the start of this timer relative to the current game time of the level.
	 *
	 * @param level the level
	 * @param offset the offset in ticks (negative to start in the past)
	 */
	public void setRelativeStart(Level level, long offset)
	{
		start = (level != null ? level.getGameTime() : 0) + offset;
	}

	public long getStart()
	{
		return start;
	}

	/**
	 * @return the number of full ticks elapsed since the start of this timer
	 */
	public long elapsedTick(Level level)
	{
		return level != null ? level.getGameTime() - start : 0;
	}

	/**
	 * @return the number of ticks elapsed since the start of this timer, including the partial tick (for rendering)
	 */
	public float elapsedTime(Level level, float partialTick)
	{
		return elapsedTick(level) + partialTick;
	}

	@Override
	public String toString()
	{
		return "Timer[start=" + start + "]";
	}
}
