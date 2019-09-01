/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.mixer;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MixerRecipeCategory extends IERecipeCategory<MixerRecipe, MultiblockRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/mixer.png");
	private final IDrawable tankTexture;
	private final IDrawable tankOverlay;
	private final IDrawable arrowDrawable;

	public MixerRecipeCategory(IGuiHelper helper)
	{
		super("mixer", "tile.immersiveengineering.metal_multiblock.mixer.name", helper.createBlankDrawable(155, 60), MixerRecipe.class, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.MIXER.getMeta()));
		tankTexture = helper.createDrawable(background, 68, 8, 74, 60);
		tankOverlay = helper.createDrawable(background, 177, 31, 20, 51, -2, 2, -2, 2);
		arrowDrawable = helper.createDrawable(background, 178, 17, 18, 13);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MultiblockRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 48, 3, 58, 47, 2000, false, null);
		guiFluidStacks.set(0, recipeWrapper.getFluidIn());

		guiFluidStacks.init(1, false, 138, 2, 16, 47, 2000, false, tankOverlay);
		guiFluidStacks.set(1, ingredients.getOutputs(VanillaTypes.FLUID).get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		for(int i = 0; i < recipeWrapper.recipeInputs.length; i++)
		{
			int x = 0+(i%2)*18;
			int y = 0+i/2*18;
			guiItemStacks.init(i, true, x, y);
			guiItemStacks.set(i, recipeWrapper.recipeInputs[i]);
			guiItemStacks.setBackground(i, JEIHelper.slotDrawable);
		}
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		tankTexture.draw(minecraft, 40, 0);
		arrowDrawable.draw(minecraft, 117, 19);
		ClientUtils.drawSlot(138, 17, 16, 47);
	}


	@Override
	public IRecipeWrapper getRecipeWrapper(MixerRecipe recipe)
	{
		return new MultiblockRecipeWrapper(recipe);
	}
}