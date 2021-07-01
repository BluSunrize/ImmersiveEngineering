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
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterTileEntity;
import blusunrize.immersiveengineering.common.gui.SorterContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.List;

import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA;

public class SorterScreen extends IEContainerScreen<SorterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("sorter");

	private final SorterTileEntity tile;

	public SorterScreen(SorterContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		this.ySize = 244;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		for(int side = 0; side < 6; side++)
		{
			int x = guiLeft+30+(side/2)*58;
			int y = guiTop+44+(side%2)*76;
			String s = I18n.format("desc.immersiveengineering.info.blockSide."+Direction.byIndex(side).toString()).substring(0, 1);
			RenderSystem.enableBlend();
			ClientUtils.font().drawStringWithShadow(transform, s, x-(ClientUtils.font().getStringWidth(s)/2), y, 0xaacccccc);
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
				int x = guiLeft+3+(side/2)*58+bit*18;
				int y = guiTop+3+(side%2)*76;
				final int bitFinal = bit;
				final int sideFinal = side;
				ButtonSorter b = new ButtonSorter(x, y, bit, btn -> {
					int mask = (1<<bitFinal);
					tile.sideFilter[sideFinal] = tile.sideFilter[sideFinal]^mask;

					CompoundNBT tag = new CompoundNBT();
					tag.putIntArray("sideConfig", tile.sideFilter);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
					fullInit();
				});
				b.active = bit==0?this.tile.doOredict(side): bit==1?this.tile.doNBT(side): this.tile.doFuzzy(side);
				this.addButton(b);
			}
	}

	public static class ButtonSorter extends Button implements ITooltipWidget
	{
		int type;
		boolean active = false;

		public ButtonSorter(int x, int y, int type, IPressable handler)
		{
			super(x, y, 18, 18, StringTextComponent.EMPTY, handler);
			this.type = type;
		}

		@Override
		public void render(MatrixStack transform, int mx, int my, float partialTicks)
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

		@Override
		public void gatherTooltip(int mouseX, int mouseY, List<ITextComponent> tooltip)
		{
			String[] split = I18n.format(Lib.DESC_INFO+"filter."+(type==0?"tag": type==1?"nbt": "damage")).split("<br>");
			for(int i = 0; i < split.length; i++)
				if (i == 0)
					tooltip.add(new StringTextComponent(split[i]));
				else
					tooltip.add(TextUtils.applyFormat(new StringTextComponent(split[i]), TextFormatting.GRAY));
		}
	}
}
