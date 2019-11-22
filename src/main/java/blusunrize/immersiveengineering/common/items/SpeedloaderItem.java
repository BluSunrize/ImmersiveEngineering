/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpeedloaderItem extends InternalStorageItem implements ITool, IBulletContainer
{
	public SpeedloaderItem()
	{
		super("speedloader", new Properties().maxStackSize(1));
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 8;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			openGui(player, hand==Hand.MAIN_HAND?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND);
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	public boolean isEmpty(ItemStack stack)
	{
		return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).map(inv->
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
		return getSlotCount(container);
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver, boolean remote)
	{
		if(!remote&&isEmpty(revolver))
			remote = true;
		else if(remote&&!ItemNBTHelper.hasKey(revolver, "bullets"))
			remote = false;
		if(!remote)
			return ListUtils.fromItems(this.getContainedItems(revolver).subList(0, getSlotCount(revolver)));
		else
			return Utils.readInventory(revolver.getOrCreateTag().getList("bullets", 10), getSlotCount(revolver));
	}

	@Nullable
	@Override
	public CompoundNBT getShareTag(ItemStack stack)
	{
		CompoundNBT ret = super.getShareTag(stack);
		if(ret==null)
			ret = new CompoundNBT();
		else
			ret = ret.copy();
		final CompoundNBT retConst = ret;
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler->
		{
			NonNullList<ItemStack> bullets = NonNullList.withSize(getSlotCount(stack), ItemStack.EMPTY);
			for(int i = 0; i < getSlotCount(stack); i++)
				bullets.set(i, handler.getStackInSlot(i));
			retConst.put("bullets", Utils.writeInventory(bullets));
		});
		return retConst;
	}
}