/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;

public class IEStairsBlock extends StairsBlock implements IIEBlock
{
	private BlockRenderLayer layer = BlockRenderLayer.SOLID;
	private final IIEBlock base;

	public <T extends Block & IIEBlock> IEStairsBlock(String name, Properties properties, T base)
	{
		super(base.getDefaultState(), properties);
		this.base = base;
		setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, name));
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
	}

	public IEStairsBlock setRenderLayer(BlockRenderLayer layer)
	{
		this.layer = layer;
		return this;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return layer;
	}

	@Override
	public boolean hasFlavour()
	{
		return base.hasFlavour();
	}

	@Override
	public String getNameForFlavour()
	{
		return base.getNameForFlavour();
	}
}
