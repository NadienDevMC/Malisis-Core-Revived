package nadiendev.malisiscorerevived.util;

import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
			tag.store(key, BlockState.CODEC, state);
	}

	@Nullable
	public static BlockState read(CompoundTag tag, String key)
	{
		if (!tag.contains(key))
			return null;
		return tag.read(key, BlockState.CODEC).filter(state -> !state.isAir()).orElse(null);
	}

public static void write(ValueOutput output, String key, @Nullable BlockState state)
	{
		output.storeNullable(key, BlockState.CODEC, state);
	}

	public static @Nullable BlockState read(ValueInput input, String key)
	{
		return input.read(key, BlockState.CODEC).filter(state -> !state.isAir()).orElse(null);
	}

	public static BlockState read(ValueInput input, String key, BlockState defaultState)
	{
		BlockState state = read(input, key);
		return state != null ? state : defaultState;
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
