package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipePowerpack implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(earmuffs.isEmpty() && IEContent.itemPowerpack.equals(stackInSlot.getItem()))
					earmuffs = stackInSlot;
				else if(armor.isEmpty() && stackInSlot.getItem() instanceof ItemArmor && ((ItemArmor) stackInSlot.getItem()).armorType == EntityEquipmentSlot.CHEST && !ImmersiveEngineering.proxy.armorHasCustomModel(stackInSlot))
					armor = stackInSlot;
				else
					return false;
		}
		if(!earmuffs.isEmpty() && !armor.isEmpty())
			return true;
		else if(!armor.isEmpty() && ItemNBTHelper.hasKey(armor, "IE:Earmuffs") && earmuffs.isEmpty())
			return true;
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(earmuffs.isEmpty() && IEContent.itemPowerpack.equals(stackInSlot.getItem()))
					earmuffs = stackInSlot;
				else if(armor.isEmpty() && stackInSlot.getItem() instanceof ItemArmor && ((ItemArmor)stackInSlot.getItem()).armorType==EntityEquipmentSlot.CHEST && !ImmersiveEngineering.proxy.armorHasCustomModel(stackInSlot))
					armor = stackInSlot;
		}

		if(!earmuffs.isEmpty() && !armor.isEmpty())
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.setItemStack(output, "IE:Powerpack", earmuffs.copy());
			return output;
		}
		else if(!armor.isEmpty() && ItemNBTHelper.hasKey(armor, "IE:Powerpack"))
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.remove(output, "IE:Powerpack");
			return output;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(IEContent.itemPowerpack,1,0);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inv);
		for(int i=0;i<remaining.size();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty() && ItemNBTHelper.hasKey(stackInSlot, "IE:Powerpack"))
				remaining.set(i, ItemNBTHelper.getItemStack(stackInSlot, "IE:Powerpack"));
		}
		return remaining;
	}
}