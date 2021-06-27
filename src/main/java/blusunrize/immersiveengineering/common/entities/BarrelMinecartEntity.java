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
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class BarrelMinecartEntity extends IEMinecartEntity<WoodenBarrelTileEntity>
{
	public static final EntityType<BarrelMinecartEntity> TYPE = Builder
			.<BarrelMinecartEntity>create(BarrelMinecartEntity::new, EntityClassification.MISC)
			.size(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_woodenbarrel");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_woodenbarrel");
	}

	public BarrelMinecartEntity(World world, double x, double y, double z)
	{
		this(TYPE, world, x, y, z);
	}

	public BarrelMinecartEntity(EntityType<?> type, World world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public BarrelMinecartEntity(EntityType<?> type, World world)
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
		CompoundNBT tag = new CompoundNBT();
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
	public ActionResultType processInitialInteract(PlayerEntity player, Hand hand)
	{
		if(super.processInitialInteract(player, hand) == ActionResultType.SUCCESS)
			return ActionResultType.SUCCESS;
		ItemStack itemstack = player.getHeldItem(hand);
		if(FluidUtil.getFluidHandler(itemstack).isPresent())
		{
			this.containedTileEntity.interact(null, player, hand, itemstack, 0, 0, 0);
			return ActionResultType.SUCCESS;//always return true to avoid placing lava in the world
		}
		return ActionResultType.PASS;
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
	public BlockState getDisplayTile()
	{
		return IEBlocks.WoodenDevices.woodenBarrel.getDefaultState();
	}

}
