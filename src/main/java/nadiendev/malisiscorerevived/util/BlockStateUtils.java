package nadiendev.malisiscorerevived.util;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Helpers to read and write {@link BlockState block states} from/to NBT.
 *
 * @author Ordinastie
 */
public class BlockStateUtils
{
	public static void write(CompoundTag tag, String key, @Nullable BlockState state)
	{
		if (state != null)
			tag.put(key, NbtUtils.writeBlockState(state));
	}

	@Nullable
	public static BlockState read(CompoundTag tag, String key)
	{
		if (!tag.contains(key))
			return null;
		BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(key));
		return state.isAir() ? null : state;
	}

	public static BlockState read(CompoundTag tag, String key, BlockState defaultState)
	{
		BlockState state = read(tag, key);
		return state != null ? state : defaultState;
	}

	/**
	 * Gets the {@link BlockState} placed by the block item, if any.
	 */
	@Nullable
	public static BlockState fromBlock(@Nullable Block block)
	{
		return block != null ? block.defaultBlockState() : null;
	}
}
