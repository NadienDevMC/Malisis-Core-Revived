package nadiendev.malisiscorerevived.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RenderUtils
{
	private static final Direction[] DIRECTIONS_AND_NULL = { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST,
			Direction.EAST, null };

	@FunctionalInterface
	public interface QuadHandler
	{
		PoseStack.@Nullable Pose getPose(BakedQuad quad);
	}

	public static BlockStateModel getModel(BlockState state)
	{
		return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
	}

	@SuppressWarnings("deprecation")
	public static List<BakedQuad> getQuads(BlockState state)
	{
		List<BlockStateModelPart> parts = new ArrayList<>();
		getModel(state).collectParts(RandomSource.create(42L), parts);
		List<BakedQuad> quads = new ArrayList<>();
		for (BlockStateModelPart part : parts)
		{
			for (Direction direction : DIRECTIONS_AND_NULL)
				quads.addAll(part.getQuads(direction));
		}
		return quads;
	}

	public static int getLight(@Nullable BlockAndLightGetter level, BlockPos pos)
	{
		return level != null ? LevelRenderer.getLightCoords(level, pos) : LightCoordsUtil.FULL_BRIGHT;
	}

	public static RenderType blockAtlasType(boolean translucent)
	{
		return translucent ? RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS) : RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
	}

	public static void renderBlockState(PoseStack poseStack, MultiBufferSource buffer, BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay)
	{
		renderBlockState(poseStack, buffer, state, level, pos, light, overlay, 1);
	}

	public static void renderBlockState(PoseStack poseStack, MultiBufferSource buffer, BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha)
	{
		renderModel(poseStack, buffer, state, getQuads(state), level, pos, light, overlay, alpha, null);
	}

	public static void renderModel(PoseStack poseStack, MultiBufferSource buffer, @Nullable BlockState state, List<BakedQuad> quads, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha, @Nullable QuadHandler handler)
	{
		if (quads.isEmpty() || alpha <= 0)
			return;
		renderQuads(poseStack.last(), buffer.getBuffer(blockAtlasType(alpha < 1)), quads, state, level, pos, light, overlay, alpha, handler);
	}

	public static void submitBlockState(PoseStack poseStack, SubmitNodeCollector collector, BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay)
	{
		submitBlockState(poseStack, collector, state, level, pos, light, overlay, 1);
	}

	public static void submitBlockState(PoseStack poseStack, SubmitNodeCollector collector, BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha)
	{
		submitQuads(poseStack, collector, getQuads(state), state, level, pos, light, overlay, alpha, null);
	}

	public static void submitQuads(PoseStack poseStack, SubmitNodeCollector collector, List<BakedQuad> quads, @Nullable BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha, @Nullable QuadHandler handler)
	{
		if (quads.isEmpty() || alpha <= 0)
			return;
		collector.submitCustomGeometry(	poseStack,
										blockAtlasType(alpha < 1),
										(pose, consumer) -> renderQuads(pose, consumer, quads, state, level, pos, light, overlay, alpha, handler));
	}

	public static void renderQuads(PoseStack.Pose defaultPose, VertexConsumer consumer, List<BakedQuad> quads, @Nullable BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha, @Nullable QuadHandler handler)
	{
		QuadInstance instance = new QuadInstance();
		instance.setLightCoords(light);
		instance.setOverlayCoords(overlay);
		for (BakedQuad quad : quads)
		{
			PoseStack.Pose pose = handler != null ? handler.getPose(quad) : defaultPose;
			if (pose == null)
				continue;
			int color = quad.materialInfo().isTinted() && state != null ? getTint(state, level, pos, quad.materialInfo().tintIndex()) : 0xFFFFFFFF;
			instance.setColor(ARGB.color(Math.round(alpha * 255), color));
			consumer.putBakedQuad(pose, quad, instance);
		}
	}

	public static void putQuad(PoseStack.Pose pose, VertexConsumer consumer, BakedQuad quad, @Nullable BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int light, int overlay, float alpha)
	{
		int color = quad.materialInfo().isTinted() && state != null ? getTint(state, level, pos, quad.materialInfo().tintIndex()) : 0xFFFFFFFF;
		putQuad(pose, consumer, quad, ARGB.color(Math.round(alpha * 255), color), light, overlay);
	}

	public static void putQuad(PoseStack.Pose pose, VertexConsumer consumer, BakedQuad quad, int color, int light, int overlay)
	{
		QuadInstance instance = new QuadInstance();
		instance.setColor(color);
		instance.setLightCoords(light);
		instance.setOverlayCoords(overlay);
		consumer.putBakedQuad(pose, quad, instance);
	}

	public static int getTint(BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos)
	{
		return getTint(state, level, pos, 0);
	}

	public static int getTint(BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int tintIndex)
	{
		BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(state, tintIndex);
		if (source == null)
			return 0xFFFFFFFF;
		int color = level instanceof BlockAndTintGetter tintLevel && pos != null ? source.colorInWorld(state, tintLevel, pos) : source.color(state);
		return 0xFF000000 | color;
	}

	public static TextureAtlasSprite getSprite(BlockState state, @Nullable Direction side)
	{
		if (side != null)
		{
			for (BakedQuad quad : getQuads(state))
			{
				if (quad.direction() == side)
					return quad.materialInfo().sprite();
			}
		}
		return getParticleSprite(state);
	}

	@SuppressWarnings("deprecation")
	public static TextureAtlasSprite getParticleSprite(BlockState state)
	{
		return getModel(state).particleMaterial().sprite();
	}

	public static TextureAtlasSprite getBlockSprite(Identifier location)
	{
		return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, location));
	}

	public static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite sprite)
	{
		TextureAtlasSprite old = quad.materialInfo().sprite();
		if (old == sprite)
			return quad;
		long[] uvs = new long[4];
		for (int i = 0; i < 4; i++)
		{
			long packed = quad.packedUV(i);
			float relU = (UVPair.unpackU(packed) - old.getU0()) / (old.getU1() - old.getU0());
			float relV = (UVPair.unpackV(packed) - old.getV0()) / (old.getV1() - old.getV0());
			uvs[i] = UVPair.pack(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * relU, sprite.getV0() + (sprite.getV1() - sprite.getV0()) * relV);
		}
		BakedQuad.MaterialInfo info = quad.materialInfo();
		BakedQuad.MaterialInfo material = new BakedQuad.MaterialInfo(	sprite,
																		info.layer(),
																		info.itemRenderType(),
																		info.tintIndex(),
																		info.shade(),
																		info.lightEmission(),
																		info.ambientOcclusion());
		return new BakedQuad(	quad.position(0),
								quad.position(1),
								quad.position(2),
								quad.position(3),
								uvs[0],
								uvs[1],
								uvs[2],
								uvs[3],
								quad.direction(),
								material);
	}

	public static float[] getCenter(BakedQuad quad)
	{
		float x = 0, y = 0, z = 0;
		for (int i = 0; i < 4; i++)
		{
			Vector3fc position = quad.position(i);
			x += position.x();
			y += position.y();
			z += position.z();
		}
		return new float[] { x / 4, y / 4, z / 4 };
	}

	public static float[] getRange(BakedQuad quad, Direction.Axis axis)
	{
		float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
		for (int i = 0; i < 4; i++)
		{
			float f = quad.position(i).get(axis.ordinal());
			min = Math.min(min, f);
			max = Math.max(max, f);
		}
		return new float[] { min, max };
	}

	public static void submitBox(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, AABB box, BoxTexture texture, int color, int light, int overlay)
	{
		collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> renderBox(pose, consumer, box, texture, color, light, overlay));
	}

	public static void renderBox(PoseStack.Pose pose, VertexConsumer consumer, AABB box, BoxTexture texture, int color, int light, int overlay)
	{
		for (Direction side : Direction.values())
		{
			if (!texture.shouldRender(side))
				continue;
			renderFace(pose, consumer, box, side, texture.getSprite(side), texture.getUVs(side, box), color, light, overlay);
		}
	}

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
		float[][] uv = { { uvs[0], uvs[1] }, { uvs[0], uvs[3] }, { uvs[2], uvs[3] }, { uvs[2], uvs[1] } };
		float nx = side.getStepX(), ny = side.getStepY(), nz = side.getStepZ();
		for (int i = 0; i < 4; i++)
		{
			consumer.addVertex(pose, v[i][0], v[i][1], v[i][2])
					.setColor(color)
					.setUv(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * uv[i][0], sprite.getV0() + (sprite.getV1() - sprite.getV0()) * uv[i][1])
					.setOverlay(overlay)
					.setLight(light)
					.setNormal(pose, nx, ny, nz);
		}
	}

	public interface BoxTexture
	{
		TextureAtlasSprite getSprite(Direction side);

		float[] getUVs(Direction side, AABB box);

		default boolean shouldRender(Direction side)
		{
			return true;
		}

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
}
