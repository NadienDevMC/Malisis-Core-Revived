package nadiendev.malisiscorerevived.client.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;

/**
 * Simple Wavefront OBJ model.<br>
 * Supports vertices (v), texture coordinates (vt), normals (vn), faces (f) of any size and objects/groups (o/g). Materials are
 * ignored : the texture is provided when rendering.<br>
 * The coordinates of the model are expected to be in block units.
 *
 * @author Ordinastie
 */
public class ObjModel
{
	private final Map<String, Group> groups = new LinkedHashMap<>();

	/**
	 * A face of the model.
	 */
	public static class Face
	{
		final float[] positions;
		final float[] uvs;
		final float[] normals;

		Face(float[] positions, float[] uvs, float[] normals)
		{
			this.positions = positions;
			this.uvs = uvs;
			this.normals = normals;
		}

		public int size()
		{
			return positions.length / 3;
		}
	}

	/**
	 * A named group of faces of the model.
	 */
	public static class Group
	{
		private final String name;
		private final List<Face> faces = new ArrayList<>();

		Group(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public List<Face> getFaces()
		{
			return faces;
		}

		/**
		 * Renders the group.
		 *
		 * @param pose the pose
		 * @param consumer the vertex consumer (QUADS mode)
		 * @param sprite the sprite used to texture the group (OBJ texture coordinates are mapped on the whole sprite)
		 * @param color the ARGB color
		 * @param light the packed light
		 * @param overlay the packed overlay
		 */
		public void render(PoseStack.Pose pose, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay)
		{
			for (Face face : faces)
			{
				int n = face.size();
				if (n < 3)
					continue;
				//split the polygon into quads (triangles have their last vertex duplicated)
				for (int start = 1; start < n - 1; start += 2)
				{
					int i1 = start, i2 = start + 1, i3 = Math.min(start + 2, n - 1);
					vertex(pose, consumer, face, 0, sprite, color, light, overlay);
					vertex(pose, consumer, face, i1, sprite, color, light, overlay);
					vertex(pose, consumer, face, i2, sprite, color, light, overlay);
					vertex(pose, consumer, face, i3, sprite, color, light, overlay);
				}
			}
		}

		private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Face face, int i, TextureAtlasSprite sprite, int color, int light, int overlay)
		{
			float u = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * face.uvs[i * 2];
			float v = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * face.uvs[i * 2 + 1];
			consumer.addVertex(pose, face.positions[i * 3], face.positions[i * 3 + 1], face.positions[i * 3 + 2])
					.setColor(color)
					.setUv(u, v)
					.setOverlay(overlay)
					.setLight(light)
					.setNormal(pose, face.normals[i * 3], face.normals[i * 3 + 1], face.normals[i * 3 + 2]);
		}

		/**
		 * @return the bounding box of this group
		 */
		public AABB getBounds()
		{
			float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
			float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
			for (Face face : faces)
			{
				for (int i = 0; i < face.size(); i++)
				{
					minX = Math.min(minX, face.positions[i * 3]);
					minY = Math.min(minY, face.positions[i * 3 + 1]);
					minZ = Math.min(minZ, face.positions[i * 3 + 2]);
					maxX = Math.max(maxX, face.positions[i * 3]);
					maxY = Math.max(maxY, face.positions[i * 3 + 1]);
					maxZ = Math.max(maxZ, face.positions[i * 3 + 2]);
				}
			}
			return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		}
	}

	/**
	 * Gets a group by name. The name is case insensitive and a group named "door_Cube.001" (Blender export) can be retrieved with "door".
	 *
	 * @param name the name of the group
	 * @return the group, or null if not found
	 */
	@Nullable
	public Group getGroup(String name)
	{
		String key = name.toLowerCase(Locale.ROOT);
		Group group = groups.get(key);
		if (group != null)
			return group;
		for (Map.Entry<String, Group> entry : groups.entrySet())
		{
			if (entry.getKey().startsWith(key + "_") || entry.getKey().startsWith(key + "."))
				return entry.getValue();
		}
		return null;
	}

	public Collection<Group> getGroups()
	{
		return groups.values();
	}

	/**
	 * Renders the specified groups, or all the groups if none is specified.
	 */
	public void render(PoseStack.Pose pose, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, String... names)
	{
		if (names.length == 0)
		{
			for (Group group : groups.values())
				group.render(pose, consumer, sprite, color, light, overlay);
			return;
		}

		for (String name : names)
		{
			Group group = getGroup(name);
			if (group != null)
				group.render(pose, consumer, sprite, color, light, overlay);
		}
	}

	/**
	 * Parses an OBJ model.
	 *
	 * @param reader the reader
	 * @return the model
	 * @throws IOException if the model can't be read
	 */
	public static ObjModel parse(BufferedReader reader) throws IOException
	{
		ObjModel model = new ObjModel();
		List<float[]> positions = new ArrayList<>();
		List<float[]> uvs = new ArrayList<>();
		List<float[]> normals = new ArrayList<>();
		Group current = null;

		String line;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			String[] parts = line.split("\\s+");
			switch (parts[0])
			{
				case "v" -> positions.add(new float[] { f(parts, 1), f(parts, 2), f(parts, 3) });
				case "vt" -> uvs.add(new float[] { f(parts, 1), 1 - f(parts, 2) });
				case "vn" -> normals.add(new float[] { f(parts, 1), f(parts, 2), f(parts, 3) });
				case "o", "g" -> {
					String name = parts.length > 1 ? line.substring(parts[0].length()).trim() : "default";
					current = model.groups.computeIfAbsent(name.toLowerCase(Locale.ROOT), Group::new);
				}
				case "f" -> {
					if (current == null)
						current = model.groups.computeIfAbsent("default", Group::new);
					current.faces.add(parseFace(parts, positions, uvs, normals));
				}
				default -> {} //mtllib, usemtl, s... are ignored
			}
		}
		return model;
	}

	private static float f(String[] parts, int index)
	{
		return index < parts.length ? Float.parseFloat(parts[index]) : 0;
	}

	private static Face parseFace(String[] parts, List<float[]> positions, List<float[]> uvs, List<float[]> normals)
	{
		int n = parts.length - 1;
		float[] pos = new float[n * 3];
		float[] uv = new float[n * 2];
		float[] norm = new float[n * 3];
		boolean hasNormals = true;

		for (int i = 0; i < n; i++)
		{
			String[] idx = parts[i + 1].split("/");
			float[] p = positions.get(index(idx[0], positions.size()));
			System.arraycopy(p, 0, pos, i * 3, 3);

			if (idx.length > 1 && !idx[1].isEmpty())
			{
				float[] t = uvs.get(index(idx[1], uvs.size()));
				uv[i * 2] = t[0];
				uv[i * 2 + 1] = t[1];
			}

			if (idx.length > 2 && !idx[2].isEmpty())
			{
				float[] nm = normals.get(index(idx[2], normals.size()));
				System.arraycopy(nm, 0, norm, i * 3, 3);
			}
			else
				hasNormals = false;
		}

		if (!hasNormals)
		{
			//compute the face normal
			Vector3f a = new Vector3f(pos[3] - pos[0], pos[4] - pos[1], pos[5] - pos[2]);
			Vector3f b = new Vector3f(pos[6] - pos[0], pos[7] - pos[1], pos[8] - pos[2]);
			Vector3f normal = a.cross(b);
			if (normal.lengthSquared() > 0)
				normal.normalize();
			for (int i = 0; i < n; i++)
			{
				norm[i * 3] = normal.x;
				norm[i * 3 + 1] = normal.y;
				norm[i * 3 + 2] = normal.z;
			}
		}

		return new Face(pos, uv, norm);
	}

	private static int index(String s, int size)
	{
		int i = Integer.parseInt(s);
		return i < 0 ? size + i : i - 1;
	}
}
