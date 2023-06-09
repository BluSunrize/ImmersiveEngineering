/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class BarrelBlock
{
	public static IEEntityBlock<? extends WoodenBarrelBlockEntity> make(BlockBehaviour.Properties props, boolean metal)
	{
		if(metal)
			return new IEEntityBlock<>(IEBlockEntities.METAL_BARREL, props);
		else
			return new IEEntityBlock<>(IEBlockEntities.WOODEN_BARREL, props);
	}

	public static Block.Properties getProperties(boolean metal)
	{
		Block.Properties base;
		if(metal)
			base = Block.Properties.of()
					.mapColor(MapColor.METAL)
					.sound(SoundType.METAL);
		else
			base = Block.Properties.of()
					.mapColor(MapColor.WOOD)
					.ignitedByLava()
					.instrument(NoteBlockInstrument.BASS)
					.sound(SoundType.WOOD);
		return base.strength(2, 5);
	}
}
