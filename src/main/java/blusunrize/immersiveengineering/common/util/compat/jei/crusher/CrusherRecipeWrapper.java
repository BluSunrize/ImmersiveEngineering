/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.crusher;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

import java.awt.*;

public class CrusherRecipeWrapper extends MultiblockRecipeWrapper
{
	float[] chances;

	public CrusherRecipeWrapper(CrusherRecipe recipe)
	{
		super(recipe);
		chances = recipe.secondaryChance;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		for(int i = 0; i < recipeOutputs.length-1; i++)
		{
			int x = 82+i%2*44;
			int y = 21+i/2*18;
			JEIHelper.slotDrawable.draw(minecraft, x, y);
			if(i < chances.length)
			{
				minecraft.fontRenderer.drawString(Utils.formatDouble(chances[i]*100, "0.##")+"%", x+19, y+6, Color.gray.getRGB());
				GlStateManager.color(1, 1, 1, 1);
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.enableDepth();
		ClientUtils.bindAtlas();
		GlStateManager.translate(50F, 20F, 16.5F);
		GlStateManager.scale(50, -50, 50);
		minecraft.getRenderItem().renderItem(CrusherRecipeCategory.crusherStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.popMatrix();

	}
}