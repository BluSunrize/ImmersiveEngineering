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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
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
		return new ItemStack(IEItems.Minecarts.cartMetalBarrel.get());
	}

	@Override
	protected Supplier<WoodenBarrelTileEntity> getTileProvider()
	{
		return () -> new MetalBarrelTileEntity(BlockPos.ZERO, MetalDevices.barrel.defaultBlockState());
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.MetalDevices.barrel.defaultBlockState();
	}

}
