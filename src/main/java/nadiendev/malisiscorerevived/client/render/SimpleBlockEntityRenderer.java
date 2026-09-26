package nadiendev.malisiscorerevived.client.render;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class SimpleBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, SimpleBlockEntityRenderer.State<T>>
{
	public static class State<T extends BlockEntity> extends BlockEntityRenderState
	{
		public @Nullable T blockEntity;
		public float partialTick;
	}

	@Override
	public State<T> createRenderState()
	{
		return new State<>();
	}

	@Override
	public void extractRenderState(T blockEntity, State<T> state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
	{
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
		state.blockEntity = blockEntity;
		state.partialTick = partialTick;
	}

	@Override
	public void submit(State<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
	{
		if (state.blockEntity == null)
			return;
		RecordingBufferSource buffer = new RecordingBufferSource();
		render(state.blockEntity, state.partialTick, poseStack, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY);
		buffer.submit(collector);
	}

	protected abstract void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);
}
