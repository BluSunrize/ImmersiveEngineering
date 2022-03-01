/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CraftingTableTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IInteractionObjectIE
{
	public static final int GRID_SIZE = 3;

	private final NonNullList<ItemStack> inventory = NonNullList.withSize(18, ItemStack.EMPTY);
	private final NonNullList<ItemStack> craftingInv = NonNullList.withSize(GRID_SIZE*GRID_SIZE, ItemStack.EMPTY);

	public CraftingTableTileEntity()
	{
		super(IETileTypes.CRAFTING_TABLE.get());
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> totalInv = NonNullList.withSize(inventory.size()+craftingInv.size(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(nbt, totalInv);
			for(int i = 0; i < inventory.size(); ++i)
				inventory.set(i, totalInv.get(i));
			for(int i = 0; i < craftingInv.size(); ++i)
				craftingInv.set(i, totalInv.get(inventory.size()+i));
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> totalInv = NonNullList.withSize(inventory.size()+craftingInv.size(), ItemStack.EMPTY);
			for(int i = 0; i < inventory.size(); ++i)
				totalInv.set(i, totalInv.get(i));
			for(int i = 0; i < craftingInv.size(); ++i)
				totalInv.set(inventory.size()+i, totalInv.get(i));
			ContainerHelper.saveAllItems(nbt, totalInv);
		}
	}

	@Override
	@Nonnull
	public Component getDisplayName()
	{
		return new TranslatableComponent("block.immersiveengineering.craftingtable");
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return this;
	}

	@Nonnull
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player)
	{
		return IInteractionObjectIE.super.createMenu(id, playerInventory, player);
	}

	private LazyOptional<IItemHandler> inventoryCap = registerConstantCap(new ItemStackHandler(inventory)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			super.onContentsChanged(slot);
			CraftingTableTileEntity.this.setChanged();
		}
	});

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return inventoryCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	public NonNullList<ItemStack> getCraftingInventory()
	{
		return craftingInv;
	}
}