/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.workbench;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class WorkbenchRecipeCategory extends IERecipeCategory<BlueprintCraftingRecipe>
{
	public WorkbenchRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.BLUEPRINT, "block.immersiveengineering.workbench");
		setBackground(helper.createDrawable(new ResourceLocation(Lib.MODID, "textures/gui/workbench.png"), 0, 11, 176, 54));
		setIcon(new ItemStack(IEBlocks.WoodenDevices.WORKBENCH));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BlueprintCraftingRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 25, 6)
				.addItemStacks(Lists.newArrayList(BlueprintCraftingRecipe.getTypedBlueprint(recipe.blueprintCategory)))
				.setBackground(JEIHelper.slotDrawable, -1, -1);
		int y = recipe.inputs.size() <= 4?13: 1;
		for(int j = 0; j < recipe.inputs.size(); j++)
			builder.addSlot(RecipeIngredientRole.INPUT, 81+j%2*18, y+j/2*18)
					.addItemStacks(Arrays.asList(recipe.inputs.get(j).getMatchingStacks()))
					.setBackground(JEIHelper.slotDrawable, -1, -1);
		builder.addSlot(RecipeIngredientRole.OUTPUT, 141, 15)
				.addItemStack(recipe.output.get())
				.setBackground(JEIHelper.slotDrawable, -1, -1);
	}
}