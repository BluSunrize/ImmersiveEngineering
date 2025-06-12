/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Arrays;
import java.util.List;

public class BottlingMachineRecipeCategory extends IERecipeCategory<BottlingMachineRecipe>
{
	private final IDrawableStatic tankOverlay;

	public BottlingMachineRecipeCategory(IGuiHelper helper, RecipeType<RecipeHolder<BottlingMachineRecipe>> bottlingMachine)
	{
		super(helper, bottlingMachine, "block.immersiveengineering.bottling_machine");
		setBackground(helper.createBlankDrawable(120, 56));
		setIcon(helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, IEMultiblockLogic.BOTTLING_MACHINE.iconStack()));
		tankOverlay = helper.drawableBuilder(IEApi.ieLoc("textures/gui/fermenter.png"), 177, 31, 20, 51)
				.addPadding(-2, 2, -2, 2)
				.build();
	}

	public static BottlingMachineRecipeCategory getDefault(IGuiHelper helper)
	{
		return new BottlingMachineRecipeCategory(helper, JEIRecipeTypes.BOTTLING_MACHINE);
	}

	public static BottlingMachineRecipeCategory getPotions(IGuiHelper helper)
	{
		BottlingMachineRecipeCategory cat = new BottlingMachineRecipeCategory(helper, JEIRecipeTypes.BOTTLING_MACHINE_POTIONS);
		cat.title.append(Component.translatable(Lib.DESC+"jei.category.potions"));
		cat.setIcon(new DoubleIcon(
				cat.getIcon(),
				helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PotionContents.createItemStack(Items.POTION, Potions.HEALING)),
				0.5f
		));
		return cat;
	}

	public static BottlingMachineRecipeCategory getBuckets(IGuiHelper helper)
	{
		BottlingMachineRecipeCategory cat = new BottlingMachineRecipeCategory(helper, JEIRecipeTypes.BOTTLING_MACHINE_BUCKETS);
		cat.title.append(Component.translatable(Lib.DESC+"jei.category.buckets"));
		cat.setIcon(new DoubleIcon(
				cat.getIcon(),
				helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.WATER_BUCKET.getDefaultInstance()),
				0.5f
		));
		return cat;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BottlingMachineRecipe recipe, IFocusGroup focuses)
	{
		int inLength = recipe.inputs.size();
		int yStart = 29-Math.min(inLength, 3)*9;
		for(int i = 0; i < inLength; i++)
			builder.addSlot(RecipeIngredientRole.INPUT, 1, yStart+i*18)
					.addItemStacks(recipe.inputs.get(i).getMatchingStackList())
					.setBackground(JEIHelper.slotDrawable, -1, -1);

		List<ItemStack> outputs = recipe.output.get();
		yStart = 29-Math.min(outputs.size(), 3)*9;
		for(int i = 0; i < outputs.size(); i++)
			builder.addSlot(RecipeIngredientRole.OUTPUT, 101, yStart+i*18)
					.addItemStack(outputs.get(i))
					.setBackground(JEIHelper.slotDrawable, -1, -1);

		int tankSize = Math.max(FluidType.BUCKET_VOLUME, recipe.fluidInput.amount());
		builder.addSlot(RecipeIngredientRole.INPUT, 24, 2)
				.setFluidRenderer(tankSize, false, 16, 52)
				.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.fluidInput.getFluids()))
				.addRichTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public void draw(BottlingMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		GuiHelper.drawSlot(24, 20, 16, 52, graphics);

		graphics.pose().pushPose();
		graphics.pose().scale(3, 3, 1);
		if(this.getIcon() instanceof DoubleIcon di)
			di.main().draw(graphics, 14, 0);
		else
			this.getIcon().draw(graphics, 14, 0);
		graphics.pose().popPose();
	}
}