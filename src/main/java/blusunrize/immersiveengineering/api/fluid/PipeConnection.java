package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class PipeConnection
{
	public ChunkCoordinates from, to;
	public ForgeDirection direction;
	public Type type;

	public PipeConnection(ChunkCoordinates from, ChunkCoordinates to, ForgeDirection direction, Type type)
	{
		this.from = from;
		this.to = to;
		this.direction = direction;
		this.type = type;
	}

	public PipeConnection(ChunkCoordinates from, ChunkCoordinates to, Type type)
	{
		this(from, to, toDirection(from, to), type);
	}

	public static ForgeDirection toDirection(ChunkCoordinates from, ChunkCoordinates to)
	{
		if (to == null || from == null) return ForgeDirection.UNKNOWN;
		int dX = to.posX - from.posX;
		int dY = to.posY - from.posY;
		int dZ = to.posZ - from.posZ;
		if (Math.abs(dX) == 1 && dY == 0 && dZ == 0) {
			return dX == 1 ? ForgeDirection.EAST : ForgeDirection.WEST;
		} else if (dX == 0 && Math.abs(dY) == 1 && dZ == 0) {
			return dY == 1 ? ForgeDirection.UP : ForgeDirection.DOWN;
		} else if (dX == 0 && dY == 0 && Math.abs(dZ) == 1) {
			return dZ == 1 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
		}
		return ForgeDirection.UNKNOWN;
	}

	public static PipeConnection fromNBT(NBTTagCompound compound)
	{
		int[] toArr = compound.getIntArray("To");
		int[] fromArr = compound.getIntArray("From");
		int directionOrdinal = compound.getInteger("Direction");
		return new PipeConnection(
				new ChunkCoordinates(toArr[0], toArr[1], toArr[2]),
				new ChunkCoordinates(fromArr[0], fromArr[1], fromArr[2]),
				ForgeDirection.getOrientation(directionOrdinal),
				Type.fromOrdinal(compound.getInteger("Type"))
				);
	}

	public static boolean isTank(TileEntity tileEntity, ForgeDirection direction)
	{
		boolean isTank = tileEntity instanceof IFluidHandler;
		if(isTank)
		{
			IFluidHandler fluidHandler = (IFluidHandler) tileEntity;
			FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(direction);
			isTank = tankInfo!=null&&tankInfo.length > 0;
		}
		return isTank;
	}

	public NBTTagCompound toNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setIntArray("To", new int[] { to.posX, to.posY, to.posZ });
		compound.setIntArray("From", new int[] { from.posX, from.posY, from.posZ });
		compound.setInteger("Direction", direction.ordinal());
		compound.setInteger("Type", type.ordinal());
		return compound;
	}

	public TileEntity getEndTile(World world)
	{
		return world.getTileEntity(to.posX, to.posY, to.posZ);
	}

	@Override
	public String toString()
	{
		return from.toString() + " -> " + to.toString() + " in direction " + direction.toString();
	}

	public boolean equals(PipeConnection connection)
	{
		return connection.direction == direction &&
				connection.to.posX == to.posX &&
				connection.to.posY == to.posY &&
				connection.to.posZ == to.posZ &&
				connection.from.posX == from.posX &&
				connection.from.posY == from.posY &&
				connection.from.posZ == from.posZ;
	}

	public enum Type
	{
		PIPE, TANK;

		private static Type[] TYPES = new Type[] { PIPE, TANK };

		public static Type fromOrdinal(int type)
		{
			return TYPES[type];
		}
	}
}
