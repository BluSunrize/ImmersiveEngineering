/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import blusunrize.immersiveengineering.common.gui.CrateEntityContainer;
import blusunrize.immersiveengineering.common.register.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CrateMinecartEntity extends IEMinecartEntity<WoodenCrateBlockEntity>
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

	public static <T extends CrateMinecartEntity>
	void registerCapabilities(RegisterCapabilitiesEvent ev, Supplier<EntityType<T>> type)
	{
		ev.registerEntity(ItemHandler.ENTITY_AUTOMATION, type.get(), (e, $) -> e.containedBlockEntity.getInventoryCap());
	}

	@Override
	public ItemStack getPickResult()
	{
		return new ItemStack(IEItems.Minecarts.CART_WOODEN_CRATE.get());
	}

	@Override
	public void writeTileToItem(ItemStack itemStack)
	{
		itemStack.set(
				IEDataComponents.GENERIC_ITEMS,
				ItemContainerContents.fromItems(containedBlockEntity.getInventory())
		);
	}

	@Override
	public void readTileFromItem(LivingEntity placer, ItemStack itemStack)
	{
		this.containedBlockEntity.onBEPlaced(itemStack);
	}

	@Override
	protected Supplier<WoodenCrateBlockEntity> getTileProvider()
	{
		return () -> new WoodenCrateBlockEntity(BlockPos.ZERO, WoodenDevices.CRATE.defaultBlockState());
	}

	@Nonnull
	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.CRATE.defaultBlockState();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player)
	{
		return new CrateEntityContainer(IEMenuTypes.WOODEN_CRATE.get(), id, inv, this);
	}
}
