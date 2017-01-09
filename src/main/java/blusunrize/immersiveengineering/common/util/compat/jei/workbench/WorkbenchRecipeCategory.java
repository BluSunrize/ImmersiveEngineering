package blusunrize.immersiveengineering.common.util.compat.jei.workbench;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice0;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class WorkbenchRecipeCategory extends IERecipeCategory<BlueprintCraftingRecipe, WorkbenchRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/workbench.png");
	private final IDrawable slotDrawable;
	public WorkbenchRecipeCategory(IGuiHelper helper)
	{
		super("workbench","tile.immersiveengineering.woodenDevice0.workbench.name", helper.createDrawable(background, 0,0, 176,74), BlueprintCraftingRecipe.class, new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.WORKBENCH.getMeta()),new ItemStack(IEContent.blockMetalMultiblock,1, BlockTypes_MetalMultiblock.AUTO_WORKBENCH.getMeta()));
		slotDrawable = helper.getSlotDrawable();
	}

	int[][] inputSlots;
	@Override
	@Deprecated
	public void setRecipe(IRecipeLayout recipeLayout, WorkbenchRecipeWrapper recipeWrapper)
	{
		//Deprecated
	}
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, WorkbenchRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 24, 16);
		inputSlots = new int[recipeWrapper.recipeInputs.length][];
		guiItemStacks.set(0, Lists.newArrayList(ItemEngineersBlueprint.getTypedBlueprint(recipeWrapper.blueprintCategory)));
		inputSlots[0] = new int[]{102,3};
		for(int i=0; i<recipeWrapper.recipeInputs.length; i++)
		{
			inputSlots[i] = new int[]{80+i%2*18, 20+i/2*18};
			guiItemStacks.init(1+i, true, inputSlots[i][0],inputSlots[i][1]);
			guiItemStacks.set(1+i, recipeWrapper.recipeInputs[i]);
		}
		guiItemStacks.init(1+recipeWrapper.recipeInputs.length, false, 140, 24);
		guiItemStacks.set(1+recipeWrapper.recipeInputs.length, recipeWrapper.recipeOutputs[0]);
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		slotDrawable.draw(minecraft, 24, 16);
		for(int[] ia : inputSlots)
			slotDrawable.draw(minecraft, ia[0],ia[1]);
		slotDrawable.draw(minecraft, 140, 24);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BlueprintCraftingRecipe recipe)
	{
		return new WorkbenchRecipeWrapper(recipe);
	}
}