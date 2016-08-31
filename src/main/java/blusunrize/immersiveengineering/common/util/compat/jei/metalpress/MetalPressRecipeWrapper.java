package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

import java.util.ArrayList;
import java.util.List;

public class MetalPressRecipeWrapper extends MultiblockRecipeWrapper
{
	public MetalPressRecipeWrapper(MetalPressRecipe recipe)
	{
		super(recipe);
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

	public static List<MetalPressRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers)
	{
		List<MetalPressRecipeWrapper> recipes = new ArrayList<>();
		for(MetalPressRecipe r : MetalPressRecipe.recipeList.values())
			recipes.add(new MetalPressRecipeWrapper(r));
		return recipes;
	}
}