/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cokeoven;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.Arrays;

public class CokeOvenRecipeCategory extends IERecipeCategory<CokeOvenRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "cokeoven");
	private final IDrawableStatic tankOverlay;
	private final IDrawableAnimated flame;

	public CokeOvenRecipeCategory(IGuiHelper helper)
	{
		super(CokeOvenRecipe.class, helper, UID, "block.immersiveengineering.coke_oven");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/coke_oven.png");
		setBackground(helper.createDrawable(background, 26, 16, 123, 55));
		setIcon(new ItemStack(IEBlocks.Multiblocks.COKE_OVEN.get()));
		tankOverlay = helper.createDrawable(background, 178, 33, 16, 47);
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(500, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CokeOvenRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 4, 19)
				.addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()));

		IRecipeSlotBuilder outputSlotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 19);
		if(!recipe.output.get().isEmpty())
			outputSlotBuilder.addItemStack(recipe.output.get());

		if(recipe.creosoteOutput > 0)
			builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 4)
					.setFluidRenderer(5*FluidType.BUCKET_VOLUME, false, 16, 47)
					.setOverlay(tankOverlay, 0, 0)
					.addIngredient(VanillaTypes.FLUID, new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput))
					.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public void draw(CokeOvenRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY)
	{
		flame.draw(stack, 31, 20);
	}
}