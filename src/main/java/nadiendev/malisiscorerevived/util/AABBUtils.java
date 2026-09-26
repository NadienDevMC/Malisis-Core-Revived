package nadiendev.malisiscorerevived.util;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Helpers to deal with {@link AABB} and {@link VoxelShape}.<br>
 * Rotations are right-handed and done around the center of the block (0.5, 0.5, 0.5).
 *
 * @author Ordinastie
 */
public class AABBUtils
{
	public static final AABB IDENTITY = new AABB(0, 0, 0, 1, 1, 1);

	public static AABB identity()
	{
		return IDENTITY;
	}

	public static AABB identity(BlockPos pos)
	{
		return new AABB(pos);
	}

	/**
	 * Rotates the {@link AABB} (defined facing {@link Direction#SOUTH}) to face the direction.
	 */
	@Nullable
	public static AABB rotate(@Nullable AABB aabb, Direction direction)
	{
		return rotate(aabb, DirectionUtils.getRotationCount(direction), Direction.Axis.Y);
	}

	/**
	 * Rotates the {@link AABB} count times by 90 degrees around the Y axis.
	 */
	@Nullable
	public static AABB rotate(@Nullable AABB aabb, int count)
	{
		return rotate(aabb, count, Direction.Axis.Y);
	}

	/**
	 * Rotates the {@link AABB} count times by 90 degrees (right-handed) around the axis going through the center of the block.
	 */
	@Nullable
	public static AABB rotate(@Nullable AABB aabb, int count, Direction.Axis axis)
	{
		if (aabb == null)
			return null;
		count = Math.floorMod(count, 4);
		if (count == 0)
			return aabb;

		double[] min = { aabb.minX - 0.5, aabb.minY - 0.5, aabb.minZ - 0.5 };
		double[] max = { aabb.maxX - 0.5, aabb.maxY - 0.5, aabb.maxZ - 0.5 };
		for (int i = 0; i < count; i++)
		{
			rotate90(min, axis);
			rotate90(max, axis);
		}

		return new AABB(min[0] + 0.5, min[1] + 0.5, min[2] + 0.5, max[0] + 0.5, max[1] + 0.5, max[2] + 0.5);
	}

	/**
	 * Rotates a point by 90 degrees (right-handed) around the axis.
	 */
	private static void rotate90(double[] p, Direction.Axis axis)
	{
		double x = p[0], y = p[1], z = p[2];
		switch (axis)
		{
			case X -> { //(y, z) -> (-z, y)
				p[1] = -z;
				p[2] = y;
			}
			case Y -> { //(x, z) -> (z, -x)
				p[0] = z;
				p[2] = -x;
			}
			case Z -> { //(x, y) -> (-y, x)
				p[0] = -y;
				p[1] = x;
			}
		}
	}

	public static AABB[] rotate(AABB[] aabbs, Direction direction)
	{
		AABB[] result = new AABB[aabbs.length];
		for (int i = 0; i < aabbs.length; i++)
			result[i] = rotate(aabbs[i], direction);
		return result;
	}

	/**
	 * Creates a {@link VoxelShape} from the {@link AABB AABBs}. Null or empty boxes are ignored.
	 */
	public static VoxelShape toShape(@Nullable AABB... aabbs)
	{
		if (aabbs == null)
			return Shapes.empty();
		VoxelShape shape = Shapes.empty();
		for (AABB aabb : aabbs)
		{
			if (aabb == null)
				continue;
			VoxelShape s = Shapes.box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
			shape = shape.isEmpty() ? s : Shapes.joinUnoptimized(shape, s, BooleanOp.OR);
		}
		return shape.optimize();
	}

	/**
	 * Clips the shape to the space of a single block.
	 */
	public static VoxelShape clip(VoxelShape shape)
	{
		if (shape.isEmpty())
			return shape;
		return Shapes.join(shape, Shapes.block(), BooleanOp.AND);
	}

	/**
	 * Gets the part of the {@link AABB} (relative to origin) that is inside the block at pos, relative to pos.
	 *
	 * @return the intersection, or null if the AABB doesn't intersect the block
	 */
	@Nullable
	public static AABB intersect(AABB aabb, BlockPos origin, BlockPos pos)
	{
		AABB block = new AABB(pos.subtract(origin));
		if (!block.intersects(aabb))
			return null;
		AABB inter = block.intersect(aabb);
		return inter.move(-block.minX, -block.minY, -block.minZ);
	}

	/**
	 * Gets the {@link AABB} encompassing all the blocks from pos1 to pos2 (both included).
	 */
	public static AABB encompassing(BlockPos pos1, BlockPos pos2)
	{
		return new AABB(Math.min(pos1.getX(), pos2.getX()),
						Math.min(pos1.getY(), pos2.getY()),
						Math.min(pos1.getZ(), pos2.getZ()),
						Math.max(pos1.getX(), pos2.getX()) + 1,
						Math.max(pos1.getY(), pos2.getY()) + 1,
						Math.max(pos1.getZ(), pos2.getZ()) + 1);
	}

	public static void write(CompoundTag tag, AABB aabb)
	{
		tag.putDouble("minX", aabb.minX);
		tag.putDouble("minY", aabb.minY);
		tag.putDouble("minZ", aabb.minZ);
		tag.putDouble("maxX", aabb.maxX);
		tag.putDouble("maxY", aabb.maxY);
		tag.putDouble("maxZ", aabb.maxZ);
	}

	@Nullable
	public static AABB read(CompoundTag tag)
	{
		if (!tag.contains("minX"))
			return null;
		return new AABB(tag.getDoubleOr("minX", 0), tag.getDoubleOr("minY", 0), tag.getDoubleOr("minZ", 0), tag.getDoubleOr("maxX", 0), tag.getDoubleOr("maxY", 0), tag.getDoubleOr("maxZ", 0));
	}

	public static void write(ValueOutput output, AABB aabb)
	{
		output.putDouble("minX", aabb.minX);
		output.putDouble("minY", aabb.minY);
		output.putDouble("minZ", aabb.minZ);
		output.putDouble("maxX", aabb.maxX);
		output.putDouble("maxY", aabb.maxY);
		output.putDouble("maxZ", aabb.maxZ);
	}

	public static @Nullable AABB read(ValueInput input)
	{
		if (input.read("minX", Codec.DOUBLE).isEmpty())
			return null;
		return new AABB(input.getDoubleOr("minX", 0), input.getDoubleOr("minY", 0), input.getDoubleOr("minZ", 0), input.getDoubleOr("maxX", 0), input.getDoubleOr("maxY", 0), input.getDoubleOr("maxZ", 0));
	}
}
