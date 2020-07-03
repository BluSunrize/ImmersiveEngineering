/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MetalMultiblockBlock extends IEMultiblockBlock
{
	private final Supplier<TileEntityType<?>> tileType;

	public MetalMultiblockBlock(String name, Supplier<TileEntityType<?>> te, Property<?>... additionalProperties)
	{
		super(name, Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3, 15).notSolid(),
				ArrayUtils.addAll(additionalProperties,
						IEProperties.MIRRORED));
		tileType = te;
		lightOpacity = 0;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
