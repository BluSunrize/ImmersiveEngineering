/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StructuralArmBlock extends IETileProviderBlock
{
	public static final IProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;

	public StructuralArmBlock(String name)
	{
		super(name, Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3, 15),
				BlockItemIE::new, FACING, BlockStateProperties.WATERLOGGED);
		setNotNormalBlock();
		setBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return StructuralArmTileEntity.TYPE.create();
	}
}
