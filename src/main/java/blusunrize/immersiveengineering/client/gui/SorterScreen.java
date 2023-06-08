/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.common.gui.SorterMenu;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;

import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA;

public class SorterScreen extends IEContainerScreen<SorterMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("sorter");

	public SorterScreen(SorterMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageHeight = 244;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+30+(side/2)*58;
			int y = topPos+44+(side%2)*76;
			String s = I18n.get("desc.immersiveengineering.info.blockSide."+Direction.from3DDataValue(side)).substring(0, 1);
			RenderSystem.enableBlend();
			graphics.drawString(ClientUtils.font(), s, x-(ClientUtils.font().width(s)/2), y, 0xaacccccc, true);
		}
	}

	@Override
	public void init()
	{
		super.init();
		this.clearWidgets();
		for(int side = 0; side < 6; side++)
			for(final FilterBit bit : FilterBit.values())
			{
				int x = leftPos+3+(side/2)*58+bit.ordinal()*18;
				int y = topPos+3+(side%2)*76;
				final int sideFinal = side;
				final GetterAndSetter<Integer> value = menu.filterMasks.get(side);
				ButtonSorter b = new ButtonSorter(x, y, bit, value::get, btn -> {
					CompoundTag tag = new CompoundTag();
					tag.putInt("sideConfigVal", value.get()^bit.mask());
					tag.putInt("sideConfigId", sideFinal);
					sendUpdateToServer(tag);
					fullInit();
				});
				this.addRenderableWidget(b);
			}
	}

	public static class ButtonSorter extends Button implements ITooltipWidget
	{
		private final FilterBit type;
		private final IntSupplier state;

		public ButtonSorter(int x, int y, FilterBit type, IntSupplier state, OnPress handler)
		{
			super(x, y, 18, 18, Component.empty(), handler, DEFAULT_NARRATION);
			this.type = type;
			this.state = state;
		}

		@Override
		public void render(GuiGraphics graphics, int mx, int my, float partialTicks)
		{
			if(this.visible)
			{
				ClientUtils.bindTexture(TEXTURE);
				isHovered = mx >= this.getX()&&my >= this.getY()&&mx < this.getX()+this.width&&my < this.getY()+this.height;
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO);
				final boolean active = (state.getAsInt()&type.mask())!=0;
				graphics.blit(TEXTURE, this.getX(), this.getY(), 176+type.ordinal()*18, (active?3: 21), this.width, this.height);
			}
		}

		@Override
		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			String[] split = I18n.get(type.getTranslationKey()).split("<br>");
			for(int i = 0; i < split.length; i++)
				if(i==0)
					tooltip.add(Component.literal(split[i]));
				else
					tooltip.add(TextUtils.applyFormat(Component.literal(split[i]), ChatFormatting.GRAY));
		}
	}

	public enum FilterBit
	{
		TAG, NBT, DAMAGE;

		public String getTranslationKey()
		{
			return Lib.DESC_INFO+"filter."+name().toLowerCase(Locale.ROOT);
		}

		public int mask()
		{
			return 1<<ordinal();
		}
	}
}
