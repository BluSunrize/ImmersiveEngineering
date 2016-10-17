package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

public class BlockIEStairs extends BlockStairs
{
	public boolean hasFlavour = false;
	public boolean isFlammable = false;
	public String name;
	float explosionResistance;
	
	public BlockIEStairs(String name, IBlockState state)
	{
		super(state);
		this.name = name;
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.useNeighborBrightness = true;
		this.explosionResistance = this.blockResistance/5f;
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

	@Override
	public float getExplosionResistance(Entity exploder)
	{
		return explosionResistance;
	}
	public BlockIEStairs setExplosionResistance(float explosionResistance)
	{
		this.explosionResistance = explosionResistance;
		return this;
	}

}