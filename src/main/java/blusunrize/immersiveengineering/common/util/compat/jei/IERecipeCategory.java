/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class IERecipeCategory<T> implements IRecipeCategory<T>
{
	public final ResourceLocation uid;
	protected final IGuiHelper guiHelper;
	private final Class<? extends T> recipeClass;
	public MutableComponent title;
	private IDrawableStatic background;
	private IDrawable icon;

	public IERecipeCategory(Class<? extends T> recipeClass, IGuiHelper guiHelper, ResourceLocation uid, String localKey)
	{
		this.recipeClass = recipeClass;
		this.guiHelper = guiHelper;
		this.uid = uid;
		this.title = new TranslatableComponent(localKey);
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
		this.setIcon(this.guiHelper.createDrawableIngredient(VanillaTypes.ITEM, stack));
	}

	protected void setIcon(IDrawable icon)
	{
		this.icon = icon;
	}

	@Override
	public ResourceLocation getUid()
	{
		return this.uid;
	}

	@Override
	public Component getTitle()
	{
		return this.title;
	}

	@Override
	public Class<? extends T> getRecipeClass()
	{
		return this.recipeClass;
	}
}