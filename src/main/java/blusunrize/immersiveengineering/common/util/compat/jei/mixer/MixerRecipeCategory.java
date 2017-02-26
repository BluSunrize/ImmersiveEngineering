package blusunrize.immersiveengineering.common.util.compat.jei.mixer;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class MixerRecipeCategory extends IERecipeCategory<MixerRecipe, MixerRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/mixer.png");
	private final IDrawable tankOverlay;
	private final IDrawable arrowDrawable;

	public MixerRecipeCategory(IGuiHelper helper)
	{
		super("mixer","tile.immersiveengineering.metalMultiblock.mixer.name", helper.createDrawable(background, 68,8, 74,60), MixerRecipe.class, new ItemStack(IEContent.blockMetalMultiblock,1, BlockTypes_MetalMultiblock.MIXER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177,31, 20,51, -2,2,-2,2);
		arrowDrawable = helper.createDrawable(background, 178,17, 18,13);
	}

	@Override
	@Deprecated
	public void setRecipe(IRecipeLayout recipeLayout, MixerRecipeWrapper recipeWrapper)
	{
		//Deprecated
	}
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MixerRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 8,3, 58,47, 4000, false, null);
		guiFluidStacks.set(0, recipeWrapper.getFluidIn());
		guiFluidStacks.init(1, false, 100,2, 16,47, 4000, false, tankOverlay);
		guiFluidStacks.set(1, ingredients.getOutputs(FluidStack.class));

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		for(int i=0; i<recipeWrapper.recipeInputs.length; i++)
		{
			int x = -40+(i%2)*18;
			int y = 0+i/2*18;
			guiItemStacks.init(i, true, x,y);
			guiItemStacks.set(i, recipeWrapper.recipeInputs[i]);
		}
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		arrowDrawable.draw(minecraft, 77,19);
		ClientUtils.drawSlot(100,17,16,47);
	}



	@Override
	public IRecipeWrapper getRecipeWrapper(MixerRecipe recipe)
	{
		return new MixerRecipeWrapper(recipe);
	}
}