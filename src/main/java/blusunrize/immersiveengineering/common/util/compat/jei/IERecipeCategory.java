package blusunrize.immersiveengineering.common.util.compat.jei;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class IERecipeCategory<T, W extends IRecipeWrapper> implements IRecipeCategory<W>, IRecipeHandler<T>
{
	public String uniqueName;
	public String localizedName;
	private final IDrawable background;
	private final Class<T> recipeClass;

	public IERecipeCategory(String uniqueName, String localKey, IDrawable background, Class<T> recipeClass, ItemStack... displayStacks)
	{
		this.uniqueName = uniqueName;
		this.localizedName = I18n.format(localKey);
		this.background = background;
		this.recipeClass = recipeClass;
		for(ItemStack stack : displayStacks)
			JEIHelper.modRegistry.addRecipeCategoryCraftingItem(stack, getUid());
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
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		return Collections.emptyList();
	}

	@Override
	public Class<T> getRecipeClass()
	{
		return this.recipeClass;
	}

	@Override
	public String getRecipeCategoryUid(@Nonnull T recipe)
	{
		return "ie."+uniqueName;
	}

	@Override
	public boolean isRecipeValid(T recipe)
	{
		return true;
	}
}