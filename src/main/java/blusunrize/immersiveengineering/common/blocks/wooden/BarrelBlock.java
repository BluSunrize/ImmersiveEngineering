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

public class BarrelBlock
{
	public static IETileProviderBlock<?> make(String name, boolean metal) {
		Block.Properties base;
		if(metal)
			base = Block.Properties.of(Material.METAL).sound(SoundType.METAL);
		else
			base = Block.Properties.of(Material.WOOD).sound(SoundType.WOOD);
		base = base.strength(2, 5);
		if (metal)
			return new IETileProviderBlock<>(name, IETileTypes.METAL_BARREL, base);
		else
			return new IETileProviderBlock<>(name, IETileTypes.WOODEN_BARREL, base);
	}
}
