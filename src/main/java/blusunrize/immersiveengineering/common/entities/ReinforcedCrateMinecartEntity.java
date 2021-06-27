/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ReinforcedCrateMinecartEntity extends CrateMinecartEntity
{
	public ReinforcedCrateMinecartEntity(World world, double x, double y, double z)
	{
		super(IEEntityTypes.REINFORCED_CRATE_CART.get(), world, x, y, z);
	}

	public ReinforcedCrateMinecartEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}

	@Override
	public boolean isImmuneToExplosions()
	{
		return true;
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Minecarts.cartReinforcedCrate.get());
	}

	@Override
	public BlockState getDisplayTile()
	{
		return IEBlocks.WoodenDevices.reinforcedCrate.getDefaultState();
	}

}
