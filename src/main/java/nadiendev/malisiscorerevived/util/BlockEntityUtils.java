package nadiendev.malisiscorerevived.util;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Helpers to deal with {@link BlockEntity block entities}.
 *
 * @author Ordinastie
 */
public class BlockEntityUtils
{
	/**
	 * Gets the {@link BlockEntity} of the specified type at the position.
	 *
	 * @return the block entity, or null if there is none or it's not of the right type
	 */
	@Nullable
	public static <T> T getBlockEntity(Class<T> clazz, @Nullable BlockGetter level, @Nullable BlockPos pos)
	{
		if (level == null || pos == null)
			return null;
		BlockEntity be = level.getBlockEntity(pos);
		return clazz.isInstance(be) ? clazz.cast(be) : null;
	}

	/**
	 * Marks the {@link BlockEntity} dirty and sends its data to the clients.
	 */
	public static void notifyUpdate(@Nullable BlockEntity be)
	{
		if (be == null || be.getLevel() == null)
			return;
		be.setChanged();
		if (!be.getLevel().isClientSide())
		{
			BlockState state = be.getBlockState();
			be.getLevel().sendBlockUpdated(be.getBlockPos(), state, state, 3);
		}
	}
}
