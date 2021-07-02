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
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.function.Supplier;

public class PottedHempBlock extends FlowerPotBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.create(Material.MISCELLANEOUS)
			.hardnessAndResistance(0.0F)
			.notSolid();

	public PottedHempBlock(Properties props)
	{
		super(() -> (FlowerPotBlock)Blocks.FLOWER_POT, () -> IEBlocks.Misc.hempPlant.get(), props);
		GenericDeferredWork.registerPotablePlant(Misc.hempPlant.get().getRegistryName(), this);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		//NOP
	}
}