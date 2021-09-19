/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Supplier;

public class MetalBarrelMinecartEntity extends BarrelMinecartEntity
{
	public static final EntityType<MetalBarrelMinecartEntity> TYPE = Builder
			.<MetalBarrelMinecartEntity>of(MetalBarrelMinecartEntity::new, MobCategory.MISC)
			.sized(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_metalbarrel");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_metalbarrel");
	}

	public MetalBarrelMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public MetalBarrelMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Misc.cartMetalBarrel);
	}

	@Nonnull
	@Override
	public EntityType<?> getType()
	{
		return TYPE;
	}

	@Override
	protected Supplier<WoodenBarrelTileEntity> getTileProvider()
	{
		return MetalBarrelTileEntity::new;
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.MetalDevices.barrel.defaultBlockState();
	}

}
