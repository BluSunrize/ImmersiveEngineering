package blusunrize.immersiveengineering.common.util.compat.jei.squeezer;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
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

public class SqueezerRecipeCategory extends IERecipeCategory
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/squeezer.png");
	private final IDrawable tankOverlay;
	
	public SqueezerRecipeCategory(IGuiHelper helper)
	{
		super("squeezer","tile.immersiveengineering.metalMultiblock.squeezer.name", helper.createDrawable(background, 6,12, 164,59), SqueezerRecipeWrapper.class, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.SQUEEZER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177,31, 16,47, -2,2,-2,2);
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
		guiItemStacks.init(0, true, 1, 22);
		guiItemStacks.init(1, false, 84, 40);
		if(recipeWrapper instanceof SqueezerRecipeWrapper)
		{
			guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
			guiItemStacks.set(1, ingredients.getOutputs(ItemStack.class));
			
			recipeLayout.getFluidStacks().init(0, false, 106,9, 16,47, 24000, false, tankOverlay);
			recipeLayout.getFluidStacks().set(0, ingredients.getOutputs(FluidStack.class));
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}
}