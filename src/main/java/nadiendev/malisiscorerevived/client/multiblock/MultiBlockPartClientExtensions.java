package nadiendev.malisiscorerevived.client.multiblock;

import org.jspecify.annotations.Nullable;

import nadiendev.malisiscorerevived.multiblock.MultiBlockPartBlock;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public class MultiBlockPartClientExtensions implements IClientBlockExtensions
{
	@Override
	public boolean addHitEffects(BlockState state, Level level, @Nullable HitResult target, ParticleEngine manager)
	{
		return true;
	}

	@Override
	public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager)
	{
		BlockPos origin = MultiBlockPartBlock.getControllerPos(level, pos);
		if (origin != null)
			level.addDestroyBlockEffect(pos, level.getBlockState(origin));
		return true;
	}
}
