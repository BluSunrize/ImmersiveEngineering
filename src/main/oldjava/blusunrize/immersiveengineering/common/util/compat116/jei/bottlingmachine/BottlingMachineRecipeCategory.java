/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;

import java.util.Arrays;

public class BottlingMachineRecipeCategory extends IERecipeCategory<BottlingMachineRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "bottlingmachine");
	private final IDrawableStatic tankOverlay;

	public BottlingMachineRecipeCategory(IGuiHelper helper)
	{
		super(BottlingMachineRecipe.class, helper, UID, "block.immersiveengineering.bottling_machine");
		setBackground(helper.createBlankDrawable(120, 50));
		setIcon(helper.createDrawableIngredient(new ItemStack(IEBlocks.Multiblocks.bottlingMachine)));
		tankOverlay = helper.drawableBuilder(new ResourceLocation(Lib.MODID, "textures/gui/fermenter.png"), 177, 31, 20, 51).addPadding(-2, 2, -2, 2).build();
	}

	@Override
	public void setIngredients(BottlingMachineRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
		ingredients.setInputs(VanillaTypes.FLUID, recipe.fluidInput.getMatchingFluidStacks());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BottlingMachineRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 12);
		guiItemStacks.init(1, false, 100, 12);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getItems()));
		guiItemStacks.set(1, recipe.output);
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);
		guiItemStacks.setBackground(1, JEIHelper.slotDrawable);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 75, 0, 16, 47, 4*FluidAttributes.BUCKET_VOLUME, false, tankOverlay);
		guiFluidStacks.set(0, recipe.fluidInput.getMatchingFluidStacks());
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public void draw(BottlingMachineRecipe recipe, PoseStack transform, double mouseX, double mouseY)
	{
		GuiHelper.drawSlot(75, 15, 16, 48, transform);

		transform.pushPose();
		transform.scale(3, 3, 1);
		this.getIcon().draw(transform, 8, 0);
		transform.popPose();
	}
}