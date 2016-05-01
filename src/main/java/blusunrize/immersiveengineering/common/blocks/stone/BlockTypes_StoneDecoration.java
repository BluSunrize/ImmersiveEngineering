package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_StoneDecoration implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COKEBRICK,
	BLASTBRICK,
	BLASTBRICK_REINFORCED,
	COKE,
	HEMPCRETE,
	CONCRETE,
	CONCRETE_TILE,
	CONCRETE_LEADED,
	INSULATING_GLASS;
	
	@Override
	public String getName()
	{
		return this.toString();
	}
	@Override
	public int getMeta()
	{
		return ordinal();
	}
	@Override
	public boolean listForCreative()
	{
		return true;
	}
}