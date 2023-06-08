/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AssemblerLogic;
import blusunrize.immersiveengineering.common.gui.AssemblerMenu;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions.FontContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.IntConsumer;

public class AssemblerScreen extends IEContainerScreen<AssemblerMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("assembler");
	private GuiButtonBoolean recursiveButton;

	public AssemblerScreen(AssemblerMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageWidth = 230;
		this.imageHeight = 218;
		this.inventoryLabelY = 127;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = Lists.newArrayList(
				new FluidInfoArea(menu.tanks[0], new Rect2i(leftPos+204, topPos+13, 16, 46), 230, 0, 20, 50, TEXTURE),
				new FluidInfoArea(menu.tanks[1], new Rect2i(leftPos+182, topPos+70, 16, 46), 230, 0, 20, 50, TEXTURE),
				new FluidInfoArea(menu.tanks[2], new Rect2i(leftPos+204, topPos+70, 16, 46), 230, 0, 20, 50, TEXTURE),
				new EnergyInfoArea(leftPos+187, topPos+13, menu.energy),
				new TooltipArea(
						new Rect2i(leftPos+162, topPos+69, 16, 16),
						() -> Component.translatable(Lib.GUI_CONFIG+"assembler."+(menu.recursiveIngredients.get()?"recursiveIngredients": "nonRecursiveIngredients"))
				)
		);
		for(int i = 0; i < AssemblerLogic.NUM_PATTERNS; i++)
		{
			final int offset = 58*i;
			areas.add(new TooltipArea(
					new Rect2i(leftPos+11+offset, topPos+67, 10, 10),
					Component.translatable(Lib.GUI_CONFIG+"assembler.clearRecipe")
			));
			int finalI = i;
			areas.add(new TooltipArea(
					new Rect2i(leftPos+27+offset, topPos+64, 16, 16), l -> addRecipeOutputTooltip(finalI, l)
			));
		}
		return areas;
	}

	private void addRecipeOutputTooltip(int i, List<Component> tooltip)
	{
		ItemStack recipeOutput = menu.patterns.get(i).getStackInSlot(9);
		if(menu.inv.getStackInSlot(18+i).isEmpty()&&!recipeOutput.isEmpty())
		{
			tooltip.add(menu.patterns.get(i).getStackInSlot(9).getHoverName());
			recipeOutput.getItem().appendHoverText(recipeOutput, ClientUtils.mc().level, tooltip, Default.NORMAL);
		}
	}

	@Override
	public void init()
	{
		super.init();
		IntConsumer sendButtonClick = id -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt("buttonID", id);
			sendUpdateToServer(tag);
		};
		for(int i = 0; i < 3; ++i)
		{
			final int id = i;
			this.addRenderableWidget(new GuiButtonIE(leftPos+11+i*59, topPos+67, 10, 10, Component.empty(), TEXTURE, 230, 50,
					btn -> sendButtonClick.accept(id))
					.setHoverOffset(0, 10));
		}
		this.recursiveButton = new GuiButtonBoolean(leftPos+162, topPos+69, 16, 16, "", menu.recursiveIngredients::get, TEXTURE, 240, 66, 3,
				btn -> {
					sendButtonClick.accept(3);
					fullInit();
				});
		this.addRenderableWidget(recursiveButton);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(int i = 0; i < AssemblerLogic.NUM_PATTERNS; i++)
			if(menu.inv.getStackInSlot(18+i).isEmpty()&&!menu.patterns.get(i).getStackInSlot(9).isEmpty())
			{
				ItemStack stack = menu.patterns.get(i).getStackInSlot(9);
				Font font = null;
				if(!stack.isEmpty())
					font = IClientItemExtensions.of(stack.getItem()).getFont(stack, FontContext.ITEM_COUNT);
				if(font==null)
					font = this.font;
				graphics.renderItem(stack, leftPos+27+i*58, topPos+64);
				graphics.renderItemDecorations(font, stack, leftPos+27+i*58, topPos+64, ChatFormatting.GRAY.toString()+stack.getCount());

				RenderSystem.disableDepthTest();
				graphics.fill(leftPos+27+i*58, topPos+64, leftPos+27+i*74, topPos+80, 0x77444444);
				RenderSystem.enableDepthTest();
			}
	}
}