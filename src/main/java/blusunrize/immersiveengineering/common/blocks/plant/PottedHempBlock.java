/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Misc;
import blusunrize.immersiveengineering.common.util.GenericDeferredWork;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

public class PottedHempBlock extends FlowerPotBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of(Material.DECORATION)
			.strength(0.0F)
			.noOcclusion();

	public PottedHempBlock(Properties props)
	{
		super(() -> (FlowerPotBlock)Blocks.FLOWER_POT, () -> IEBlocks.Misc.hempPlant.get(), props);
		GenericDeferredWork.registerPotablePlant(Misc.hempPlant.get().getRegistryName(), this);
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		//NOP
	}
}