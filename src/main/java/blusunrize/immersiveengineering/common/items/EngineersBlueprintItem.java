/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EngineersBlueprintItem extends IEBaseItem
{
	public EngineersBlueprintItem()
	{
		super("blueprint", new Properties().maxStackSize(1));
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return this.getTranslationKey();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		String key = getCategory(stack);
		if(!key.isEmpty()
				&&BlueprintCraftingRecipe.recipeCategories.contains(key))
		{
			String formatKey = Lib.DESC_INFO+"blueprint."+key;
			String formatted = I18n.format(formatKey);
			if(formatKey.equals(formatted))
				list.add(new StringTextComponent(key));
			else
				list.add(new TranslationTextComponent(formatKey));
			if(Screen.hasShiftDown())
			{
				list.add(new TranslationTextComponent(Lib.DESC_INFO+"blueprint.creates1"));
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(key);
				if(recipes.length > 0)
					for(BlueprintCraftingRecipe recipe : recipes)
						list.add(new StringTextComponent(" ").appendSibling(recipe.output.getDisplayName()));
			}
			else
				list.add(new TranslationTextComponent(Lib.DESC_INFO+"blueprint.creates0"));
		}
	}


	@Override
	public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> list)
	{
		if(this.isInGroup(tab))
			for(String key : BlueprintCraftingRecipe.recipeCategories)
			{
				ItemStack stack = new ItemStack(this);
				ItemNBTHelper.putString(stack, "blueprint", key);
				list.add(stack);
			}
	}

	@Nonnull
	public static BlueprintCraftingRecipe[] getRecipes(ItemStack stack)
	{
		return BlueprintCraftingRecipe.findRecipes(getCategory(stack));
	}

	public static String getCategory(ItemStack stack)
	{
		return ItemNBTHelper.getString(stack, "blueprint");
	}
}