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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedCrateMinecartEntity extends CrateMinecartEntity
{
	public ReinforcedCrateMinecartEntity(Level world, double x, double y, double z)
	{
		super(IEEntityTypes.REINFORCED_CRATE_CART.get(), world, x, y, z);
	}

	public ReinforcedCrateMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
	}

	@Override
	public boolean ignoreExplosion()
	{
		return true;
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Minecarts.cartReinforcedCrate.get());
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.reinforcedCrate.defaultBlockState();
	}

}
