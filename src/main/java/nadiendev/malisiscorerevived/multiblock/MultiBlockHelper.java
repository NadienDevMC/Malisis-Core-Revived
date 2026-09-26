package nadiendev.malisiscorerevived.multiblock;

import javax.annotation.Nullable;

import nadiendev.malisiscorerevived.registry.MalisisCoreRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * Helpers to place and break multi-blocks made of an origin block implementing {@link IMultiBlockController} and
 * {@link MultiBlockPartBlock parts}.
 *
 * @author Ordinastie
 */
public class MultiBlockHelper
{
	/**
	 * Checks whether all the positions can be replaced by the multi-block.
	 *
	 * @param context the placement context (the clicked position is always considered replaceable, but must be free of entities)
	 * @param positions the positions occupied by the multi-block
	 * @return true if the multi-block can be placed
	 */
	public static boolean canPlace(BlockPlaceContext context, Iterable<BlockPos> positions)
	{
		Level level = context.getLevel();
		for (BlockPos pos : positions)
		{
			//no entity in the way
			if (!level.isUnobstructed(null, Shapes.block().move(pos.getX(), pos.getY(), pos.getZ())))
				return false;
			if (pos.equals(context.getClickedPos()))
				continue;
			if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos))
				return false;
			BlockState state = level.getBlockState(pos);
			if (!state.canBeReplaced(context))
				return false;
		}
		return true;
	}

	/**
	 * Places the parts of a multi-block.
	 *
	 * @param level the level
	 * @param origin the origin of the multi-block (not replaced)
	 * @param positions the positions occupied by the multi-block
	 */
	public static void placeParts(Level level, BlockPos origin, Iterable<BlockPos> positions)
	{
		BlockState partState = MalisisCoreRegistries.MULTIBLOCK_PART.get().defaultBlockState();
		for (BlockPos pos : positions)
		{
			if (pos.equals(origin))
				continue;
			pos = pos.immutable();
			level.setBlock(pos, partState, Block.UPDATE_ALL);
			if (level.getBlockEntity(pos) instanceof MultiBlockPartBlockEntity be)
			{
				be.setOrigin(origin);
				level.sendBlockUpdated(pos, partState, partState, Block.UPDATE_ALL);
			}
		}
	}

	/**
	 * Removes the parts of a multi-block (without drops).
	 *
	 * @param level the level
	 * @param origin the origin of the multi-block
	 * @param positions the positions occupied by the multi-block
	 */
	public static void removeParts(Level level, BlockPos origin, Iterable<BlockPos> positions)
	{
		for (BlockPos pos : positions)
		{
			if (pos.equals(origin))
				continue;
			if (origin.equals(getOrigin(level, pos)))
				level.setBlock(pos.immutable(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
		}
	}

	/**
	 * Gets the origin of the multi-block the part at pos belongs to.
	 *
	 * @return the origin position, or null if the block at pos is not a part
	 */
	@Nullable
	public static BlockPos getOrigin(BlockGetter level, BlockPos pos)
	{
		if (!(level.getBlockEntity(pos) instanceof MultiBlockPartBlockEntity be))
			return null;
		return be.getOrigin();
	}

	/**
	 * Gets the origin of the multi-block the block at pos belongs to, or pos itself if the block is not a part.
	 */
	public static BlockPos getOriginOrSelf(BlockGetter level, BlockPos pos)
	{
		BlockPos origin = getOrigin(level, pos);
		return origin != null ? origin : pos;
	}

	/**
	 * Checks whether the block at pos is a part of the multi-block with the specified origin.
	 */
	public static boolean isPartOf(BlockGetter level, BlockPos pos, BlockPos origin)
	{
		return origin.equals(getOrigin(level, pos));
	}
}
