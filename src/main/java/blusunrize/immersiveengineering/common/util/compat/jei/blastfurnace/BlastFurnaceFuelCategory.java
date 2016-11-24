package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class BlastFurnaceFuelCategory extends IERecipeCategory
{
	public static ResourceLocation background = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
	IDrawable flame;

	public BlastFurnaceFuelCategory(IGuiHelper helper)
	{
		super("blastfurnace.fuel","gui.immersiveengineering.blastFurnace.fuel", helper.createDrawable(background, 55, 38, 18, 32, 0, 0, 0, 80), BlastFurnaceFuelWrapper.class);

		flame = helper.createDrawable(BlastFurnaceRecipeCategory.background, 176, 0, 14, 14);
	}

	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return flame;
	}

	@Override
	@Deprecated
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper)
	{
		//Deprecated
	}
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 14);
		if(recipeWrapper instanceof BlastFurnaceFuelWrapper)
			guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}
}