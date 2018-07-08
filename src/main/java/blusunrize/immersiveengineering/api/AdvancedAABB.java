/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

/**
 * @author BluSunrize - 24.09.2015
 * <p>
 * Just an AABB with additional info, for use with pipes
 */
public class AdvancedAABB extends AxisAlignedBB
{
	public EnumFacing fd;
	public Vec3d[][] drawOverride;

	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, EnumFacing fd, Vec3d[]... drawOverride)
	{
		super(xMin, yMin, zMin, xMax, yMax, zMax);
		this.fd = fd;
		this.drawOverride = drawOverride;
	}

	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, EnumFacing fd)
	{
		this(xMin, yMin, zMin, xMax, yMax, zMax, fd, new Vec3d[0][]);
	}

	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, Vec3d[]... drawOverride)
	{
		this(xMin, yMin, zMin, xMax, yMax, zMax, null, drawOverride);
	}

	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
	{
		this(xMin, yMin, zMin, xMax, yMax, zMax, null, new Vec3d[0][]);
	}

	public AdvancedAABB(AxisAlignedBB aabb, EnumFacing fd)
	{
		this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, fd, new Vec3d[0][]);
	}
}