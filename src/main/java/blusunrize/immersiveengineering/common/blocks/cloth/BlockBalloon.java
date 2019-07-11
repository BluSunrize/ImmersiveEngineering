/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBalloon extends BlockIETileProvider
{
	public BlockBalloon()
	{
		super("balloon", Properties.create(Material.WOOL).hardnessAndResistance(0.8F), ItemBlockBalloon.class);
		setHasColours();
		setLightOpacity(0);
		setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		setNotNormalBlock();
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(@Nonnull BlockState state)
	{
		return new TileEntityBalloon();
	}

	@Override
	public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallStrength)
	{
		entity.fallDistance = 0;
	}
}
