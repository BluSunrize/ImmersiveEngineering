/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemSpeedloader;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.RecipeSerializers.SimpleSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RecipeSpeedloader extends ShapedRecipe
{
	public static final IRecipeSerializer<RecipeSpeedloader> SERIALIZER = RecipeSerializers.register(
			new SimpleSerializer<>(ImmersiveEngineering.MODID+":speedloader", RecipeSpeedloader::new)
	);

	public RecipeSpeedloader(ResourceLocation id)
	{
		super(id, null, 3, 3, getPattern(), new ItemStack(IEContent.itemSpeedloader));
	}

	private static NonNullList<Ingredient> getPattern()
	{
		Ingredient bullet = Ingredient.fromItems(IEContent.itemBullet);
		Ingredient speedloader = Ingredient.fromItems(IEContent.itemSpeedloader);
		return NonNullList.from(Ingredient.EMPTY,
				bullet, bullet, bullet,
				bullet, speedloader, bullet,
				bullet, bullet, bullet);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(IInventory matrix)
	{
		ItemStack speedloader = matrix.getStackInSlot(4);

		if(!speedloader.isEmpty()&&speedloader.getItem() instanceof ItemSpeedloader&&((ItemSpeedloader)speedloader.getItem()).isEmpty(speedloader))
		{
			ItemStack out = speedloader.copy();
			NonNullList<ItemStack> fill = NonNullList.withSize(8, ItemStack.EMPTY);
			for(int i = 0; i < 8; i++)
			{
				int j = i >= 4?i+1: i;
				fill.set(i, Utils.copyStackWithAmount(matrix.getStackInSlot(j), 1));
			}
			((ItemSpeedloader)out.getItem()).setContainedItems(out, fill);
			return out;
		}
		else
			return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}
}