/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class CushionBlock extends IEBaseBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.create(Material.WOOL)
			.sound(SoundType.CLOTH)
			.hardnessAndResistance(0.8F);

	public CushionBlock(Properties props)
	{
		super(props);
	}

	@Override
	public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallStrength)
	{
		entity.fallDistance = 0;
	}
}
