/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;
import java.util.List;

public class MetalPressRecipeCategory extends IERecipeCategory<MetalPressRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "metalpress");

	public MetalPressRecipeCategory(IGuiHelper helper)
	{
		super(MetalPressRecipe.class, helper, UID, "block.immersiveengineering.metal_press");
		setBackground(helper.createBlankDrawable(100, 50));
		setIcon(new ItemStack(IEBlocks.Multiblocks.metalPress));
	}

	@Override
	public void setIngredients(MetalPressRecipe recipe, IIngredients ingredients)
	{
		List<List<ItemStack>> l = JEIIngredientStackListBuilder.make(recipe.input).build();
		l.add(ListUtils.fromItems(recipe.mold.stack));
		ingredients.setInputLists(VanillaTypes.ITEM, l);
		ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MetalPressRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 12);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);

		guiItemStacks.init(1, true, 56, 0);
		guiItemStacks.set(1, recipe.mold.stack);

		guiItemStacks.init(2, false, 82, 12);
		guiItemStacks.set(2, recipe.output);
		guiItemStacks.setBackground(2, JEIHelper.slotDrawable);
	}

	@Override
	public void draw(MetalPressRecipe recipe, PoseStack transform, double mouseX, double mouseY)
	{
		transform.pushPose();
		transform.scale(3, 3, 1);
		this.getIcon().draw(transform, 5, 0);
		transform.popPose();
	}
}