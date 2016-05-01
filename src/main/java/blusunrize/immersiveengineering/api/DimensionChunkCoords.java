package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;

public class DimensionChunkCoords extends ChunkCoordIntPair
{
	public int dimension;
	public DimensionChunkCoords(int dimension, int x, int z)
	{
		super(x, z);
		this.dimension=dimension;
	}

	@Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof DimensionChunkCoords))
            return false;
        else
        {
        	DimensionChunkCoords coordPair = (DimensionChunkCoords)o;
            return this.dimension==coordPair.dimension && this.chunkXPos==coordPair.chunkXPos && this.chunkZPos==coordPair.chunkZPos;
        }
    }
	@Override
    public String toString()
    {
        return "[dim:"+ this.dimension+ "; " +this.chunkXPos+ ", " +this.chunkZPos + "]";
    }

	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("dim", dimension);
		tag.setInteger("x", this.chunkXPos);
		tag.setInteger("z", this.chunkZPos);
		return tag;
	}
	public static DimensionChunkCoords readFromNBT(NBTTagCompound tag)
	{
		if(tag.hasKey("dim",3)&&tag.hasKey("x",3)&&tag.hasKey("z",3))
			return new DimensionChunkCoords(tag.getInteger("dim"),tag.getInteger("x"),tag.getInteger("z"));
		return null;
	}
}