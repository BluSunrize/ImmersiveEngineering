/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.common.register.IEBlocks.Misc;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class PottedHempBlock extends FlowerPotBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.pushReaction(PushReaction.DESTROY)
			.strength(0.0F)
			.noOcclusion();

	public PottedHempBlock(Properties props)
	{
		super(() -> (FlowerPotBlock)Blocks.FLOWER_POT, Misc.HEMP_PLANT, props);
	}
}