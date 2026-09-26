package nadiendev.malisiscorerevived.multiblock;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Implemented by the {@link net.minecraft.world.level.block.Block} at the origin of a multi-block.<br>
 * The {@link MultiBlockPartBlock parts} of the multi-block delegate their behavior to it.
 *
 * @author Ordinastie
 */
public interface IMultiBlockController
{
	/**
	 * Gets the shape of the part at partPos, relative to partPos.
	 *
	 * @param originState the state of the origin block
	 * @param level the level
	 * @param origin the position of the origin
	 * @param partPos the position of the part
	 * @param context the collision context
	 * @param collision whether the collision shape is requested (otherwise, the outline shape)
	 * @return the shape
	 */
	VoxelShape getPartShape(BlockState originState, BlockGetter level, BlockPos origin, BlockPos partPos, CollisionContext context, boolean collision);

	/**
	 * Called when a part is right-clicked with an empty hand.<br>
	 * By default, calls the origin block as if it was clicked directly.
	 */
	default InteractionResult usePart(BlockState originState, Level level, BlockPos origin, BlockPos partPos, Player player, BlockHitResult hit)
	{
		return originState.useWithoutItem(level, player, hit.withPosition(origin));
	}

	/**
	 * Called when a part is right-clicked with an item.<br>
	 * By default, calls the origin block as if it was clicked directly.
	 */
	default InteractionResult useItemOnPart(ItemStack stack, BlockState originState, Level level, BlockPos origin, BlockPos partPos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		return originState.useItemOn(stack, level, player, hand, hit.withPosition(origin));
	}

	/**
	 * Called when a neighbor of a part changes (redstone for example).
	 */
	default void onPartNeighborChanged(BlockState originState, Level level, BlockPos origin, BlockPos partPos, Block neighborBlock, @Nullable Orientation orientation)
	{}

	/**
	 * Whether the part at partPos can be climbed.
	 */
	default boolean isPartLadder(BlockState originState, LevelReader level, BlockPos origin, BlockPos partPos, LivingEntity entity)
	{
		return false;
	}
}
