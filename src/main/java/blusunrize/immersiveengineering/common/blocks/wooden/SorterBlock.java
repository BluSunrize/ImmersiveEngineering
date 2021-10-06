/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class SorterBlock
{
	public static IETileProviderBlock<?> make(String name, boolean fluid) {
		Block.Properties props = Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2F, 5F);
		if (fluid)
			return new IETileProviderBlock<>(name, IETileTypes.FLUID_SORTER, props);
		else
			return new IETileProviderBlock<>(name, IETileTypes.SORTER, props);
	}
}
