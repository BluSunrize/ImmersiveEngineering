/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.RecipeSerializers.SimpleSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RecipePowerpack implements IRecipe
{
	public static final IRecipeSerializer<RecipePowerpack> SERIALIZER = RecipeSerializers.register(
			new SimpleSerializer<>(ImmersiveEngineering.MODID+":powerpack", RecipePowerpack::new)
	);

	private final ResourceLocation id;

	public RecipePowerpack(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(IInventory inv, World world)
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
	public ItemStack getCraftingResult(IInventory inv)
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
		return new ItemStack(IEContent.itemPowerpack, 1);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(IInventory inv)
	{
		NonNullList<ItemStack> remaining = IRecipe.super.getRemainingItems(inv);
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
		if(!(stack.getItem() instanceof ArmorItem)||((ArmorItem)stack.getItem()).getEquipmentSlot()!=EquipmentSlotType.CHEST)
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

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}
}