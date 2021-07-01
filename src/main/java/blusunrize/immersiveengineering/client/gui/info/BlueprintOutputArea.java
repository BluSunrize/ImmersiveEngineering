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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class BlueprintOutputArea extends InfoArea
{
	private final IESlot.BlueprintOutput slot;

	public BlueprintOutputArea(BlueprintOutput slot, int guiLeft, int guiTop)
	{
		super(new Rectangle2d(guiLeft+slot.xPos, guiTop+slot.yPos, 16, 16));
		this.slot = slot;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<ITextComponent> tooltip)
	{
		BlueprintCraftingRecipe recipe = slot.recipe;
		if(recipe.output.isEmpty()|| slot.getHasStack())
			return;
		tooltip.add(TextUtils.applyFormat(
				recipe.output.getDisplayName().deepCopy(), recipe.output.getRarity().color
		));
		List<ItemStack> inputs = new ArrayList<>();
		for(IngredientWithSize stack : recipe.inputs)
		{
			ItemStack toAdd = ItemHandlerHelper.copyStackWithSize(stack.getRandomizedExampleStack(mc().player.ticksExisted), stack.getCount());
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
					new StringTextComponent(ss.getCount()+"x ").appendSibling(ss.getDisplayName()),
					TextFormatting.GRAY
			));
	}

	@Override
	public void draw(MatrixStack transform)
	{
		ItemStack ghostStack = slot.recipe.output;
		if(ghostStack.isEmpty()||slot.getHasStack())
			return;
		Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(ghostStack, area.getX(), area.getY());
		RenderSystem.depthFunc(GL11.GL_GREATER);
		fill(transform, area.getX(), area.getY(), area.getX()+area.getWidth(), area.getY()+area.getHeight(), 0xbb333333);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
	}
}
