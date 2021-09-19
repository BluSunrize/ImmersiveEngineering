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
import blusunrize.immersiveengineering.common.blocks.wooden.SorterTileEntity;
import blusunrize.immersiveengineering.common.gui.SorterContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA;

public class SorterScreen extends IEContainerScreen<SorterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("sorter");

	private final SorterTileEntity tile;

	public SorterScreen(SorterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.imageHeight = 244;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		for(AbstractWidget button : this.buttons)
		{
			if(button instanceof ButtonSorter)
				if(mx > button.x&&mx < button.x+18&&my > button.y&&my < button.y+18)
				{
					List<Component> tooltip = new ArrayList<>();
					int type = ((ButtonSorter)button).type;
					String[] split = I18n.get(Lib.DESC_INFO+"filter."+(type==0?"tag": type==1?"nbt": "damage")).split("<br>");
					for(int i = 0; i < split.length; i++)
						tooltip.add(TextUtils.applyFormat(
								new TextComponent(split[i]), i==0?ChatFormatting.WHITE: ChatFormatting.GRAY
						));
					GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
				}
		}
	}


	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+30+(side/2)*58;
			int y = topPos+44+(side%2)*76;
			String s = I18n.get("desc.immersiveengineering.info.blockSide."+Direction.from3DDataValue(side).toString()).substring(0, 1);
			RenderSystem.enableBlend();
			ClientUtils.font().drawShadow(transform, s, x-(ClientUtils.font().width(s)/2), y, 0xaacccccc);
		}
	}

	@Override
	public void init()
	{
		super.init();
		this.buttons.clear();
		for(int side = 0; side < 6; side++)
			for(int bit = 0; bit < 3; bit++)
			{
				int x = leftPos+3+(side/2)*58+bit*18;
				int y = topPos+3+(side%2)*76;
				final int bitFinal = bit;
				final int sideFinal = side;
				ButtonSorter b = new ButtonSorter(x, y, bit, btn -> {
					int mask = (1<<bitFinal);
					tile.sideFilter[sideFinal] = tile.sideFilter[sideFinal]^mask;

					CompoundTag tag = new CompoundTag();
					tag.putIntArray("sideConfig", tile.sideFilter);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
					fullInit();
				});
				b.active = bit==0?this.tile.doOredict(side): bit==1?this.tile.doNBT(side): this.tile.doFuzzy(side);
				this.addButton(b);
			}
	}

	public static class ButtonSorter extends Button
	{
		int type;
		boolean active = false;

		public ButtonSorter(int x, int y, int type, OnPress handler)
		{
			super(x, y, 18, 18, TextComponent.EMPTY, handler);
			this.type = type;
		}

		@Override
		public void render(PoseStack transform, int mx, int my, float partialTicks)
		{
			if(this.visible)
			{
				ClientUtils.bindTexture(TEXTURE);
				isHovered = mx >= this.x&&my >= this.y&&mx < this.x+this.width&&my < this.y+this.height;
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO);
				this.blit(transform, this.x, this.y, 176+type*18, (active?3: 21), this.width, this.height);
			}
		}
	}
}
