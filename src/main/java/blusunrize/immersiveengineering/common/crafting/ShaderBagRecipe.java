/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ShaderBagRecipe implements ICraftingRecipe
{
	private final ResourceLocation id;

	public ShaderBagRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world)
	{
		ItemStack stack = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(stack.isEmpty())
				{
					if(Misc.shaderBag.containsValue(stackInSlot.getItem()))
						stack = stackInSlot;
					else
						return false;
				}
				else
					return false;
		}
		return !stack.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				Rarity next = ShaderRegistry.getLowerRarity(stackInSlot.getRarity());
				ItemStack output = new ItemStack(Misc.shaderBag.get(next), next!=stackInSlot.getRarity()?2: 1);
				if(next!=null)
				{
					ItemNBTHelper.putString(output, "rarity", next.toString());
					return output;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(Misc.shaderBag.get(Rarity.COMMON), 2);
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.SHADER_BAG_SERIALIZER.get();
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}
}