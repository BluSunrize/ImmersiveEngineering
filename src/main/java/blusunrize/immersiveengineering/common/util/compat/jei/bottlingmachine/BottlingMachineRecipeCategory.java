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
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;

import java.util.Arrays;
import java.util.List;

public class BottlingMachineRecipeCategory extends IERecipeCategory<BottlingMachineRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "bottlingmachine");
	private final IDrawableStatic tankOverlay;

	public BottlingMachineRecipeCategory(IGuiHelper helper)
	{
		super(BottlingMachineRecipe.class, helper, UID, "block.immersiveengineering.bottling_machine");
		setBackground(helper.createBlankDrawable(120, 50));
		setIcon(helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(IEBlocks.Multiblocks.BOTTLING_MACHINE)));
		tankOverlay = helper.drawableBuilder(new ResourceLocation(Lib.MODID, "textures/gui/fermenter.png"), 177, 31, 20, 51).addPadding(-2, 2, -2, 2).build();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BottlingMachineRecipe recipe, List<? extends IFocus<?>> focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 13)
				.addItemStacks(Arrays.asList(recipe.input.getItems()))
				.setBackground(JEIHelper.slotDrawable, -1, -1);
		builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 13)
				.addItemStack(recipe.output.get())
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		builder.addSlot(RecipeIngredientRole.INPUT, 76, 1)
				.setFluidRenderer(4*FluidAttributes.BUCKET_VOLUME, false, 16, 47)
				.addIngredients(VanillaTypes.FLUID, recipe.fluidInput.getMatchingFluidStacks())
				.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public void draw(BottlingMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack transform, double mouseX, double mouseY)
	{
		GuiHelper.drawSlot(75, 15, 16, 48, transform);

		transform.pushPose();
		transform.scale(3, 3, 1);
		this.getIcon().draw(transform, 8, 0);
		transform.popPose();
	}
}