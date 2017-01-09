package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

public class MetalPressRecipeWrapper extends MultiblockRecipeWrapper
{
	int testHash = 0;
	public MetalPressRecipeWrapper(MetalPressRecipe recipe)
	{
		super(recipe);
		testHash = recipe.testHash;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		GlStateManager.pushMatrix();
		ClientUtils.bindAtlas();
		GlStateManager.translate(60F, 20F, 16.5F);
		GlStateManager.scale(50, -50, 50);
		minecraft.getRenderItem().renderItem(MetalPressRecipeCategory.metalPressStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.popMatrix();
	}

	@Override
	public int hashCode()
	{
		return testHash;
	}
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof MetalPressRecipeWrapper)
			return other.hashCode()==this.hashCode();
		return false;
	}
}