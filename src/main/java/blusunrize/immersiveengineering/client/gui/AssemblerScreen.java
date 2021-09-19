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
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.metal.AssemblerTileEntity;
import blusunrize.immersiveengineering.common.gui.AssemblerContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class AssemblerScreen extends IEContainerScreen<AssemblerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("assembler");
	public AssemblerTileEntity tile;

	public AssemblerScreen(AssemblerContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.imageWidth = 230;
		this.imageHeight = 218;
	}

	@Override
	public void init()
	{
		super.init();
		IntConsumer sendButtonClick = id -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt("buttonID", id);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile.master(), tag));
		};
		for(int i = 0; i < 3; ++i)
		{
			final int id = i;
			this.addButton(new GuiButtonIE(leftPos+11+i*59, topPos+67, 10, 10, TextComponent.EMPTY, TEXTURE, 230, 50,
					btn -> sendButtonClick.accept(id))
					.setHoverOffset(0, 10));
		}
		this.addButton(new GuiButtonBoolean(leftPos+162, topPos+69, 16, 16, "", tile.recursiveIngredients, TEXTURE, 240, 66, 3,
				btn -> {
					sendButtonClick.accept(3);
					tile.recursiveIngredients = !tile.recursiveIngredients;
					fullInit();
				}));
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<Component> tooltip = new ArrayList<>();
		if(mx >= leftPos+187&&mx < leftPos+194&&my >= topPos+12&&my < topPos+59)
			tooltip.add(new TextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		GuiHelper.handleGuiTank(transform, tile.tanks[0], leftPos+204, topPos+13, 16, 46, 250, 0, 20, 50, mx, my, TEXTURE, tooltip);
		GuiHelper.handleGuiTank(transform, tile.tanks[1], leftPos+182, topPos+70, 16, 46, 250, 0, 20, 50, mx, my, TEXTURE, tooltip);
		GuiHelper.handleGuiTank(transform, tile.tanks[2], leftPos+204, topPos+70, 16, 46, 250, 0, 20, 50, mx, my, TEXTURE, tooltip);

		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
				if(mx >= leftPos+27+i*58&&mx < leftPos+43+i*58&&my >= topPos+64&&my < topPos+80)
				{
					tooltip.add(tile.patterns[i].inv.get(9).getHoverName());
					tile.patterns[i].inv.get(9).getItem().appendHoverText(tile.patterns[i].inv.get(9), ClientUtils.mc().level, tooltip, Default.NORMAL);
					for(int j = 0; j < tooltip.size(); j++)
						TextUtils.applyFormat(
								tooltip.get(j),
								j==0?tile.patterns[i].inv.get(9).getRarity().color: ChatFormatting.GRAY
						);
				}

		if(((mx >= leftPos+11&&mx < leftPos+21)||(mx >= leftPos+69&&mx < leftPos+79)||(mx >= leftPos+127&&mx < leftPos+137))&&my > topPos+67&&my < topPos+77)
			tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"assembler.clearRecipe"));
		if(mx >= leftPos+162&&mx < leftPos+178&&my > topPos+69&&my < topPos+85)
			tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"assembler."+(tile.recursiveIngredients?"recursiveIngredients": "nonRecursiveIngredients")));

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, leftPos+187, topPos+13+(46-stored), leftPos+194, topPos+59, 0xffb51500, 0xff600b00);

		GuiHelper.handleGuiTank(transform, tile.tanks[0], leftPos+204, topPos+13, 16, 46, 230, 0, 20, 50, mx, my, TEXTURE, null);
		GuiHelper.handleGuiTank(transform, tile.tanks[1], leftPos+182, topPos+70, 16, 46, 230, 0, 20, 50, mx, my, TEXTURE, null);
		GuiHelper.handleGuiTank(transform, tile.tanks[2], leftPos+204, topPos+70, 16, 46, 230, 0, 20, 50, mx, my, TEXTURE, null);

		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
			{
				ItemStack stack = tile.patterns[i].inv.get(9);
				transform.pushPose();
				Font font = null;
				if(!stack.isEmpty())
					font = stack.getItem().getFontRenderer(stack);
				if(font==null)
					font = this.font;
				itemRenderer.renderAndDecorateItem(stack, leftPos+27+i*58, topPos+64);
				itemRenderer.renderGuiItemDecorations(font, stack, leftPos+27+i*58, topPos+64, ChatFormatting.GRAY.toString()+stack.getCount());

				RenderSystem.disableDepthTest();
				fill(transform, leftPos+27+i*58, topPos+64, leftPos+27+i*74, topPos+80, 0x77444444);
				RenderSystem.enableDepthTest();

				transform.popPose();
				Lighting.turnOff();
			}
	}
}