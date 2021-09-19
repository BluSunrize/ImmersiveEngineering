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
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.google.common.collect.Lists;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;
import java.util.List;

public class WorkbenchRecipeCategory extends IERecipeCategory<BlueprintCraftingRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "workbench");

	public WorkbenchRecipeCategory(IGuiHelper helper)
	{
		super(BlueprintCraftingRecipe.class, helper, UID, "block.immersiveengineering.workbench");
		setBackground(helper.createDrawable(new ResourceLocation(Lib.MODID, "textures/gui/workbench.png"), 0, 11, 176, 54));
		setIcon(new ItemStack(IEBlocks.WoodenDevices.workbench));
	}

	@Override
	public void setIngredients(BlueprintCraftingRecipe recipe, IIngredients ingredients)
	{
		List<List<ItemStack>> l = JEIIngredientStackListBuilder.make(recipe.inputs).build();
		l.add(ListUtils.fromItems(BlueprintCraftingRecipe.getTypedBlueprint(recipe.blueprintCategory)));
		ingredients.setInputLists(VanillaTypes.ITEM, l);
		ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlueprintCraftingRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		int i = 0;
		guiItemStacks.init(i, true, 24, 5);
		guiItemStacks.set(i, Lists.newArrayList(BlueprintCraftingRecipe.getTypedBlueprint(recipe.blueprintCategory)));
		guiItemStacks.setBackground(i++, JEIHelper.slotDrawable);
		int y = recipe.inputs.length <= 4?12: 0;
		for(int j = 0; j < recipe.inputs.length; j++)
		{
			guiItemStacks.init(i, true, 80+j%2*18, y+j/2*18);
			guiItemStacks.set(i, Arrays.asList(recipe.inputs[j].getMatchingStacks()));
			guiItemStacks.setBackground(i++, JEIHelper.slotDrawable);
		}
		guiItemStacks.init(i, false, 140, 14);
		guiItemStacks.set(i, recipe.output);
		guiItemStacks.setBackground(i++, JEIHelper.slotDrawable);
	}
}