/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class ArcFurnaceRecipeCategory extends IERecipeCategory<ArcFurnaceRecipe, ArcFurnaceRecipeWrapper>
{
	private final IDrawable icon;

	private ArcFurnaceRecipeCategory(IGuiHelper helper, Class recipeClass, @Nullable String subtype, @Nullable IDrawable icon)
	{
		super("arcFurnace"+(subtype!=null?"."+subtype.toLowerCase():""), "tile.immersiveengineering.metal_multiblock.arc_furnace.name", helper.createBlankDrawable(140, 50), recipeClass, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.ARC_FURNACE.getMeta()));
		this.icon = icon;
		if(subtype!=null)
			this.localizedName+=" - "+subtype;
	}


	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return icon;
	}

	public static ArcFurnaceRecipeCategory getDefault(IGuiHelper helper)
	{
		return new ArcFurnaceRecipeCategory(helper, ArcFurnaceRecipe.class, null, null);
	}

	public static ArcFurnaceRecipeCategory getRecycling(IGuiHelper helper)
	{
		return new ArcFurnaceRecipeCategory(helper, ArcRecyclingRecipe.class, "Recycling", helper.createDrawable(new ResourceLocation("immersiveengineering:textures/gui/recycle.png"), 0,0,16,16, 16, 16));
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