package blusunrize.immersiveengineering.common.util.shapes;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class MultiblockCacheKey
{
	public final Direction facing;
	public final boolean mirrored;
	public final BlockPos posInMultiblock;

	public MultiblockCacheKey(Direction facing, boolean mirrored, BlockPos posInMultiblock)
	{
		this.facing = facing;
		this.mirrored = mirrored;
		this.posInMultiblock = posInMultiblock;
	}

	public <T extends MultiblockPartTileEntity<T> & IDirectionalTile & IMirrorAble> MultiblockCacheKey(T te)
	{
		this(te.getFacing(), te.getIsMirrored(), te.posInMultiblock);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		MultiblockCacheKey that = (MultiblockCacheKey)o;
		return mirrored==that.mirrored&&
				facing==that.facing&&
				posInMultiblock.equals(that.posInMultiblock);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(facing, mirrored, posInMultiblock);
	}
}
