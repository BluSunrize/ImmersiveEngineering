/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ModWorkbenchScreen extends ToolModificationScreen<ModWorkbenchContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("workbench");

	private final ModWorkbenchTileEntity workbench;

	public ModWorkbenchScreen(ModWorkbenchContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		workbench = container.tile;
		this.imageHeight = 168;
	}

	@Override
	protected void sendMessage(CompoundTag data)
	{
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this.workbench, data));
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		for(int i = 0; i < menu.slotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput&&!s.hasItem())
			{
				BlueprintCraftingRecipe recipe = ((IESlot.BlueprintOutput)s).recipe;
				if(recipe!=null&&!recipe.output.isEmpty())
					if(isHovering(s.x, s.y, 16, 16, mx, my))
					{
						List<Component> tooltip = new ArrayList<>();
						tooltip.add(TextUtils.applyFormat(
								recipe.output.getHoverName().copy(),
								recipe.output.getRarity().color
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
									new TextComponent(ss.getCount()+"x ").append(ss.getHoverName()),
									ChatFormatting.GRAY
							));

						GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
						Lighting.turnBackOn();
					}
			}
		}
	}


	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);


		for(int i = 0; i < menu.slotCount; i++)
		{
			Slot s = menu.getSlot(i);
			GuiHelper.drawSlot(
					transform, leftPos+s.x, topPos+s.y, 16, 16, 0x77222222, 0x77444444, 0x77999999
			);
		}

		ItemRenderer itemRender = mc().getItemRenderer();
		for(int i = 0; i < menu.slotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput&&!s.hasItem())
			{
				ItemStack ghostStack = ((IESlot.BlueprintOutput)s).recipe.output;
				if(!ghostStack.isEmpty())
				{
					itemRender.renderAndDecorateItem(ghostStack, leftPos+s.x, topPos+s.y);
					RenderSystem.depthFunc(GL11.GL_GREATER);
					fill(transform, leftPos+s.x, topPos+s.y, leftPos+s.x+16, topPos+s.y+16, 0xbb333333);
					RenderSystem.depthFunc(GL11.GL_LEQUAL);
				}
			}
		}
	}
}