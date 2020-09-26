/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Misc;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PottedHempBlock extends FlowerPotBlock
{
	public PottedHempBlock(String name)
	{
		super(() -> (FlowerPotBlock)Blocks.FLOWER_POT, () -> IEBlocks.Misc.hempPlant,
				Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).notSolid());
		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(Misc.hempPlant.getRegistryName(), () -> this);
		setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEBlocks.add(this);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		//NOP
	}
}