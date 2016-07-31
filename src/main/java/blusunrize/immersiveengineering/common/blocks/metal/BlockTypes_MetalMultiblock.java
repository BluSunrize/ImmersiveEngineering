package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.Locale;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_MetalMultiblock implements IStringSerializable, BlockIEBase.IBlockEnum
{
	METAL_PRESS(true),
	CRUSHER(true),
	TANK(false),
	SILO(false),
	ASSEMBLER(false),
	AUTO_WORKBENCH(true),
	BOTTLING_MACHINE(true),
	SQUEEZER(true),
	FERMENTER(true),
	REFINERY(true),
	DIESEL_GENERATOR(true),
	EXCAVATOR(true),
	BUCKET_WHEEL(true),
	ARC_FURNACE(true);

	private boolean needsCustomState;
	BlockTypes_MetalMultiblock(boolean needsCustomState)
	{
		this.needsCustomState = needsCustomState;
	}
	
	@Override
	public String getName()
	{
		return this.toString().toLowerCase();
	}
	@Override
	public int getMeta()
	{
		return ordinal();
	}
	@Override
	public boolean listForCreative()
	{
		return false;
	}
	
	public boolean needsCustomState()
	{
		return this.needsCustomState;
	}
	public String getCustomState()
	{
		String[] split = getName().split("_");
		String s = split[0].toLowerCase(Locale.ENGLISH);
		for(int i=1; i<split.length; i++)
			s+=split[i].substring(0,1).toUpperCase(Locale.ENGLISH)+split[i].substring(1).toLowerCase(Locale.ENGLISH);
		return s;
	}
}