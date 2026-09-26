package nadiendev.malisiscorerevived.client.model;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import nadiendev.malisiscorerevived.MalisisCore;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Loads and caches the {@link ObjModel OBJ models}. The cache is cleared when the resources are reloaded.
 *
 * @author Ordinastie
 */
public class ObjModelCache implements ResourceManagerReloadListener
{
	public static final ObjModelCache INSTANCE = new ObjModelCache();

	private static final ObjModel EMPTY = new ObjModel();

	private final Map<ResourceLocation, ObjModel> models = new ConcurrentHashMap<>();

	/**
	 * Gets the model at the location (for example "malisisdoors:models/saloon_door.obj").
	 *
	 * @param location the location of the model
	 * @return the model (empty if it couldn't be loaded)
	 */
	public static ObjModel get(ResourceLocation location)
	{
		return INSTANCE.models.computeIfAbsent(location, ObjModelCache::load);
	}

	@Nullable
	private static ObjModel load(ResourceLocation location)
	{
		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		Optional<Resource> resource = manager.getResource(location);
		if (resource.isEmpty())
		{
			MalisisCore.LOGGER.error("OBJ model {} not found", location);
			return EMPTY;
		}

		try (BufferedReader reader = resource.get().openAsReader())
		{
			return ObjModel.parse(reader);
		}
		catch (Exception e)
		{
			MalisisCore.LOGGER.error("Failed to load OBJ model {}", location, e);
			return EMPTY;
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager)
	{
		models.clear();
	}
}
