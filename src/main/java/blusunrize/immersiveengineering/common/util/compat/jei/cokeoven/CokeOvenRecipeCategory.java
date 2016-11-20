package blusunrize.immersiveengineering.common.util.compat.jei.cokeoven;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
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
import net.minecraftforge.fluids.FluidStack;

public class CokeOvenRecipeCategory extends IERecipeCategory
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/cokeOven.png");
	private final IDrawable tankOverlay;
	
	public CokeOvenRecipeCategory(IGuiHelper helper)
	{
		super("cokeoven","tile.immersiveengineering.stoneDevice.coke_oven.name", helper.createDrawable(background, 8,13, 142, 60), CokeOvenRecipeWrapper.class, new ItemStack(IEContent.blockStoneDevice,1,BlockTypes_StoneDevices.COKE_OVEN.getMeta()));
		tankOverlay = helper.createDrawable(background, 176,31, 16,47, -2,2,-2,2);
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
		guiItemStacks.init(0, true, 21, 21);
		guiItemStacks.init(1, false, 76, 21);
		if(recipeWrapper instanceof CokeOvenRecipeWrapper)
		{
			CokeOvenRecipeWrapper recipe = (CokeOvenRecipeWrapper) recipeWrapper;
			guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
			guiItemStacks.set(1, ingredients.getOutputs(ItemStack.class));
			
			recipeLayout.getFluidStacks().init(0, false, 121,7, 16,47, 12000, false, tankOverlay);
			recipeLayout.getFluidStacks().set(0, ingredients.getOutputs(FluidStack.class));
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}
}