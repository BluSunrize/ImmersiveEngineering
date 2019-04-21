/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

public class BlockCrate extends BlockIETileProvider
{

	private boolean reinforced;

	public BlockCrate(String name, boolean reinforced)
	{
		super(name, Properties.create(Material.WOOD).hardnessAndResistance(2, 5),
				ItemBlockIEBase.class);
		this.reinforced = reinforced;
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockReader worldIn)
	{
		return new TileEntityWoodenCrate();
	}

	@Override
	public float getExplosionResistance(IBlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
	{
		if(reinforced)
			return 1200000;
		return super.getExplosionResistance(state, world, pos, exploder, explosion);
	}
}
