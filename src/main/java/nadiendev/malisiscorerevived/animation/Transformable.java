package nadiendev.malisiscorerevived.animation;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Target of {@link Transformation transformations}.<br>
 * Wraps the {@link PoseStack} used by a renderer as well as additional render parameters (alpha).<br>
 * <br>
 * All the rotation and scale offsets are relative to the center of the block (0.5, 0.5, 0.5) so a {@link PoseStack} passed here should
 * already have been translated to the block center, as done by {@link #centered(PoseStack)}.<br>
 * Transformations are applied in the same order they are called : the last one called is the first applied to the vertices.
 *
 * @author Ordinastie
 */
public class Transformable
{
	private final PoseStack poseStack;
	private int alpha = 255;

	public Transformable(PoseStack poseStack)
	{
		this.poseStack = poseStack;
	}

	/**
	 * Creates a {@link Transformable} for the pose stack after translating it to the center of the block.<br>
	 * The caller is responsible for pushing the pose before, and translating back by -0.5 before rendering.
	 *
	 * @param poseStack the pose stack
	 * @return the transformable
	 */
	public static Transformable centered(PoseStack poseStack)
	{
		poseStack.translate(0.5F, 0.5F, 0.5F);
		return new Transformable(poseStack);
	}

	public PoseStack poseStack()
	{
		return poseStack;
	}

	public void translate(float x, float y, float z)
	{
		if (x == 0 && y == 0 && z == 0)
			return;
		poseStack.translate(x, y, z);
	}

	/**
	 * Rotates the pose around the axis (x, y, z) going through the center of the block offset by (offsetX, offsetY, offsetZ).
	 *
	 * @param angle the angle in degrees (right-handed)
	 */
	public void rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		if (angle == 0)
			return;
		poseStack.translate(offsetX, offsetY, offsetZ);
		poseStack.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(angle), x, y, z));
		poseStack.translate(-offsetX, -offsetY, -offsetZ);
	}

	public void rotate(float angle, float x, float y, float z)
	{
		rotate(angle, x, y, z, 0, 0, 0);
	}

	/**
	 * Scales the pose around the center of the block offset by (offsetX, offsetY, offsetZ).
	 */
	public void scale(float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		if (x == 1 && y == 1 && z == 1)
			return;
		//prevent degenerated matrices (the normal matrix needs to be invertible)
		x = safe(x);
		y = safe(y);
		z = safe(z);
		poseStack.translate(offsetX, offsetY, offsetZ);
		poseStack.scale(x, y, z);
		poseStack.translate(-offsetX, -offsetY, -offsetZ);
	}

	private static float safe(float f)
	{
		return Math.abs(f) < 1.0E-4F ? (f < 0 ? -1.0E-4F : 1.0E-4F) : f;
	}

	/**
	 * @return the alpha (0-255)
	 */
	public int getAlpha()
	{
		return alpha;
	}

	public float getAlphaF()
	{
		return alpha / 255F;
	}

	public void setAlpha(int alpha)
	{
		this.alpha = Math.max(0, Math.min(255, alpha));
	}
}
