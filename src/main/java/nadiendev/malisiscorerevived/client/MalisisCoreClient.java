package nadiendev.malisiscorerevived.client;

import nadiendev.malisiscorerevived.MalisisCore;
import nadiendev.malisiscorerevived.client.model.ObjModelCache;
import nadiendev.malisiscorerevived.client.multiblock.MultiBlockPartClientExtensions;
import nadiendev.malisiscorerevived.registry.MalisisCoreRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Client entry point of MalisisCore.
 */
@Mod(value = MalisisCore.MODID, dist = Dist.CLIENT)
public class MalisisCoreClient
{
	public MalisisCoreClient(IEventBus modEventBus, ModContainer container)
	{
		modEventBus.addListener(MalisisCoreClient::onRegisterReloadListeners);
		modEventBus.addListener(MalisisCoreClient::onRegisterClientExtensions);
	}

	private static void onRegisterReloadListeners(AddClientReloadListenersEvent event)
	{
		event.addListener(MalisisCore.id("obj_models"), ObjModelCache.INSTANCE);
	}

	private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event)
	{
		event.registerBlock(new MultiBlockPartClientExtensions(), MalisisCoreRegistries.MULTIBLOCK_PART.get());
	}
}
