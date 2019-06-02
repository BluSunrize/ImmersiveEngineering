/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.RecipeSerializers.SimpleSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RecipeShaderBags implements IRecipe
{
	public static final IRecipeSerializer<RecipeShaderBags> SERIALIZER = RecipeSerializers.register(
			new SimpleSerializer<>(ImmersiveEngineering.MODID+":shader_bags", RecipeShaderBags::new)
	);

	private final ResourceLocation id;

	public RecipeShaderBags(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(IInventory inv, @Nonnull World world)
	{
		ItemStack stack = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(stack.isEmpty())
				{
					if(IEContent.itemShaderBag.equals(stackInSlot.getItem())&&ItemNBTHelper.hasKey(stackInSlot, "rarity"))
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
	public ItemStack getCraftingResult(IInventory inv)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				ItemStack output = new ItemStack(IEContent.itemShaderBag, IEContent.itemShaderBag.equals(stackInSlot.getItem())?2: 1);
				EnumRarity next = ShaderRegistry.getLowerRarity(stackInSlot.getRarity());
				if(next!=null)
				{
					ItemNBTHelper.setString(output, "rarity", next.toString());
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
		return new ItemStack(IEContent.itemShaderBag, 2);
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}
}