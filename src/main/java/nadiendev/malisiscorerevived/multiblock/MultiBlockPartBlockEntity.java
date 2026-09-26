package nadiendev.malisiscorerevived.multiblock;

import javax.annotation.Nullable;

import nadiendev.malisiscorerevived.registry.MalisisCoreRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity of a {@link MultiBlockPartBlock} : stores the position of the multi-block origin (relative to the part).
 *
 * @author Ordinastie
 */
public class MultiBlockPartBlockEntity extends BlockEntity
{
	@Nullable
	private BlockPos offset;

	public MultiBlockPartBlockEntity(BlockPos pos, BlockState state)
	{
		super(MalisisCoreRegistries.MULTIBLOCK_PART_ENTITY.get(), pos, state);
	}

	@Nullable
	public BlockPos getOrigin()
	{
		return offset != null ? getBlockPos().offset(offset) : null;
	}

	public void setOrigin(BlockPos origin)
	{
		this.offset = origin.subtract(getBlockPos());
		setChanged();
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);
		if (tag.contains("originX"))
			offset = new BlockPos(tag.getInt("originX"), tag.getInt("originY"), tag.getInt("originZ"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.saveAdditional(tag, registries);
		if (offset != null)
		{
			tag.putInt("originX", offset.getX());
			tag.putInt("originY", offset.getY());
			tag.putInt("originZ", offset.getZ());
		}
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
