/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.common.blocks.metal.AssemblerTileEntity;
import blusunrize.immersiveengineering.common.gui.AssemblerContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class AssemblerScreen extends IEContainerScreen<AssemblerContainer>
{
	static final String texture = "immersiveengineering:textures/gui/assembler.png";
	public AssemblerTileEntity tile;

	public AssemblerScreen(AssemblerContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.xSize = 230;
		this.ySize = 218;
	}

	@Override
	public void init()
	{
		super.init();
		IntConsumer sendButtonClick = id -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt("buttonID", id);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile.master(), tag));
		};
		for(int i = 0; i < 3; ++i)
		{
			final int id = i;
			this.addButton(new GuiButtonIE(guiLeft+11+i*59, guiTop+67, 10, 10, StringTextComponent.EMPTY, texture, 230, 50,
					btn -> sendButtonClick.accept(id))
					.setHoverOffset(0, 10));
		}
		this.addButton(new GuiButtonBoolean(guiLeft+162, guiTop+69, 16, 16, "", tile.recursiveIngredients, texture, 240, 66, 3,
				btn -> {
					sendButtonClick.accept(3);
					tile.recursiveIngredients = !tile.recursiveIngredients;
					fullInit();
				}));
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();
		if(mx >= guiLeft+187&&mx < guiLeft+194&&my >= guiTop+12&&my < guiTop+59)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		ClientUtils.handleGuiTank(transform, tile.tanks[0], guiLeft+204, guiTop+13, 16, 46, 250, 0, 20, 50, mx, my, texture, tooltip);
		ClientUtils.handleGuiTank(transform, tile.tanks[1], guiLeft+182, guiTop+70, 16, 46, 250, 0, 20, 50, mx, my, texture, tooltip);
		ClientUtils.handleGuiTank(transform, tile.tanks[2], guiLeft+204, guiTop+70, 16, 46, 250, 0, 20, 50, mx, my, texture, tooltip);

		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
				if(mx >= guiLeft+27+i*58&&mx < guiLeft+43+i*58&&my >= guiTop+64&&my < guiTop+80)
				{
					tooltip.add(tile.patterns[i].inv.get(9).getDisplayName());
					tile.patterns[i].inv.get(9).getItem().addInformation(tile.patterns[i].inv.get(9), ClientUtils.mc().world, tooltip, TooltipFlags.NORMAL);
					for(int j = 0; j < tooltip.size(); j++)
						ClientUtils.applyFormat(
								tooltip.get(j),
								j==0?tile.patterns[i].inv.get(9).getRarity().color: TextFormatting.GRAY
						);
				}

		if(((mx >= guiLeft+11&&mx < guiLeft+21)||(mx >= guiLeft+69&&mx < guiLeft+79)||(mx >= guiLeft+127&&mx < guiLeft+137))&&my > guiTop+67&&my < guiTop+77)
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"assembler.clearRecipe"));
		if(mx >= guiLeft+162&&mx < guiLeft+178&&my > guiTop+69&&my < guiTop+85)
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"assembler."+(tile.recursiveIngredients?"recursiveIngredients": "nonRecursiveIngredients")));

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(texture);
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+187, guiTop+13+(46-stored), guiLeft+194, guiTop+59, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(transform, tile.tanks[0], guiLeft+204, guiTop+13, 16, 46, 230, 0, 20, 50, mx, my, texture, null);
		ClientUtils.handleGuiTank(transform, tile.tanks[1], guiLeft+182, guiTop+70, 16, 46, 230, 0, 20, 50, mx, my, texture, null);
		ClientUtils.handleGuiTank(transform, tile.tanks[2], guiLeft+204, guiTop+70, 16, 46, 230, 0, 20, 50, mx, my, texture, null);

		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
			{
				ItemStack stack = tile.patterns[i].inv.get(9);
				transform.push();
				FontRenderer font = null;
				if(!stack.isEmpty())
					font = stack.getItem().getFontRenderer(stack);
				if(font==null)
					font = this.font;
				itemRenderer.renderItemAndEffectIntoGUI(stack, guiLeft+27+i*58, guiTop+64);
				itemRenderer.renderItemOverlayIntoGUI(font, stack, guiLeft+27+i*58, guiTop+64, TextFormatting.GRAY.toString()+stack.getCount());

				RenderSystem.disableDepthTest();
				ClientUtils.drawColouredRect(guiLeft+27+i*58, guiTop+64, 16, 16, 0x77444444);
				RenderSystem.enableDepthTest();

				transform.pop();
				RenderHelper.disableStandardItemLighting();
			}
	}
}