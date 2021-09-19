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
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class BarrelMinecartEntity extends IEMinecartEntity<WoodenBarrelTileEntity>
{
	public static final EntityType<BarrelMinecartEntity> TYPE = Builder
			.<BarrelMinecartEntity>of(BarrelMinecartEntity::new, MobCategory.MISC)
			.sized(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_woodenbarrel");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_woodenbarrel");
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
		return new ItemStack(IEItems.Misc.cartWoodenBarrel);
	}

	@Override
	public void writeTileToItem(ItemStack itemStack)
	{
		CompoundTag tag = new CompoundTag();
		this.containedTileEntity.writeTank(tag, true);
		if(!tag.isEmpty())
			itemStack.setTag(tag);
	}

	@Override
	public void readTileFromItem(LivingEntity placer, ItemStack itemStack)
	{
		this.containedTileEntity.readOnPlacement(placer, itemStack);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand)
	{
		if(super.interact(player, hand) == InteractionResult.SUCCESS)
			return InteractionResult.SUCCESS;
		ItemStack itemstack = player.getItemInHand(hand);
		if(FluidUtil.getFluidHandler(itemstack).isPresent())
		{
			this.containedTileEntity.interact(null, player, hand, itemstack, 0, 0, 0);
			return InteractionResult.SUCCESS;//always return true to avoid placing lava in the world
		}
		return InteractionResult.PASS;
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
		return WoodenBarrelTileEntity::new;
	}

	@Override
	protected void invalidateCaps()
	{
		if(this.containedTileEntity!=null)
			this.containedTileEntity.getCapability(FLUID_HANDLER_CAPABILITY).invalidate();
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.woodenBarrel.defaultBlockState();
	}

}
