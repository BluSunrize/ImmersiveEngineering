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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class SpeedloaderItem extends InternalStorageItem implements IBulletContainer
{
	public SpeedloaderItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public int getSlotCount()
	{
		return 8;
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
		return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).map(inv ->
		{
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack b = inv.getStackInSlot(i);
				if(!b.isEmpty()&&b.getItem() instanceof BulletItem)
					return false;
			}
			return true;
		}).orElse(true);
	}

	@Override
	public int getBulletCount(ItemStack container)
	{
		return getSlotCount();
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver)
	{
		return ListUtils.fromItems(this.getContainedItems(revolver).subList(0, getSlotCount()));
	}

	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack)
	{
		return RevolverItem.copyBulletsToShareTag(stack, super.getShareTag(stack));
	}

	@Override
	public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt)
	{
		super.readShareTag(stack, nbt);
		RevolverItem.readBulletsFromShareTag(stack, nbt);
	}

	@Nonnull
	@Override
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack pStack)
	{
		return Optional.of(new RevolverServerTooltip(getBullets(pStack), getBulletCount(pStack)));
	}
}