/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.mixer;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
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

public class MixerRecipeCategory extends IERecipeCategory<MixerRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "mixer");
	private final IDrawableStatic tankTexture;
	private final IDrawableStatic tankOverlay;
	private final IDrawableStatic arrowDrawable;

	public MixerRecipeCategory(IGuiHelper helper)
	{
		super(MixerRecipe.class, helper, UID, "block.immersiveengineering.mixer");
		setBackground(helper.createBlankDrawable(155, 60));
		setIcon(new ItemStack(IEBlocks.Multiblocks.mixer));
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/mixer.png");
		tankTexture = helper.createDrawable(background, 68, 8, 74, 60);
		tankOverlay = helper.drawableBuilder(background, 177, 31, 20, 51).addPadding(-2, 2, -2, 2).build();
		arrowDrawable = helper.createDrawable(background, 178, 17, 18, 13);
	}

	@Override
	public void setIngredients(MixerRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputs(VanillaTypes.FLUID, recipe.fluidInput.getMatchingFluidStacks());
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.itemInputs).build());
		ingredients.setOutput(VanillaTypes.FLUID, recipe.fluidOutput);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MixerRecipe recipe, IIngredients ingredients)
	{
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 48, 3, 58, 47, 4*FluidAttributes.BUCKET_VOLUME, false, null);
		guiFluidStacks.set(0, recipe.fluidInput.getMatchingFluidStacks());

		guiFluidStacks.init(1, false, 138, 2, 16, 47, 4*FluidAttributes.BUCKET_VOLUME, false, tankOverlay);
		guiFluidStacks.set(1, recipe.fluidOutput);
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		for(int i = 0; i < recipe.itemInputs.length; i++)
		{
			int x = (i%2)*18;
			int y = i/2*18;
			guiItemStacks.init(i, true, x, y);
			guiItemStacks.set(i, Arrays.asList(recipe.itemInputs[i].getMatchingStacks()));
			guiItemStacks.setBackground(i, JEIHelper.slotDrawable);
		}
	}

	@Override
	public void draw(MixerRecipe recipe, PoseStack transform, double mouseX, double mouseY)
	{
		tankTexture.draw(transform, 40, 0);
		arrowDrawable.draw(transform, 117, 19);
		GuiHelper.drawSlot(138, 17, 16, 47, transform);
	}

}