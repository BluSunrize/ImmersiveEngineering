package blusunrize.immersiveengineering.common.util.compat.jei.crusher;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

public class CrusherRecipeWrapper extends MultiblockRecipeWrapper
{
	public CrusherRecipeWrapper(CrusherRecipe recipe)
	{
		super(recipe);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		for(int i=1; i<recipeOutputs.length; i++)
			JEIHelper.slotDrawable.draw(minecraft, 102+(i-1)%2*18,21);
		GlStateManager.pushMatrix();
		ClientUtils.bindAtlas();
		GlStateManager.translate(70F, 20F, 16.5F);
		GlStateManager.scale(50, -50, 50);
		minecraft.getRenderItem().renderItem(CrusherRecipeCategory.crusherStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.popMatrix();
	}
}