/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.client.render.tooltip.RevolverServerTooltip;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.util.ListUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class SpeedloaderItem extends InternalStorageItem implements IBulletContainer
{
	public SpeedloaderItem()
	{
		super(new Properties().stacksTo(1), 8);
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide)
			openGui(player, hand);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Nullable
	@Override
	protected ItemContainerType<?> getContainerType()
	{
		return IEMenuTypes.REVOLVER;
	}

	public boolean isEmpty(ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv==null)
			return true;
		for(int i = 0; i < inv.getSlots(); i++)
		{
			ItemStack b = inv.getStackInSlot(i);
			if(!b.isEmpty()&&b.getItem() instanceof BulletItem)
				return false;
		}
		return true;
	}

	@Override
	public int getBulletCount(ItemStack container)
	{
		return getSlotCount();
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver)
	{
		return ListUtils.fromStream(getContainedItems(revolver).stream(), getSlotCount());
	}

	@Nonnull
	@Override
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack pStack)
	{
		return Optional.of(new RevolverServerTooltip(getBullets(pStack), getBulletCount(pStack)));
	}
}