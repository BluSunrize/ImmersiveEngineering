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
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class MetalBarrelMinecartEntity extends BarrelMinecartEntity
{
	public MetalBarrelMinecartEntity(World world, double x, double y, double z)
	{
		super(IEEntityTypes.METAL_BARREL_CART.get(), world, x, y, z);
	}

	public MetalBarrelMinecartEntity(EntityType<?> type, World world)
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
		return MetalBarrelTileEntity::new;
	}

	@Override
	public BlockState getDisplayTile()
	{
		return IEBlocks.MetalDevices.barrel.getDefaultState();
	}

}
