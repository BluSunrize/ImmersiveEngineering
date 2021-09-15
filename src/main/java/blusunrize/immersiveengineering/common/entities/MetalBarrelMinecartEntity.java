/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class MetalBarrelMinecartEntity extends BarrelMinecartEntity
{
	public MetalBarrelMinecartEntity(Level world, double x, double y, double z)
	{
		super(IEEntityTypes.METAL_BARREL_CART.get(), world, x, y, z);
	}

	public MetalBarrelMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Minecarts.CART_METAL_BARREL.get());
	}

	@Override
	protected Supplier<WoodenBarrelBlockEntity> getTileProvider()
	{
		return () -> new MetalBarrelBlockEntity(BlockPos.ZERO, MetalDevices.BARREL.defaultBlockState());
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.MetalDevices.BARREL.defaultBlockState();
	}

}
