/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class CraftingTableTileEntity extends IEBaseTileEntity implements IIEInventory, IStateBasedDirectional, IInteractionObjectIE
{
	public static TileEntityType<CraftingTableTileEntity> TYPE;
	NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);

	public CraftingTableTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getList("inventory", NBT.TAG_COMPOUND), 27);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			writeInv(nbt, false);
		}
	}

	public void writeInv(CompoundNBT nbt, boolean toItem)
	{
		boolean write = false;
		ListNBT invList = new ListNBT();
		for(int i = 0; i < this.inventory.size(); i++)
			if(!this.inventory.get(i).isEmpty())
			{
				if(toItem)
					write = true;
				CompoundNBT itemTag = new CompoundNBT();
				itemTag.putByte("Slot", (byte)i);
				this.inventory.get(i).write(itemTag);
				invList.add(itemTag);
			}
		if(!toItem||write)
			nbt.put("inventory", invList);
	}

	@Override
	@Nonnull
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.immersiveengineering.craftingtable");
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
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
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player)
	{
		return IInteractionObjectIE.super.createMenu(id, playerInventory, player);
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
	}

	private LazyOptional<IItemHandler> insertionCap = registerConstantCap(new IEInventoryHandler(27, this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionCap.cast();
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
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}
}