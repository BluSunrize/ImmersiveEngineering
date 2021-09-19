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
import blusunrize.immersiveengineering.common.items.IEItems;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedCrateMinecartEntity extends CrateMinecartEntity
{
	public static final EntityType<ReinforcedCrateMinecartEntity> TYPE = Builder
			.<ReinforcedCrateMinecartEntity>of(ReinforcedCrateMinecartEntity::new, MobCategory.MISC)
			.sized(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_reinforcedcrate");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_reinforcedcrate");
	}

	public ReinforcedCrateMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
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
		return new ItemStack(IEItems.Misc.cartReinforcedCrate);
	}

	@Nonnull
	@Override
	public EntityType<?> getType()
	{
		return TYPE;
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.reinforcedCrate.defaultBlockState();
	}

}
