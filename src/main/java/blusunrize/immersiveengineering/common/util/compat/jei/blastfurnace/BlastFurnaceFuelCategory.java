package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.util.ResourceLocation;

public class BlastFurnaceFuelCategory extends IERecipeCategory
{
	public static ResourceLocation background = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
	
	public BlastFurnaceFuelCategory(IGuiHelper helper)
	{
		super("blastfurnace.fuel","gui.ImmersiveEngineering.blastFurnace.fuel", helper.createDrawable(background, 55, 38, 18, 32, 0, 0, 0, 80), BlastFurnaceFuelWrapper.class);
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 14);
		if(recipeWrapper instanceof BlastFurnaceFuelWrapper)
		{
			BlastFurnaceFuelWrapper recipe = (BlastFurnaceFuelWrapper) recipeWrapper;
			guiItemStacks.setFromRecipe(0, recipe.getInputs());
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}
}