package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;

public class RecipePotionBullets implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack bullet = null;
		ItemStack potion = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(bullet==null && IEContent.itemBullet.equals(stackInSlot.getItem()) && stackInSlot.getItemDamage()==10)
					bullet = stackInSlot;
				else if(potion==null && stackInSlot.getItem() instanceof ItemPotion)
					potion = stackInSlot;
				else
					return false;
		}
		return bullet!=null&&potion!=null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack bullet = null;
		ItemStack potion = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(bullet==null && IEContent.itemBullet.equals(stackInSlot.getItem()) && stackInSlot.getItemDamage()==10)
					bullet = stackInSlot;
				else if(potion==null && stackInSlot.getItem() instanceof ItemPotion)
					potion = stackInSlot;
		}
		ItemStack newBullet = Utils.copyStackWithAmount(bullet, 1);
		ItemNBTHelper.setItemStack(newBullet, "potion", potion.copy());
		return newBullet;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(IEContent.itemBullet,1,10);
	}

}
