/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.ItemContainerType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

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

	public IEBaseItem(Properties props, CreativeModeTab group)
	{
		super(props.tab(group));
	}

	public IEBaseItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return burnTime;
	}

	public boolean isHidden()
	{
		return isHidden;
	}

	protected void openGui(Player player, InteractionHand hand)
	{
		openGui(player, hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND);
	}

	protected void openGui(Player player, EquipmentSlot slot)
	{
		ItemStack stack = player.getItemBySlot(slot);
		NetworkHooks.openGui((ServerPlayer)player, new MenuProvider()
		{
			@Nonnull
			@Override
			public Component getDisplayName()
			{
				return new TextComponent("");
			}

			@Nullable
			@Override
			public AbstractContainerMenu createMenu(
					int i, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity
			)
			{
				if(!(stack.getItem() instanceof IEBaseItem))
					return null;
				ItemContainerType<?> containerType = ((IEBaseItem)stack.getItem()).getContainerType();
				if(containerType==null)
					return null;
				return containerType.create(i, playerInventory, playerEntity.level, slot, stack);
			}
			// Matches IEContainerTypes#register (for items)
		}, buffer -> buffer.writeInt(slot.ordinal()));
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

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return Mob.getEquipmentSlotForItem(stack)==armorType||getEquipmentSlot(stack)==armorType;
	}

	@Override
	public int getBarColor(ItemStack pStack)
	{
		// All our items use the vanilla color gradient, even if they use different getBarWidth implementation
		return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}
}
