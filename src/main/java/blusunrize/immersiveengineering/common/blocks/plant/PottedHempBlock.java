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
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.material.Material;

public class PottedHempBlock extends FlowerPotBlock
{
	public PottedHempBlock(String name)
	{
		super(() -> (FlowerPotBlock)Blocks.FLOWER_POT, () -> IEBlocks.Misc.hempPlant,
				Block.Properties.of(Material.DECORATION).strength(0.0F).noOcclusion());
		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(Misc.hempPlant.getRegistryName(), () -> this);
		setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEBlocks.add(this);
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		//NOP
	}
}