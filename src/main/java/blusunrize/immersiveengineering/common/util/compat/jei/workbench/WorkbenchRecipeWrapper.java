package blusunrize.immersiveengineering.common.util.compat.jei.workbench;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class WorkbenchRecipeWrapper extends MultiblockRecipeWrapper
{
	String blueprintCategory;
	public WorkbenchRecipeWrapper(BlueprintCraftingRecipe recipe)
	{
		super(recipe);
		blueprintCategory = recipe.blueprintCategory;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
//		GlStateManager.pushMatrix();
//		ClientUtils.bindAtlas();
//		GlStateManager.translate(70F, 20F, 16.5F);
//		GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
//		GlStateManager.rotate(80.0F, 0.0F, 1.0F, 0.0F);
//		GlStateManager.scale(60, -60, 60);
//		minecraft.getRenderItem().renderItem(WorkbenchRecipeCategory.crusherStack, ItemCameraTransforms.TransformType.GUI);
//		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
	{
		return false;
	}
}