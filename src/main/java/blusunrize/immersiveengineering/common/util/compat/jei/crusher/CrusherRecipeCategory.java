package blusunrize.immersiveengineering.common.util.compat.jei.crusher;

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

public class CrusherRecipeCategory extends IERecipeCategory
{
	private final IDrawable slotDrawable;
	static ItemStack crusherStack;
	public CrusherRecipeCategory(IGuiHelper helper)
	{
		super("crusher","tile.ImmersiveEngineering.metalMultiblock.crusher.name", helper.createBlankDrawable(140,50), CrusherRecipeWrapper.class);
		slotDrawable = helper.getSlotDrawable();
		crusherStack = new ItemStack(IEContent.blockMetalMultiblock,1, BlockTypes_MetalMultiblock.CRUSHER.getMeta());
	}

	int[][] outputSlots;
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 20, 3);
		guiItemStacks.init(1, false, 102, 3);
		if(recipeWrapper instanceof CrusherRecipeWrapper)
		{
			CrusherRecipeWrapper recipe = (CrusherRecipeWrapper) recipeWrapper;
			outputSlots = new int[recipe.recipeOutputs.length][];
			guiItemStacks.set(0, recipe.getInputs());
			guiItemStacks.set(1, recipe.recipeOutputs[0]);
			outputSlots[0] = new int[]{102,3};
			for(int i=1; i<recipe.recipeOutputs.length; i++)
			{
				outputSlots[i] = new int[]{102+(i-1)%2*18,21};
				guiItemStacks.init(i+1, false, outputSlots[i][0],outputSlots[i][1]);
				guiItemStacks.set(i+1, recipe.recipeOutputs[i]);
			}
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		slotDrawable.draw(minecraft, 20, 3);
		for(int[] ia : outputSlots)
			slotDrawable.draw(minecraft, ia[0],ia[1]);
	}
}