package nadiendev.malisiscorerevived.multiblock;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultiBlockPartBlock extends Block implements EntityBlock, LiquidBlockContainer
{
	public static final MapCodec<MultiBlockPartBlock> CODEC = simpleCodec(MultiBlockPartBlock::new);

	public MultiBlockPartBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new MultiBlockPartBlockEntity(pos, state);
	}

	public static @Nullable BlockPos getControllerPos(BlockGetter level, BlockPos pos)
	{
		BlockPos origin = MultiBlockHelper.getOrigin(level, pos);
		if (origin == null)
			return null;
		return level.getBlockState(origin).getBlock() instanceof IMultiBlockController ? origin : null;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.INVISIBLE;
	}

	private VoxelShape getShape(BlockGetter level, BlockPos pos, CollisionContext context, boolean collision)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return collision ? Shapes.empty() : Shapes.block();
		BlockState originState = level.getBlockState(origin);
		return ((IMultiBlockController) originState.getBlock()).getPartShape(originState, level, origin, pos, context, collision);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return getShape(level, pos, context, false);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return getShape(level, pos, context, true);
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state)
	{
		return true;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos)
	{
		return 1.0F;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return InteractionResult.PASS;
		BlockState originState = level.getBlockState(origin);
		return ((IMultiBlockController) originState.getBlock()).usePart(originState, level, origin, pos, player, hit);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		BlockState originState = level.getBlockState(origin);
		return ((IMultiBlockController) originState.getBlock()).useItemOnPart(stack, originState, level, origin, pos, player, hand, hit);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return;
		BlockState originState = level.getBlockState(origin);
		((IMultiBlockController) originState.getBlock()).onPartNeighborChanged(originState, level, origin, pos, neighborBlock, orientation);
	}

	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return 1.0F;
		return level.getBlockState(origin).getDestroyProgress(player, level, origin);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return ItemStack.EMPTY;
		return level.getBlockState(origin).getCloneItemStack(origin, level, includeData, player);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin != null && !level.isClientSide() && (player.isCreative() || !player.hasCorrectToolForDrops(level.getBlockState(origin))))
			level.removeBlock(origin, false);
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public boolean canPlaceLiquid(@Nullable LivingEntity entity, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState)
	{
		return false;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type)
	{
		return false;
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return false;
		BlockState originState = level.getBlockState(origin);
		return ((IMultiBlockController) originState.getBlock()).isPartLadder(originState, level, origin, pos, entity);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return super.getExplosionResistance(state, level, pos, explosion);
		return level.getBlockState(origin).getExplosionResistance(level, origin, explosion);
	}

	@Override
	public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity)
	{
		BlockPos origin = getControllerPos(level, pos);
		if (origin == null)
			return super.getSoundType(state, level, pos, entity);
		return level.getBlockState(origin).getSoundType(level, origin, entity);
	}
}
