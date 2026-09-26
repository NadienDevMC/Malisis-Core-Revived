package nadiendev.malisiscorerevived.client;

import nadiendev.malisiscorerevived.MalisisCore;
import nadiendev.malisiscorerevived.client.model.ObjModelCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * Client entry point of MalisisCore.
 */
@Mod(value = MalisisCore.MODID, dist = Dist.CLIENT)
public class MalisisCoreClient
{
	public MalisisCoreClient(IEventBus modEventBus, ModContainer container)
	{
		modEventBus.addListener(MalisisCoreClient::onRegisterReloadListeners);
	}

	private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener(ObjModelCache.INSTANCE);
	}
}
