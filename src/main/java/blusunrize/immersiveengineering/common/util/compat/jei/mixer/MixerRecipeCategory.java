/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.mixer;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.DoubleIcon;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Arrays;

public class MixerRecipeCategory extends IERecipeCategory<MixerRecipe>
{
	private final IDrawableStatic tankTexture;
	private final IDrawableStatic tankOverlay;
	private final IDrawableStatic arrowDrawable;

	public MixerRecipeCategory(IGuiHelper helper, RecipeType<RecipeHolder<MixerRecipe>> recipeType)
	{
		super(helper, recipeType, "block.immersiveengineering.mixer");
		setBackground(helper.createBlankDrawable(155, 60));
		setIcon(IEMultiblockLogic.MIXER.iconStack());
		ResourceLocation background = IEApi.ieLoc("textures/gui/mixer.png");
		tankTexture = helper.createDrawable(background, 68, 8, 74, 60);
		tankOverlay = helper.drawableBuilder(background, 177, 31, 20, 51).addPadding(-2, 2, -2, 2).build();
		arrowDrawable = helper.createDrawable(background, 178, 17, 18, 13);
	}

	public static MixerRecipeCategory getDefault(IGuiHelper helper)
	{
		return new MixerRecipeCategory(helper, JEIRecipeTypes.MIXER);
	}

	public static MixerRecipeCategory getPotions(IGuiHelper helper)
	{
		MixerRecipeCategory cat = new MixerRecipeCategory(helper, JEIRecipeTypes.MIXER_POTIONS);
		cat.title.append(Component.translatable(Lib.DESC+"jei.category.potions"));
		cat.setIcon(new DoubleIcon(
				cat.getIcon(),
				helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PotionContents.createItemStack(Items.POTION, Potions.HEALING)),
				0.5f
		));
		return cat;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MixerRecipe recipe, IFocusGroup focuses)
	{
		int tankSize = Math.max(2*FluidType.BUCKET_VOLUME, Math.max(recipe.fluidInput.amount(), recipe.fluidOutput.getAmount()));
		builder.addSlot(RecipeIngredientRole.INPUT, 48, 3)
				.setFluidRenderer(tankSize, false, 58, 47)
				.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.fluidInput.getFluids()))
				.addRichTooltipCallback(JEIHelper.fluidTooltipCallback);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 3)
				.setFluidRenderer(tankSize, false, 16, 47)
				.setOverlay(tankOverlay, 0, 0)
				.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.fluidOutput)
				.addRichTooltipCallback(JEIHelper.fluidTooltipCallback);

		for(int i = 0; i < recipe.itemInputs.size(); i++)
		{
			int x = (i%2)*18+1;
			int y = i/2*18+1;
			builder.addSlot(RecipeIngredientRole.INPUT, x, y)
					.addItemStacks(Arrays.asList(recipe.itemInputs.get(i).getMatchingStacks()))
					.setBackground(JEIHelper.slotDrawable, -1, -1);
		}
	}

	@Override
	public void draw(MixerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		tankTexture.draw(graphics, 40, 0);
		arrowDrawable.draw(graphics, 117, 19);
		GuiHelper.drawSlot(139, 18, 16, 47, graphics);
	}

}