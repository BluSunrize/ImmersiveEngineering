package blusunrize.immersiveengineering.api;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author BluSunrize - 24.09.2015
 *
 * Just an AABB with additional info, for use with pipes
 */
public class AdvancedAABB extends AxisAlignedBB
{
	public ForgeDirection fd;
	public Vec3[][] drawOverride;
	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, ForgeDirection fd, Vec3[]... drawOverride)
	{
		super(xMin, yMin, zMin, xMax, yMax, zMax);
		this.fd = fd;
		this.drawOverride = drawOverride;
	}
	
	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, ForgeDirection fd)
	{
		this(xMin, yMin, zMin, xMax, yMax, zMax, fd, new Vec3[0][]);
	}
	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, Vec3[]... drawOverride)
	{
		this(xMin, yMin, zMin, xMax, yMax, zMax, null, drawOverride);
	}
	public AdvancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
	{
		this(xMin, yMin, zMin, xMax, yMax, zMax, null, new Vec3[0][]);
	}
}