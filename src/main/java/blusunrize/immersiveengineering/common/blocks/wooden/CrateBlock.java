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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class CrateBlock extends IETileProviderBlock<WoodenCrateTileEntity>
{
	private final boolean reinforced;

	public CrateBlock(String name, boolean reinforced)
	{
		super(name, IETileTypes.WOODEN_CRATE, Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2, 5));
		this.reinforced = reinforced;
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion)
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
