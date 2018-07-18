/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.Loader;

public class BlockMetalDecoration0 extends BlockIEBase<BlockTypes_MetalDecoration0>
{
	public BlockMetalDecoration0()
	{
		super("metal_decoration0", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration0.class), ItemBlockIEBase.class);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		this.setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT);
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(Loader.isModLoaded("ctm")&&!itemBlock&&meta > 2)
			return "ctm";
		return null;
	}
}