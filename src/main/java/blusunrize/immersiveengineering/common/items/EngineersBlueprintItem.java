/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EngineersBlueprintItem extends IEBaseItem
{
	public EngineersBlueprintItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Nonnull
	@Override
	public String getDescriptionId(ItemStack stack)
	{
		return this.getDescriptionId();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		String key = getCategory(stack);
		if(key.isEmpty())
			return;
		String formatKey = Lib.DESC_INFO+"blueprint."+key;
		String formatted = I18n.get(formatKey);
		if(formatKey.equals(formatted))
			list.add(Component.literal(key).withStyle(ChatFormatting.GRAY));
		else
			list.add(Component.translatable(formatKey).withStyle(ChatFormatting.GRAY));
		if(world==null)
			return;
		List<RecipeHolder<BlueprintCraftingRecipe>> recipes = BlueprintCraftingRecipe.findRecipes(world, key);
		if(recipes.isEmpty())
			return;
		if(Screen.hasShiftDown())
		{
			list.add(Component.translatable(Lib.DESC_INFO+"blueprint.creates1").withStyle(ChatFormatting.GRAY));
			for(RecipeHolder<BlueprintCraftingRecipe> recipe : recipes)
				list.add(Component.literal(" ").append(recipe.value().output.get().getHoverName()).withStyle(ChatFormatting.DARK_GRAY));
		}
		else
			list.add(Component.translatable(Lib.DESC_INFO+"blueprint.creates0").withStyle(ChatFormatting.GRAY));
	}


	@Override
	public void fillCreativeTab(Output out)
	{
		final Level level = ImmersiveEngineering.proxy.getClientWorld();
		if(level!=null)
			for(String key : BlueprintCraftingRecipe.getCategoriesWithRecipes(level))
			{
				ItemStack stack = new ItemStack(this);
				ItemNBTHelper.putString(stack, "blueprint", key);
				out.accept(stack);
			}
	}

	@Nonnull
	public static List<RecipeHolder<BlueprintCraftingRecipe>> getRecipes(Level level, ItemStack stack)
	{
		return BlueprintCraftingRecipe.findRecipes(level, getCategory(stack));
	}

	public static String getCategory(ItemStack stack)
	{
		return ItemNBTHelper.getString(stack, "blueprint");
	}
}