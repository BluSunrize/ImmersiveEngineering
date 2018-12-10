/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemMaintenanceKit extends ItemIEBase implements IGuiItem
{
	public ItemMaintenanceKit()
	{
		super("maintenance_kit", 1);
		this.setMaxDamage(50);
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_MaintenanceKit;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND?EntityEquipmentSlot.MAINHAND: EntityEquipmentSlot.OFFHAND);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}
}
