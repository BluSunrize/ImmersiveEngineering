package blusunrize.immersiveengineering.common.util.compat.jei;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class IERecipeCategory<T> implements IRecipeCategory, IRecipeHandler<T>
{
	public String uniqueName;
	public String localizedName;
	private final IDrawable background;
	private final Class<T> wrapperClass;
	
	public IERecipeCategory(String uniqueName, String localKey, IDrawable background, Class<T> wrapperClass)
	{
		this.uniqueName = uniqueName;
		this.localizedName = I18n.format(localKey);
		this.background = background;
		this.wrapperClass = wrapperClass;
	}

	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return null;
	}

	@Override
	public String getUid()
	{
		return "ie."+uniqueName;
	}

	@Override
	public String getTitle()
	{
		return localizedName;
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
	}

	@Override
	public void drawAnimations(Minecraft minecraft)
	{
	}

	@Override
	public Class<T> getRecipeClass()
	{
		return this.wrapperClass;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return "ie."+uniqueName;
	}
	@Override
	public String getRecipeCategoryUid(@Nonnull Object recipe)
	{
		return "ie."+uniqueName;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(T recipe)
	{
		return (IRecipeWrapper)recipe;
	}

	@Override
	public boolean isRecipeValid(T recipe)
	{
		return true;
	}
}