package blusunrize.immersiveengineering.common.util.compat.jei.squeezer;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.util.ResourceLocation;

public class SqueezerRecipeCategory extends IERecipeCategory
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/squeezer.png");
	private final IDrawable tankOverlay;
	
	public SqueezerRecipeCategory(IGuiHelper helper)
	{
		super("squeezer","tile.immersiveengineering.metalMultiblock.squeezer.name", helper.createDrawable(background, 6,12, 164,59), SqueezerRecipeWrapper.class);
		tankOverlay = helper.createDrawable(background, 177,31, 16,47, -2,2,-2,2);
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 1, 22);
		guiItemStacks.init(1, false, 84, 40);
		if(recipeWrapper instanceof SqueezerRecipeWrapper)
		{
			SqueezerRecipeWrapper recipe = (SqueezerRecipeWrapper) recipeWrapper;
			guiItemStacks.setFromRecipe(0, recipe.recipeInputs[0]);
			guiItemStacks.set(1, recipe.getOutputs());
			
			recipeLayout.getFluidStacks().init(0, false, 106,9, 16,47, 24000, false, tankOverlay);
			recipeLayout.getFluidStacks().set(0, recipe.getFluidOutputs());
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}
}