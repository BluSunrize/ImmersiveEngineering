package blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;

public class ArcFurnaceRecipeCategory extends IERecipeCategory
{
	private final IDrawable slotDrawable;
	//	static ItemStack arcFurnaceStack;
	public ArcFurnaceRecipeCategory(IGuiHelper helper, String recipeType, Class wrapperClass)
	{
		super("arcFurnace"+(recipeType!=null?"."+recipeType:""),"tile.immersiveengineering.metalMultiblock.arc_furnace.name", helper.createBlankDrawable(140,50), wrapperClass);
		slotDrawable = helper.getSlotDrawable();
		if(recipeType!=null)
			this.localizedName+=" - "+recipeType;
		//		arcFurnaceStack = new ItemStack(IEContent.blockMetalMultiblock,1, BlockTypes_MetalMultiblock.ARC_FURNACE.getMeta());
	}
	public ArcFurnaceRecipeCategory(IGuiHelper helper)
	{
		this(helper,null,ArcFurnaceRecipeWrapper.class);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		int i = 0;
		guiItemStacks.init(i++, true, 20, 0);
		if(recipeWrapper instanceof ArcFurnaceRecipeWrapper)
		{
			ArcFurnaceRecipeWrapper recipe = (ArcFurnaceRecipeWrapper) recipeWrapper;
			guiItemStacks.set(0, recipe.recipeInputs[0]);
			for(int j=0; j<recipe.recipeInputs.length-1; j++)
				if(recipe.recipeInputs[i]!=null)
				{
					guiItemStacks.init(i, true, 12+j%2*18, 18+j/2*18);
					guiItemStacks.set(i, recipe.recipeInputs[i++]);
				}
			int outputSize = recipe.recipeOutputs.length;
			boolean hasSlag = recipe.getOutputs().size()>outputSize;
			for(int j=0; j<outputSize; j++)
			{
				int x = 122-(Math.min(outputSize-1,2)*18)+j%3*18;
				int y = (outputSize>3?0:18)+(j/3*18);
				guiItemStacks.init(i, false,  x, y);
				guiItemStacks.set(i++, recipe.recipeOutputs[j]);
			}
			if(hasSlag)
			{
				guiItemStacks.init(i, false,  122, 36);
				guiItemStacks.set(i++, recipe.getOutputs().get(recipe.getOutputs().size()-1));
			}
			//			guiItemStacks.set(1, recipe.recipeInputs[1]);
			//			guiItemStacks.set(2, recipe.getOutputs());
		}
		else
			Log.error("Unknown recipe wrapper type: {}", recipeWrapper);
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		slotDrawable.draw(minecraft, 20, 0);
		for(int j=0; j<4; j++)
			slotDrawable.draw(minecraft, 12+j%2*18, 18+j/2*18);
		for(int j=0; j<6; j++)
			slotDrawable.draw(minecraft, 86+j%3*18, 0+j/3*18);
		slotDrawable.draw(minecraft, 122, 36);
	}
}