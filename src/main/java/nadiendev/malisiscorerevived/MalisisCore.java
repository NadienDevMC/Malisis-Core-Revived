package nadiendev.malisiscorerevived;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import nadiendev.malisiscorerevived.registry.MalisisCoreRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * MalisisCore is the library shared by the Malisis mods.<br>
 * It provides:
 * <ul>
 * <li>an animation / transformation framework ({@link nadiendev.malisiscorerevived.animation}) that drives block entity renderers,</li>
 * <li>a small OBJ model loader and rendering helpers ({@link nadiendev.malisiscorerevived.client}),</li>
 * <li>a generic multi-block system ({@link nadiendev.malisiscorerevived.multiblock}),</li>
 * <li>various utilities to deal with bounding boxes, directions and block entities.</li>
 * </ul>
 *
 * @author Ordinastie
 */
@Mod(MalisisCore.MODID)
public class MalisisCore
{
	public static final String MODID = "malisiscore";
	public static final Logger LOGGER = LogUtils.getLogger();

	public MalisisCore(IEventBus modEventBus, ModContainer modContainer)
	{
		MalisisCoreRegistries.register(modEventBus);
	}

	public static Identifier id(String path)
	{
		return Identifier.fromNamespaceAndPath(MODID, path);
	}
}
