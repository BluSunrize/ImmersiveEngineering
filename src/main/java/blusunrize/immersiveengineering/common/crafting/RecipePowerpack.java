/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config;
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

public class RecipePowerpack extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack powerpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(powerpack.isEmpty()&&IEContent.itemPowerpack.equals(stackInSlot.getItem()))
					powerpack = stackInSlot;
				else if(armor.isEmpty()&&isValidArmor(stackInSlot))
					armor = stackInSlot;
				else
					return false;
		}
		if(!powerpack.isEmpty()&&!armor.isEmpty()&&!ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack))
			return true;
		else return !armor.isEmpty()&&ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack)&&powerpack.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack powerpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(powerpack.isEmpty()&&IEContent.itemPowerpack.equals(stackInSlot.getItem()))
					powerpack = stackInSlot;
				else if(armor.isEmpty()&&isValidArmor(stackInSlot))
					armor = stackInSlot;
		}

		if(!powerpack.isEmpty()&&!armor.isEmpty()&&!ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack))
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.setItemStack(output, Lib.NBT_Powerpack, powerpack.copy());

			return output;
		}
		else if(!armor.isEmpty()&&ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack))
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.remove(output, Lib.NBT_Powerpack);
			return output;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(IEContent.itemPowerpack, 1, 0);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inv);
		for(int i = 0; i < remaining.size(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty()&&ItemNBTHelper.hasKey(stackInSlot, Lib.NBT_Powerpack))
				remaining.set(i, ItemNBTHelper.getItemStack(stackInSlot, Lib.NBT_Powerpack));
		}
		return remaining;
	}

	private boolean isValidArmor(ItemStack stack)
	{
		if(!(stack.getItem() instanceof ItemArmor)||((ItemArmor)stack.getItem()).armorType!=EntityEquipmentSlot.CHEST)
			return false;
		if(stack.getItem()==IEContent.itemPowerpack)
			return false;
		String regName = stack.getItem().getRegistryName().toString();
		for(String s : Config.IEConfig.Tools.powerpack_whitelist)
			if(regName.equals(s))
				return true;
		for(String s : Config.IEConfig.Tools.powerpack_blacklist)
			if(regName.equals(s))
				return false;
		return true;
	}
}