package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class BlockIEStairs extends BlockStairs
{
	public boolean hasFlavour = false;
	public boolean isFlammable = false;
	public String name;
	
	public BlockIEStairs(String name, IBlockState state)
	{
		super(state);
		this.name = name;
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.useNeighborBrightness = true;
		ImmersiveEngineering.registerBlock(this, ItemBlockIEStairs.class, name);
		IEContent.registeredIEBlocks.add(this);
	}
	
	public BlockIEStairs setFlammable(boolean b)
	{
		this.isFlammable = b;
		return this;
	}
	
	public BlockIEStairs setHasFlavour(boolean hasFlavour)
	{
		this.hasFlavour = hasFlavour;
		return this;
	}
}