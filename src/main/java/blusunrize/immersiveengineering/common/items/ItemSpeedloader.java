/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class ItemSpeedloader extends ItemInternalStorage implements ITool, IGuiItem, IBulletContainer
{
	public ItemSpeedloader()
	{
		super("speedloader", 1);
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 8;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND?EntityEquipmentSlot.MAINHAND: EntityEquipmentSlot.OFFHAND);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	public boolean isEmpty(ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(inv!=null)
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack b = inv.getStackInSlot(i);
				if(!b.isEmpty()&&b.getItem() instanceof ItemBullet&&ItemNBTHelper.hasKey(b, "bullet"))
					return false;
			}
		return true;
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_Revolver;
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
			return Utils.readInventory(ItemNBTHelper.getTag(revolver).getTagList("bullets", 10), getSlotCount(revolver));
	}

	@Nullable
	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack)
	{
		NBTTagCompound ret = super.getNBTShareTag(stack);
		if(ret==null)
			ret = new NBTTagCompound();
		else
			ret = ret.copy();
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(handler!=null)
		{
			NonNullList<ItemStack> bullets = NonNullList.withSize(getSlotCount(stack), ItemStack.EMPTY);
			for(int i = 0; i < getSlotCount(stack); i++)
				bullets.set(i, handler.getStackInSlot(i));
			ret.setTag("bullets", Utils.writeInventory(bullets));
		}
		return ret;
	}
}