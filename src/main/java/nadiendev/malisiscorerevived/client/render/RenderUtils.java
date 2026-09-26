package nadiendev.malisiscorerevived.client.render;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Rendering helpers used by the Malisis block entity renderers.
 *
 * @author Ordinastie
 */
public class RenderUtils
{
	private static final Direction[] DIRECTIONS_AND_NULL = { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST,
			Direction.EAST, null };

	/**
	 * Filter/transformer applied on each quad of a model when rendering it with
	 * {@link RenderUtils#renderModel(PoseStack, MultiBufferSource, BlockState, BakedModel, BlockAndTintGetter, BlockPos, int, int, float, QuadHandler)}.
	 */
	@FunctionalInterface
	public interface QuadHandler
	{
		/**
		 * @param quad the quad to render
		 * @return the pose to render the quad with, or null to skip the quad
		 */
		@Nullable
		PoseStack.Pose getPose(BakedQuad quad);
	}

	public static BakedModel getModel(BlockState state)
	{
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	/**
	 * Gets the packed light at the position.
	 */
	public static int getLight(@Nullable BlockAndTintGetter level, BlockPos pos)
	{
		return level != null ? LevelRenderer.getLightColor(level, pos) : net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
	}

	/**
	 * Renders the model of the {@link BlockState} with the current pose.
	 */
	public static void renderBlockState(PoseStack poseStack, MultiBufferSource buffer, BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int light, int overlay)
	{
		renderModel(poseStack, buffer, state, getModel(state), level, pos, light, overlay, 1, null);
	}

	/**
	 * Renders the model of the {@link BlockState} with the current pose and the specified alpha.
	 */
	public static void renderBlockState(PoseStack poseStack, MultiBufferSource buffer, BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha)
	{
		renderModel(poseStack, buffer, state, getModel(state), level, pos, light, overlay, alpha, null);
	}

	/**
	 * Renders a {@link BakedModel} for a {@link BlockState}.<br>
	 * Tint is applied when level and pos are provided.
	 *
	 * @param poseStack the pose stack
	 * @param buffer the buffer source
	 * @param state the state
	 * @param model the model
	 * @param level the level (for tint), can be null
	 * @param pos the position (for tint), can be null
	 * @param light the packed light
	 * @param overlay the packed overlay
	 * @param alpha the alpha (less than 1 forces translucent rendering)
	 * @param handler the quad handler, can be null to render all the quads with the current pose
	 */
	public static void renderModel(PoseStack poseStack, MultiBufferSource buffer, BlockState state, BakedModel model, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha, @Nullable QuadHandler handler)
	{
		RandomSource random = RandomSource.create();
		PoseStack.Pose defaultPose = poseStack.last();
		for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
		{
			RenderType entityType = alpha < 1 ? Sheets.translucentCullBlockSheet() : RenderTypeHelper.getEntityRenderType(renderType, false);
			VertexConsumer consumer = buffer.getBuffer(entityType);
			for (Direction direction : DIRECTIONS_AND_NULL)
			{
				random.setSeed(42L);
				List<BakedQuad> quads = model.getQuads(state, direction, random, ModelData.EMPTY, renderType);
				for (BakedQuad quad : quads)
				{
					PoseStack.Pose pose = handler != null ? handler.getPose(quad) : defaultPose;
					if (pose == null)
						continue;
					putQuad(pose, consumer, quad, state, level, pos, light, overlay, alpha);
				}
			}
		}
	}

	/**
	 * Puts a {@link BakedQuad} into the consumer, applying the block tint if possible.
	 */
	public static void putQuad(PoseStack.Pose pose, VertexConsumer consumer, BakedQuad quad, @Nullable BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha)
	{
		float r = 1, g = 1, b = 1;
		if (quad.isTinted() && state != null)
		{
			int color = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, quad.getTintIndex());
			if (color != -1)
			{
				r = (color >> 16 & 255) / 255F;
				g = (color >> 8 & 255) / 255F;
				b = (color & 255) / 255F;
			}
		}
		consumer.putBulkData(pose, quad, r, g, b, alpha, light, overlay, false);
	}

	/**
	 * Gets the tint color of the {@link BlockState} (for the tint index 0), or white if the block is not tinted.
	 */
	public static int getTint(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos)
	{
		int color = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
		return color == -1 ? 0xFFFFFFFF : 0xFF000000 | color;
	}

	/**
	 * Gets the sprite used by the model of the {@link BlockState} for the side.<br>
	 * Falls back to the particle sprite if the model has no quad for that side.
	 */
	public static TextureAtlasSprite getSprite(BlockState state, @Nullable Direction side)
	{
		BakedModel model = getModel(state);
		if (side != null)
		{
			RandomSource random = RandomSource.create(42L);
			List<BakedQuad> quads = model.getQuads(state, side, random, ModelData.EMPTY, null);
			if (quads.isEmpty())
			{
				random.setSeed(42L);
				for (BakedQuad quad : model.getQuads(state, null, random, ModelData.EMPTY, null))
				{
					if (quad.getDirection() == side)
						return quad.getSprite();
				}
			}
			else
				return quads.get(0).getSprite();
		}
		return model.getParticleIcon(ModelData.EMPTY);
	}

	/**
	 * Gets a sprite from the block atlas.
	 *
	 * @param location the location of the texture (for example "malisisdoors:block/garage_door")
	 */
	public static TextureAtlasSprite getBlockSprite(net.minecraft.resources.ResourceLocation location)
	{
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(location);
	}

	/**
	 * Creates a copy of the {@link BakedQuad} using a different sprite.
	 */
	public static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite sprite)
	{
		TextureAtlasSprite old = quad.getSprite();
		if (old == sprite)
			return quad;
		int[] vertices = quad.getVertices().clone();
		for (int i = 0; i < 4; i++)
		{
			int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
			float u = Float.intBitsToFloat(vertices[offset]);
			float v = Float.intBitsToFloat(vertices[offset + 1]);
			float relU = (u - old.getU0()) / (old.getU1() - old.getU0());
			float relV = (v - old.getV0()) / (old.getV1() - old.getV0());
			vertices[offset] = Float.floatToRawIntBits(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * relU);
			vertices[offset + 1] = Float.floatToRawIntBits(sprite.getV0() + (sprite.getV1() - sprite.getV0()) * relV);
		}
		return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite, quad.isShade(), quad.hasAmbientOcclusion());
	}

	/**
	 * Gets the center of the quad (average of its vertices).
	 */
	public static float[] getCenter(BakedQuad quad)
	{
		int[] vertices = quad.getVertices();
		float x = 0, y = 0, z = 0;
		for (int i = 0; i < 4; i++)
		{
			int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
			x += Float.intBitsToFloat(vertices[offset]);
			y += Float.intBitsToFloat(vertices[offset + 1]);
			z += Float.intBitsToFloat(vertices[offset + 2]);
		}
		return new float[] { x / 4, y / 4, z / 4 };
	}

	/**
	 * Gets the minimum and maximum of the quad positions along the axis.
	 */
	public static float[] getRange(BakedQuad quad, Direction.Axis axis)
	{
		int[] vertices = quad.getVertices();
		float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
		int index = axis.ordinal();
		for (int i = 0; i < 4; i++)
		{
			float f = Float.intBitsToFloat(vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + index]);
			min = Math.min(min, f);
			max = Math.max(max, f);
		}
		return new float[] { min, max };
	}

	/**
	 * Gets a render type to render OBJ models or boxes textured with the block atlas.
	 *
	 * @param translucent whether the render type should support translucency
	 */
	public static RenderType blockAtlasType(boolean translucent)
	{
		return translucent	? RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)
							: RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);
	}

	//#region Boxes
	/**
	 * Renders a box.
	 *
	 * @param pose the pose
	 * @param consumer the consumer (QUADS mode)
	 * @param box the box to render
	 * @param texture provides the sprite and UVs for each face
	 * @param color the ARGB color
	 * @param light the packed light
	 * @param overlay the packed overlay
	 */
	public static void renderBox(PoseStack.Pose pose, VertexConsumer consumer, AABB box, BoxTexture texture, int color, int light, int overlay)
	{
		for (Direction side : Direction.values())
		{
			if (!texture.shouldRender(side))
				continue;
			renderFace(pose, consumer, box, side, texture.getSprite(side), texture.getUVs(side, box), color, light, overlay);
		}
	}

	/**
	 * Renders a single face of a box.
	 *
	 * @param uvs the UVs (u0, v0, u1, v1) relative to the sprite (0 to 1)
	 */
	public static void renderFace(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Direction side, TextureAtlasSprite sprite, float[] uvs, int color, int light, int overlay)
	{
		float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
		float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
		float[][] v = switch (side)
		{
			case DOWN -> new float[][] { { x0, y0, z1 }, { x0, y0, z0 }, { x1, y0, z0 }, { x1, y0, z1 } };
			case UP -> new float[][] { { x0, y1, z0 }, { x0, y1, z1 }, { x1, y1, z1 }, { x1, y1, z0 } };
			case NORTH -> new float[][] { { x1, y1, z0 }, { x1, y0, z0 }, { x0, y0, z0 }, { x0, y1, z0 } };
			case SOUTH -> new float[][] { { x0, y1, z1 }, { x0, y0, z1 }, { x1, y0, z1 }, { x1, y1, z1 } };
			case WEST -> new float[][] { { x0, y1, z0 }, { x0, y0, z0 }, { x0, y0, z1 }, { x0, y1, z1 } };
			case EAST -> new float[][] { { x1, y1, z1 }, { x1, y0, z1 }, { x1, y0, z0 }, { x1, y1, z0 } };
		};
		// vertex order matches the vanilla FaceBakery : (u0, v0), (u0, v1), (u1, v1), (u1, v0)
		float[][] uv = { { uvs[0], uvs[1] }, { uvs[0], uvs[3] }, { uvs[2], uvs[3] }, { uvs[2], uvs[1] } };
		float nx = side.getStepX(), ny = side.getStepY(), nz = side.getStepZ();
		for (int i = 0; i < 4; i++)
		{
			consumer.addVertex(pose, v[i][0], v[i][1], v[i][2])
					.setColor(color)
					.setUv(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * uv[i][0],
							sprite.getV0() + (sprite.getV1() - sprite.getV0()) * uv[i][1])
					.setOverlay(overlay)
					.setLight(light)
					.setNormal(pose, nx, ny, nz);
		}
	}

	/**
	 * Provides the sprites and UVs used to render a box.
	 */
	public interface BoxTexture
	{
		TextureAtlasSprite getSprite(Direction side);

		/**
		 * @return the UVs (u0, v0, u1, v1) relative to the sprite (0 to 1)
		 */
		float[] getUVs(Direction side, AABB box);

		default boolean shouldRender(Direction side)
		{
			return true;
		}

		/**
		 * Box texture using the same UVs as a vanilla model element (UVs derived from the position of the box inside its block).
		 */
		static BoxTexture of(Function<Direction, TextureAtlasSprite> sprites)
		{
			return new BoxTexture()
			{
				@Override
				public TextureAtlasSprite getSprite(Direction side)
				{
					return sprites.apply(side);
				}

				@Override
				public float[] getUVs(Direction side, AABB box)
				{
					return blockUVs(side, box);
				}
			};
		}

		static BoxTexture of(TextureAtlasSprite sprite)
		{
			return of(side -> sprite);
		}

		/**
		 * Box texture stretching the whole sprite over the area (UVs interpolated from the position of the box inside the area).
		 */
		static BoxTexture stretched(TextureAtlasSprite sprite, AABB area)
		{
			return new BoxTexture()
			{
				@Override
				public TextureAtlasSprite getSprite(Direction side)
				{
					return sprite;
				}

				@Override
				public float[] getUVs(Direction side, AABB box)
				{
					return areaUVs(side, box, area);
				}
			};
		}
	}

	/**
	 * Calculates the UVs of a face the same way vanilla does for model elements without explicit UVs.<br>
	 * The box is considered relative to the block containing its minimum corner.
	 */
	public static float[] blockUVs(Direction side, AABB box)
	{
		double ox = Math.floor(box.minX + 1.0E-5), oy = Math.floor(box.minY + 1.0E-5), oz = Math.floor(box.minZ + 1.0E-5);
		float minX = (float) (box.minX - ox), minY = (float) (box.minY - oy), minZ = (float) (box.minZ - oz);
		float maxX = (float) Math.min(1, box.maxX - ox), maxY = (float) Math.min(1, box.maxY - oy), maxZ = (float) Math.min(1, box.maxZ - oz);
		return switch (side)
		{
			case DOWN -> new float[] { minX, 1 - maxZ, maxX, 1 - minZ };
			case UP -> new float[] { minX, minZ, maxX, maxZ };
			case NORTH -> new float[] { 1 - maxX, 1 - maxY, 1 - minX, 1 - minY };
			case SOUTH -> new float[] { minX, 1 - maxY, maxX, 1 - minY };
			case WEST -> new float[] { minZ, 1 - maxY, maxZ, 1 - minY };
			case EAST -> new float[] { 1 - maxZ, 1 - maxY, 1 - minZ, 1 - minY };
		};
	}

	/**
	 * Calculates the UVs of a face so that the texture is stretched over the whole area.
	 */
	public static float[] areaUVs(Direction side, AABB box, AABB area)
	{
		float minX = rel(box.minX, area.minX, area.maxX), maxX = rel(box.maxX, area.minX, area.maxX);
		float minY = rel(box.minY, area.minY, area.maxY), maxY = rel(box.maxY, area.minY, area.maxY);
		float minZ = rel(box.minZ, area.minZ, area.maxZ), maxZ = rel(box.maxZ, area.minZ, area.maxZ);
		return switch (side)
		{
			case DOWN -> new float[] { minX, 1 - maxZ, maxX, 1 - minZ };
			case UP -> new float[] { minX, minZ, maxX, maxZ };
			case NORTH -> new float[] { 1 - maxX, 1 - maxY, 1 - minX, 1 - minY };
			case SOUTH -> new float[] { minX, 1 - maxY, maxX, 1 - minY };
			case WEST -> new float[] { minZ, 1 - maxY, maxZ, 1 - minY };
			case EAST -> new float[] { 1 - maxZ, 1 - maxY, 1 - minZ, 1 - minY };
		};
	}

	private static float rel(double v, double min, double max)
	{
		if (max - min <= 0)
			return 0;
		return (float) Math.max(0, Math.min(1, (v - min) / (max - min)));
	}
	//#end Boxes
}
