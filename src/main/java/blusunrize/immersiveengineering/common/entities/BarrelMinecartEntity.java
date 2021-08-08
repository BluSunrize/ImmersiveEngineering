/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidUtil;

import java.util.function.Supplier;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class BarrelMinecartEntity extends IEMinecartEntity<WoodenBarrelBlockEntity>
{
	public BarrelMinecartEntity(Level world, double x, double y, double z)
	{
		this(IEEntityTypes.BARREL_MINECART.get(), world, x, y, z);
	}

	public BarrelMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public BarrelMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Minecarts.cartWoodenBarrel.get());
	}

	@Override
	public void writeTileToItem(ItemStack itemStack)
	{
		CompoundTag tag = new CompoundTag();
		this.containedBlockEntity.writeTank(tag, true);
		if(!tag.isEmpty())
			itemStack.setTag(tag);
	}

	@Override
	public void readTileFromItem(LivingEntity placer, ItemStack itemStack)
	{
		this.containedBlockEntity.readOnPlacement(placer, itemStack);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand)
	{
		if(super.interact(player, hand) == InteractionResult.SUCCESS)
			return InteractionResult.SUCCESS;
		ItemStack itemstack = player.getItemInHand(hand);
		if(FluidUtil.getFluidHandler(itemstack).isPresent())
		{
			this.containedBlockEntity.interact(null, player, hand, itemstack, 0, 0, 0);
			return InteractionResult.SUCCESS;//always return true to avoid placing lava in the world
		}
		return InteractionResult.PASS;
	}

	@Override
	protected Supplier<WoodenBarrelBlockEntity> getTileProvider()
	{
		return () -> new WoodenBarrelBlockEntity(BlockPos.ZERO, WoodenDevices.woodenBarrel.defaultBlockState());
	}

	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		if(this.containedBlockEntity!=null)
			this.containedBlockEntity.getCapability(FLUID_HANDLER_CAPABILITY).invalidate();
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.woodenBarrel.defaultBlockState();
	}

}
