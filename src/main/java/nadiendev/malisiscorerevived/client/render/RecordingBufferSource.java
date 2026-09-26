package nadiendev.malisiscorerevived.client.render;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public class RecordingBufferSource implements MultiBufferSource
{
	private static final PoseStack IDENTITY = new PoseStack();

	private final Map<RenderType, Recorder> buffers = new LinkedHashMap<>();

	@Override
	public VertexConsumer getBuffer(RenderType renderType)
	{
		return buffers.computeIfAbsent(renderType, type -> new Recorder());
	}

	public void submit(SubmitNodeCollector collector)
	{
		buffers.forEach((type, recorder) -> {
			if (!recorder.isEmpty())
				collector.submitCustomGeometry(IDENTITY, type, recorder::replay);
		});
		buffers.clear();
	}

	private static class Recorder implements VertexConsumer
	{
		private static final int FLOATS = 8;
		private static final int INTS = 3;

		private float[] floats = new float[FLOATS * 64];
		private int[] ints = new int[INTS * 64];
		private int count;

		boolean isEmpty()
		{
			return count == 0;
		}

		private int current()
		{
			return count - 1;
		}

		@Override
		public VertexConsumer addVertex(float x, float y, float z)
		{
			if ((count + 1) * FLOATS > floats.length)
			{
				floats = Arrays.copyOf(floats, floats.length * 2);
				ints = Arrays.copyOf(ints, ints.length * 2);
			}
			count++;
			int f = current() * FLOATS;
			floats[f] = x;
			floats[f + 1] = y;
			floats[f + 2] = z;
			floats[f + 5] = 0;
			floats[f + 6] = 1;
			floats[f + 7] = 0;
			int i = current() * INTS;
			ints[i] = 0xFFFFFFFF;
			ints[i + 1] = 0;
			ints[i + 2] = 0;
			return this;
		}

		@Override
		public VertexConsumer setColor(int r, int g, int b, int a)
		{
			ints[current() * INTS] = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
			return this;
		}

		@Override
		public VertexConsumer setColor(int color)
		{
			ints[current() * INTS] = color;
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v)
		{
			int f = current() * FLOATS;
			floats[f + 3] = u;
			floats[f + 4] = v;
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v)
		{
			ints[current() * INTS + 1] = u & 0xFFFF | v << 16;
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v)
		{
			ints[current() * INTS + 2] = u & 0xFFFF | v << 16;
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z)
		{
			int f = current() * FLOATS;
			floats[f + 5] = x;
			floats[f + 6] = y;
			floats[f + 7] = z;
			return this;
		}

		@Override
		public VertexConsumer setLineWidth(float width)
		{
			return this;
		}

		void replay(PoseStack.Pose pose, VertexConsumer consumer)
		{
			for (int v = 0; v < count; v++)
			{
				int f = v * FLOATS;
				int i = v * INTS;
				consumer.addVertex(floats[f], floats[f + 1], floats[f + 2])
						.setColor(ints[i])
						.setUv(floats[f + 3], floats[f + 4])
						.setOverlay(ints[i + 1])
						.setLight(ints[i + 2])
						.setNormal(floats[f + 5], floats[f + 6], floats[f + 7]);
			}
		}
	}
}
