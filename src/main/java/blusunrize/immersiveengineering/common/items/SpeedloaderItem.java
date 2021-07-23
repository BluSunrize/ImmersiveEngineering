/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.ListUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpeedloaderItem extends InternalStorageItem implements ITool, IBulletContainer
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
		return IEContainerTypes.REVOLVER;
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
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Override
	public int getBulletCount(ItemStack container)
	{
		return getSlotCount();
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver, boolean remote)
	{
		if(!remote&&isEmpty(revolver))
			remote = true;
		else if(remote&&!ItemNBTHelper.hasKey(revolver, "bullets"))
			remote = false;
		if(!remote)
			return ListUtils.fromItems(this.getContainedItems(revolver).subList(0, getSlotCount()));
		else
		{
			NonNullList<ItemStack> result = NonNullList.withSize(getSlotCount(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(revolver.getOrCreateTag().getCompound("bullets"), result);
			return result;
		}
	}

	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack)
	{
		CompoundTag ret = super.getShareTag(stack);
		if(ret==null)
			ret = new CompoundTag();
		else
			ret = ret.copy();
		final CompoundTag retConst = ret;
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler->
		{
			NonNullList<ItemStack> bullets = NonNullList.withSize(getSlotCount(), ItemStack.EMPTY);
			for(int i = 0; i < getSlotCount(); i++)
				bullets.set(i, handler.getStackInSlot(i));
			retConst.put("bullets", ContainerHelper.saveAllItems(new CompoundTag(), bullets));
		});
		return retConst;
	}
}