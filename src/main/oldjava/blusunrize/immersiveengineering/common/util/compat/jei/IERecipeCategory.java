/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;

public abstract class IERecipeCategory<T extends Recipe<?>> implements IRecipeCategory<RecipeHolder<T>>
{
	protected final IGuiHelper guiHelper;
	private final RecipeType<RecipeHolder<T>> type;
	public MutableComponent title;
	private IDrawableStatic background;
	private IDrawable icon;

	public IERecipeCategory(IGuiHelper guiHelper, RecipeType<RecipeHolder<T>> type, String localKey)
	{
		this.guiHelper = guiHelper;
		this.type = type;
		this.title = Component.translatable(localKey);
	}

	@Override
	public IDrawable getBackground()
	{
		return this.background;
	}

	protected void setBackground(IDrawableStatic background)
	{
		this.background = background;
	}

	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return this.icon;
	}

	protected void setIcon(ItemStack stack)
	{
		this.setIcon(this.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack));
	}

	protected void setIcon(IDrawable icon)
	{
		this.icon = icon;
	}

	@Override
	public Component getTitle()
	{
		return this.title;
	}

	@Override
	public final RecipeType<RecipeHolder<T>> getRecipeType()
	{
		return type;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipe, IFocusGroup focuses)
	{
		setRecipe(builder, recipe.value(), focuses);
	}

	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses)
	{
	}

	@Override
	public void draw(RecipeHolder<T> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		draw(recipe.value(), recipeSlotsView, graphics, mouseX, mouseY);
	}

	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
	}
}