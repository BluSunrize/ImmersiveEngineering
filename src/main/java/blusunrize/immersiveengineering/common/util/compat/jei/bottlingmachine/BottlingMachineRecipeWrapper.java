/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

public class BottlingMachineRecipeWrapper extends MultiblockRecipeWrapper
{
	public BottlingMachineRecipeWrapper(BottlingMachineRecipe recipe)
	{
		super(recipe);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		ClientUtils.drawSlot(101, 13, 16, 16);
		GlStateManager.pushMatrix();
		GlStateManager.enableDepth();
		ClientUtils.bindAtlas();
		GlStateManager.translate(45F, 20F, 16.5F);
		GlStateManager.scale(50, -50, 50);
		minecraft.getRenderItem().renderItem(BottlingMachineRecipeCategory.bottlignMachineStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.popMatrix();
	}
}