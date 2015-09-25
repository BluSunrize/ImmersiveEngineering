package blusunrize.immersiveengineering.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class DirectionalChunkCoords extends ChunkCoordinates
{
	public ForgeDirection direction;

	public DirectionalChunkCoords(ChunkCoordinates chunkCoordinates)
	{
		this(chunkCoordinates, ForgeDirection.UNKNOWN);
	}

	public DirectionalChunkCoords(ChunkCoordinates chunkCoordinates, ForgeDirection direction)
	{
		this(chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ, ForgeDirection.UNKNOWN);
	}

	public DirectionalChunkCoords(int x, int y, int z, ForgeDirection direction)
	{
		super(x, y, z);
		this.direction = direction;
	}

	public String toString()
	{
		return "DirectionalChunkCoords{x=" + this.posX + ", y=" + this.posY + ", z=" + this.posZ + ", direction=" + this.direction.toString() + "}";
	}

	public TileEntity getTile(World world)
	{
		return world.getTileEntity(posX, posY, posZ);
	}
}
