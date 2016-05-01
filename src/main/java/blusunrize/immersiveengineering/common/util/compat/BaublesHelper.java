package blusunrize.immersiveengineering.common.util.compat;

import baubles.api.BaublesApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class BaublesHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
		Lib.BAUBLES = true;
		Config.setBoolean("baubles", true);
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
	}

	public static ItemStack getBauble(EntityPlayer player, int slot)
	{
		IInventory invBaubles = BaublesApi.getBaubles(player);
		if(invBaubles!=null)
			return invBaubles.getStackInSlot(slot);
		return null;
	}
	public static void setBauble(EntityPlayer player, int slot, ItemStack stack)
	{
		IInventory invBaubles = BaublesApi.getBaubles(player);
		if(invBaubles!=null)
			invBaubles.setInventorySlotContents(slot, stack);
	}
}