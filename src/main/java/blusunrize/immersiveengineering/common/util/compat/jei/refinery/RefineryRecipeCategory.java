package blusunrize.immersiveengineering.common.util.compat.jei.refinery;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class RefineryRecipeCategory extends IERecipeCategory
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/refinery.png");
	private final IDrawable tankOverlay;

	public RefineryRecipeCategory(IGuiHelper helper)
	{
		super("refinery","tile.immersiveengineering.metalMultiblock.refinery.name", helper.createDrawable(background, 6,10, 164,62), RefineryRecipeWrapper.class, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.REFINERY.getMeta()));
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
		if(recipeWrapper instanceof RefineryRecipeWrapper)
		{
			List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
			if(inputs.size()>0)
			{
				recipeLayout.getFluidStacks().init(0, true, 7,10, 16,47, 24000, false, tankOverlay);
				recipeLayout.getFluidStacks().set(0, inputs.get(0));

				if(inputs.size()>1)
				{
					recipeLayout.getFluidStacks().init(1, true, 55,10, 16,47, 24000, false, tankOverlay);
					recipeLayout.getFluidStacks().set(1, inputs.get(1));
				}
			}
			recipeLayout.getFluidStacks().init(2, false, 103,10, 16,47, 24000, false, tankOverlay);
			recipeLayout.getFluidStacks().set(2, ingredients.getOutputs(FluidStack.class));
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}
}