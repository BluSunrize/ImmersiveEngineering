/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEBaseItem extends Item implements IColouredItem
{
	private int burnTime = -1;
	private boolean isHidden = false;

	public IEBaseItem()
	{
		this(new Properties());
	}

	public IEBaseItem(Properties props)
	{
		this(props, ImmersiveEngineering.ITEM_GROUP);
	}

	public IEBaseItem(Properties props, ItemGroup group)
	{
		super(props.group(group));
	}

	public IEBaseItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		return burnTime;
	}

	public boolean isHidden()
	{
		return isHidden;
	}

	protected void openGui(PlayerEntity player, Hand hand)
	{
		openGui(player, hand==Hand.MAIN_HAND?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND);
	}

	protected void openGui(PlayerEntity player, EquipmentSlotType slot)
	{
		ItemStack stack = player.getItemStackFromSlot(slot);
		NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider()
		{
			@Nonnull
			@Override
			public ITextComponent getDisplayName()
			{
				return new StringTextComponent("");
			}

			@Nullable
			@Override
			public Container createMenu(
					int i, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity
			)
			{
				if(!(stack.getItem() instanceof IEBaseItem))
					return null;
				ItemContainerType<?> containerType = ((IEBaseItem)stack.getItem()).getContainerType();
				if(containerType==null)
					return null;
				return containerType.create(i, playerInventory, playerEntity.world, slot, stack);
			}
			// Matches IEContainerTypes#register (for items)
		}, buffer -> buffer.writeInt(slot.ordinal()));
	}

	public static Item.Properties withIEOBJRender()
	{
		return ImmersiveEngineering.proxy.useIEOBJRenderer(new Properties());
	}

	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean isIERepairable(@Nonnull ItemStack stack)
	{
		return super.isRepairable(stack);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	@Nullable
	protected IEContainerTypes.ItemContainerType<?> getContainerType()
	{
		return null;
	}
}
