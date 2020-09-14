/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CrateBlock extends IETileProviderBlock
{

	private boolean reinforced;

	public CrateBlock(String name, boolean reinforced)
	{
		super(name, Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2, 5),
				BlockItemIE::new);
		this.reinforced = reinforced;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new WoodenCrateTileEntity();
	}

	@Override
	public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion)
	{
		if(reinforced)
			return 1200000;
		return super.getExplosionResistance(state, world, pos, explosion);
	}

	public boolean isReinforced()
	{
		return reinforced;
	}
}
