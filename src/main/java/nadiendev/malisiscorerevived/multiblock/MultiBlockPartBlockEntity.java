package nadiendev.malisiscorerevived.multiblock;

import org.jspecify.annotations.Nullable;

import nadiendev.malisiscorerevived.registry.MalisisCoreRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MultiBlockPartBlockEntity extends BlockEntity
{
	private @Nullable BlockPos offset;

	public MultiBlockPartBlockEntity(BlockPos pos, BlockState state)
	{
		super(MalisisCoreRegistries.MULTIBLOCK_PART_ENTITY.get(), pos, state);
	}

	public @Nullable BlockPos getOrigin()
	{
		return offset != null ? getBlockPos().offset(offset) : null;
	}

	public void setOrigin(BlockPos origin)
	{
		this.offset = origin.subtract(getBlockPos());
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);
		offset = input.read("origin", BlockPos.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);
		output.storeNullable("origin", BlockPos.CODEC, offset);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state)
	{
		super.preRemoveSideEffects(pos, state);
		if (!(level instanceof ServerLevel serverLevel))
			return;
		BlockPos origin = getOrigin();
		if (origin != null && serverLevel.getBlockState(origin).getBlock() instanceof IMultiBlockController)
			serverLevel.destroyBlock(origin, true);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
