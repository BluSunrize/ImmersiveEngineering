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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CrateMinecartEntity extends IEMinecartEntity<WoodenCrateTileEntity>
{
	public CrateMinecartEntity(Level world, double x, double y, double z)
	{
		this(IEEntityTypes.CRATE_MINECART.get(), world, x, y, z);
	}

	public CrateMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public CrateMinecartEntity(EntityType<?> type, Level world)
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
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, containedTileEntity.getInventory());
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
			WoodenCrateTileEntity tile = new WoodenCrateTileEntity(BlockPos.ZERO, WoodenDevices.crate.defaultBlockState());
			tile.setOverrideState(getDisplayBlockState());
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
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.crate.defaultBlockState();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player)
	{
		return IEContainerTypes.CRATE_MINECART.construct(id, inv, this);
	}
}
