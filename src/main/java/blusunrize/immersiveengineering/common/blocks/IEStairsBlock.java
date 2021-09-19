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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;

public class IEStairsBlock extends StairBlock implements IIEBlock
{
	private final IIEBlock base;

	public <T extends Block & IIEBlock> IEStairsBlock(String name, Properties properties, T base)
	{
		super(base.defaultBlockState(), properties);
		this.base = base;
		setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, name));
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
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
