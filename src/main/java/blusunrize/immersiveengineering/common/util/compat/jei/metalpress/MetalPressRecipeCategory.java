package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class MetalPressRecipeCategory extends IERecipeCategory
{
	private final IDrawable slotDrawable;
	static ItemStack metalPressStack;
	public MetalPressRecipeCategory(IGuiHelper helper)
	{
		super("metalPress","tile.ImmersiveEngineering.metalMultiblock.metal_press.name", helper.createBlankDrawable(140,50), MetalPressRecipeWrapper.class);
		slotDrawable = helper.getSlotDrawable();
		metalPressStack = new ItemStack(IEContent.blockMetalMultiblock,1, BlockTypes_MetalMultiblock.METAL_PRESS.getMeta());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 20, 3);
		guiItemStacks.init(1, true, 61, 0);
		guiItemStacks.init(2, false, 102, 3);
		if(recipeWrapper instanceof MetalPressRecipeWrapper)
		{
			MetalPressRecipeWrapper recipe = (MetalPressRecipeWrapper) recipeWrapper;
			guiItemStacks.set(0, recipe.recipeInputs[0]);
			guiItemStacks.set(1, recipe.recipeInputs[1]);
			guiItemStacks.set(2, recipe.getOutputs());
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		slotDrawable.draw(minecraft, 20, 3);
		slotDrawable.draw(minecraft, 102, 3);
	}
}