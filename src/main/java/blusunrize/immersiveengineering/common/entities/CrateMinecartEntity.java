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
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CrateMinecartEntity extends IEMinecartEntity<WoodenCrateTileEntity>
{
	public CrateMinecartEntity(World world, double x, double y, double z)
	{
		this(IEEntityTypes.CRATE_MINECART.get(), world, x, y, z);
	}

	public CrateMinecartEntity(EntityType<?> type, World world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public CrateMinecartEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Minecarts.cartWoodenCrate.get());
	}

	@Override
	public void writeTileToItem(ItemStack itemStack)
	{
		CompoundNBT tag = new CompoundNBT();
		ItemStackHelper.saveAllItems(tag, containedTileEntity.getInventory());
		if(!tag.isEmpty())
			itemStack.setTag(tag);
	}

	@Override
	public void readTileFromItem(LivingEntity placer, ItemStack itemStack)
	{
		this.containedTileEntity.readOnPlacement(placer, itemStack);
	}

	@Override
	protected Supplier<WoodenCrateTileEntity> getTileProvider()
	{
		return () -> {
			WoodenCrateTileEntity tile = new WoodenCrateTileEntity();
			tile.setOverrideState(getDisplayTile());
			return tile;
		};
	}

	@Override
	protected void invalidateCaps()
	{
		if(this.containedTileEntity!=null)
			this.containedTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).invalidate();
	}

	@Nonnull
	@Override
	public BlockState getDisplayTile()
	{
		return IEBlocks.WoodenDevices.crate.getDefaultState();
	}

	@Nullable
	@Override
	public Container createMenu(int id, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player)
	{
		return IEContainerTypes.CRATE_MINECART.construct(id, inv, this);
	}
}
