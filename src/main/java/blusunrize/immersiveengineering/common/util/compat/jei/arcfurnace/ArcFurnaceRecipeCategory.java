package blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ArcFurnaceRecipeCategory extends IERecipeCategory<ArcFurnaceRecipe, ArcFurnaceRecipeWrapper>
{
	private final String subType;
	//	static ItemStack arcFurnaceStack;
	public ArcFurnaceRecipeCategory(IGuiHelper helper, String recipeType, Class recipeClass)
	{
		super("arcFurnace"+(recipeType!=null?"."+recipeType: ""), "tile.immersiveengineering.metalMultiblock.arc_furnace.name", helper.createBlankDrawable(140, 50), recipeClass, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.ARC_FURNACE.getMeta()));
		subType = recipeType;
		if(recipeType!=null)
			this.localizedName += " - "+recipeType;
		//		arcFurnaceStack = new ItemStack(IEContent.blockMetalMultiblock,1, BlockTypes_MetalMultiblock.ARC_FURNACE.getMeta());
	}

	public ArcFurnaceRecipeCategory(IGuiHelper helper)
	{
		this(helper, null, ArcFurnaceRecipe.class);
	}

	@Override
	@Deprecated
	public void setRecipe(IRecipeLayout recipeLayout, ArcFurnaceRecipeWrapper recipeWrapper)
	{
		//Deprecated
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ArcFurnaceRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		int i = 0;
		guiItemStacks.init(i++, true, 20, 0);
		guiItemStacks.set(0, recipeWrapper.recipeInputs[0]);
		for(int j = 0; j < recipeWrapper.recipeInputs.length-1; j++)
			if(recipeWrapper.recipeInputs[i]!=null)
			{
				guiItemStacks.init(i, true, 12+j%2*18, 18+j/2*18);
				guiItemStacks.set(i, recipeWrapper.recipeInputs[i++]);
			}
		int outputSize = recipeWrapper.recipeOutputs.length;
		boolean hasSlag = recipeWrapper.getItemOut().size() > outputSize;
		for(int j = 0; j < outputSize; j++)
		{
			int x = 122-(Math.min(outputSize-1, 2)*18)+j%3*18;
			int y = (outputSize > 3?0: 18)+(j/3*18);
			guiItemStacks.init(i, false, x, y);
			guiItemStacks.set(i++, recipeWrapper.recipeOutputs[j]);
		}
		if(hasSlag)
		{
			guiItemStacks.init(i, false, 122, 36);
			guiItemStacks.set(i++, recipeWrapper.getItemOut().get(recipeWrapper.getItemOut().size()-1));
		}
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		JEIHelper.slotDrawable.draw(minecraft, 20, 0);
		for(int j = 0; j < 4; j++)
			JEIHelper.slotDrawable.draw(minecraft, 12+j%2*18, 18+j/2*18);
		for(int j = 0; j < 6; j++)
			JEIHelper.slotDrawable.draw(minecraft, 86+j%3*18, 0+j/3*18);
		JEIHelper.slotDrawable.draw(minecraft, 122, 36);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(ArcFurnaceRecipe recipe)
	{
		return ArcFurnaceRecipeWrapper.getWrapper(recipe);
	}
}