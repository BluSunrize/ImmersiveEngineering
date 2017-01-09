package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class BlastFurnaceFuelCategory extends IERecipeCategory<Object, BlastFurnaceFuelWrapper>
{
	public static ResourceLocation background = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
	IDrawable flame;

	public BlastFurnaceFuelCategory(IGuiHelper helper)
	{
		super("blastfurnace.fuel","gui.immersiveengineering.blastFurnace.fuel", helper.createDrawable(background, 55, 38, 18, 32, 0, 0, 0, 80), Object.class);

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
	public void setRecipe(IRecipeLayout recipeLayout, BlastFurnaceFuelWrapper recipeWrapper)
	{
		//Deprecated
	}
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlastFurnaceFuelWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 14);
		guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
	}

	@Override
	public boolean isRecipeValid(Object recipe)
	{
		return BlastFurnaceRecipe.blastFuels.containsKey(recipe);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(Object recipe)
	{
		Integer value = BlastFurnaceRecipe.blastFuels.get(recipe);
		if(value!=null && value!=0)
		{
			List<ItemStack> list;
			if(recipe instanceof ItemStack)
				list = Arrays.asList((ItemStack)recipe);
			else
				list = (List<ItemStack>)recipe;
			return new BlastFurnaceFuelWrapper(JEIHelper.jeiHelpers.getGuiHelper(), list, value);
		}
		return null;
	}
}