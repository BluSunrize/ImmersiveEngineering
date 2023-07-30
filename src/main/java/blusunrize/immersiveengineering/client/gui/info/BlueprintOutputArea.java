/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.IESlot.BlueprintOutput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class BlueprintOutputArea extends InfoArea
{
	private final IESlot.BlueprintOutput slot;

	public BlueprintOutputArea(BlueprintOutput slot, int guiLeft, int guiTop)
	{
		super(new Rect2i(guiLeft+slot.x, guiTop+slot.y, 16, 16));
		this.slot = slot;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		BlueprintCraftingRecipe recipe = slot.recipe;
		if(recipe.output.get().isEmpty()||slot.hasItem())
			return;
		tooltip.add(TextUtils.applyFormat(
				recipe.output.get().getHoverName().copy(), recipe.output.get().getRarity().color
		));
		List<ItemStack> inputs = new ArrayList<>();
		for(IngredientWithSize stack : recipe.inputs)
		{
			ItemStack toAdd = ItemHandlerHelper.copyStackWithSize(stack.getRandomizedExampleStack(mc().player.tickCount), stack.getCount());
			if(toAdd.isEmpty())
				continue;
			boolean isNew = true;
			for(ItemStack ss : inputs)
				if(ItemHandlerHelper.canItemStacksStack(ss, toAdd))
				{
					ss.grow(toAdd.getCount());
					isNew = false;
					break;
				}
			if(isNew)
				inputs.add(toAdd.copy());
		}
		for(ItemStack ss : inputs)
			tooltip.add(TextUtils.applyFormat(
					Component.literal(ss.getCount()+"x ").append(ss.getHoverName()),
					ChatFormatting.GRAY
			));
	}

	@Override
	public void draw(GuiGraphics graphics)
	{
		ItemStack ghostStack = slot.recipe.output.get();
		if(ghostStack.isEmpty()||slot.hasItem())
			return;
		graphics.renderItem(ghostStack, area.getX(), area.getY());
		graphics.fill(RenderType.guiGhostRecipeOverlay(), area.getX(), area.getY(), area.getX()+area.getWidth(), area.getY()+area.getHeight(), 0xbb333333);
	}
}
