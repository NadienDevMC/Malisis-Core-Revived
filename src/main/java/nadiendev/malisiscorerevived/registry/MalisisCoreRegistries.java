package nadiendev.malisiscorerevived.registry;

import java.util.function.Supplier;

import nadiendev.malisiscorerevived.MalisisCore;
import nadiendev.malisiscorerevived.multiblock.MultiBlockPartBlock;
import nadiendev.malisiscorerevived.multiblock.MultiBlockPartBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry objects owned by MalisisCore itself.
 */
public class MalisisCoreRegistries
{
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MalisisCore.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(	Registries.BLOCK_ENTITY_TYPE,
																										MalisisCore.MODID);

	/** Invisible block filling the space occupied by a multi-block. All its behavior is delegated to the multi-block origin. */
	public static final DeferredBlock<MultiBlockPartBlock> MULTIBLOCK_PART = BLOCKS.registerBlock("multiblock_part",
																									MultiBlockPartBlock::new,
																									BlockBehaviour.Properties	.of()
																																.strength(2.0F)
																																.sound(SoundType.STONE)
																																.noOcclusion()
																																.noLootTable()
																																.dynamicShape()
																																.pushReaction(PushReaction.BLOCK));

	@SuppressWarnings("DataFlowIssue")
	public static final Supplier<BlockEntityType<MultiBlockPartBlockEntity>> MULTIBLOCK_PART_ENTITY = BLOCK_ENTITIES.register(	"multiblock_part",
																																() -> BlockEntityType.Builder	.of(MultiBlockPartBlockEntity::new,
																																								MULTIBLOCK_PART.get())
																																								.build(null));

	public static void register(IEventBus modEventBus)
	{
		BLOCKS.register(modEventBus);
		BLOCK_ENTITIES.register(modEventBus);
	}
}
