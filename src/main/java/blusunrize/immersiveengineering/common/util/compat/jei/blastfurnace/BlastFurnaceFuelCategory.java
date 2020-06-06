/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class BlastFurnaceFuelCategory extends IERecipeCategory<BlastFurnaceFuel>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "blastfurnace_fuel");
	private final IDrawableAnimated flame;

	public BlastFurnaceFuelCategory(IGuiHelper helper)
	{
		super(BlastFurnaceFuel.class, helper, UID, "gui.immersiveengineering.blastFurnace.fuel");
		ResourceLocation furnaceBackgroundLocation = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		setBackground(helper.drawableBuilder(furnaceBackgroundLocation, 55, 36, 18, 36).addPadding(0, 0, 0, 68).build());
		setIcon(helper.createDrawable(new ResourceLocation(Lib.MODID, "textures/gui/blast_furnace.png"), 176, 0, 14, 14));

		IDrawableStatic flameStatic = helper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
		this.flame = helper.createAnimatedDrawable(flameStatic, 20*4, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public void setIngredients(BlastFurnaceFuel recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlastFurnaceFuel recipe, IIngredients iIngredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 16);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));
	}

	@Override
	public void draw(BlastFurnaceFuel recipe, double mouseX, double mouseY)
	{
		this.flame.draw(1, 0);
		String burnTime = I18n.format("desc.immersiveengineering.info.seconds", Utils.formatDouble(recipe.burnTime/20, "#.##"));
		ClientUtils.font().drawString(burnTime, 24, 12, 0x777777);
	}
}