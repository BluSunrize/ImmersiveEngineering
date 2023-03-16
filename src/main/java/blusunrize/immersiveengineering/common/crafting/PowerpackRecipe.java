/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class PowerpackRecipe implements CraftingRecipe
{
	private final ResourceLocation id;

	public PowerpackRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public boolean matches(CraftingContainer inv, @Nonnull Level world)
	{
		ItemStack powerpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(powerpack.isEmpty()&&stackInSlot.is(Misc.POWERPACK.asItem()))
					powerpack = stackInSlot;
				else if(armor.isEmpty()&&isValidArmor(stackInSlot))
					armor = stackInSlot;
				else
					return false;
			}
		}
		if(!powerpack.isEmpty()&&!armor.isEmpty()&&!ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack))
			return true;
		else return !armor.isEmpty()&&ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack)&&powerpack.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess access)
	{
		ItemStack powerpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
				if(powerpack.isEmpty()&&stackInSlot.is(Misc.POWERPACK.asItem()))
					powerpack = stackInSlot;
				else if(armor.isEmpty()&&isValidArmor(stackInSlot))
					armor = stackInSlot;
		}

		if(!powerpack.isEmpty()&&!armor.isEmpty()&&!ItemNBTHelper.hasKey(armor, Lib.NBT_Powerpack))
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.setItemStack(output, Lib.NBT_Powerpack, ItemHandlerHelper.copyStackWithSize(powerpack, 1));
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
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return new ItemStack(Misc.POWERPACK, 1);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
	{
		NonNullList<ItemStack> remaining = CraftingRecipe.super.getRemainingItems(inv);
		for(int i = 0; i < remaining.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty()&&ItemNBTHelper.hasKey(stackInSlot, Lib.NBT_Powerpack))
				remaining.set(i, ItemNBTHelper.getItemStack(stackInSlot, Lib.NBT_Powerpack));
		}
		return remaining;
	}

	private boolean isValidArmor(ItemStack stack)
	{
		if(!(stack.getItem() instanceof ArmorItem armor)||armor.getEquipmentSlot()!=EquipmentSlot.CHEST)
			return false;
		if(stack.getItem()==Misc.POWERPACK.asItem())
			return false;
		return !stack.is(IETags.powerpackForbidAttach);
	}

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.POWERPACK_SERIALIZER.get();
	}

	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.withSize(1, Ingredient.of(Misc.POWERPACK));
	}

	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}