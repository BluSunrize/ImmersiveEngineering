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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.ButtonTexture;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterBlockEntity.FilterConfig;
import blusunrize.immersiveengineering.common.gui.SorterMenu;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

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
		for(Direction side : Direction.values())
			for(final FilterBit bit : FilterBit.values())
			{
				int sideId = side.ordinal();
				int x = leftPos+3+(sideId/2)*58+bit.ordinal()*18;
				int y = topPos+3+(sideId%2)*76;
				final int sideFinal = sideId;
				final GetterAndSetter<FilterConfig> value = menu.filterMasks.get(side);
				ButtonSorter b = new ButtonSorter(x, y, bit, value::get, btn -> {
					CompoundTag tag = new CompoundTag();
					tag.put("sideConfigVal", FilterConfig.CODEC.toNBT(bit.toggle(value.get())));
					tag.putInt("sideConfigId", sideFinal);
					sendUpdateToServer(tag);
					fullInit();
				});
				this.addRenderableWidget(b);
			}
	}

	// TODO replace by GuiButtonBoolean
	public static class ButtonSorter extends GuiButtonBoolean implements ITooltipWidget
	{
		private static final Map<FilterBit, ButtonTexture> TRUE_TEXTURES = Map.of(
				FilterBit.DAMAGE, new ButtonTexture(ieLoc("sorter/damage")),
				FilterBit.NBT, new ButtonTexture(ieLoc("sorter/components")),
				FilterBit.TAG, new ButtonTexture(ieLoc("sorter/tags"))
		);
		private static final Map<FilterBit, ButtonTexture> FALSE_TEXTURES = Map.of(
				FilterBit.DAMAGE, new ButtonTexture(ieLoc("sorter/no_damage")),
				FilterBit.NBT, new ButtonTexture(ieLoc("sorter/no_components")),
				FilterBit.TAG, new ButtonTexture(ieLoc("sorter/no_tags"))
		);

		private final FilterBit type;
		private final Supplier<FilterConfig> state;

		public ButtonSorter(int x, int y, FilterBit type, Supplier<FilterConfig> state, IIEPressable<GuiButtonState<Boolean>> handler)
		{
			super(
					x, y, 18, 18, Component.empty(), () -> type.get(state.get()),
					FALSE_TEXTURES.get(type), TRUE_TEXTURES.get(type), handler
			);
			this.type = type;
			this.state = state;
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

		public boolean get(FilterConfig config)
		{
			return switch(this)
			{
				case TAG -> config.allowTags();
				case NBT -> config.considerComponents();
				case DAMAGE -> config.ignoreDamage();
			};
		}

		public FilterConfig toggle(FilterConfig config)
		{
			return switch(this)
			{
				case TAG -> new FilterConfig(!config.allowTags(), config.considerComponents(), config.ignoreDamage());
				case NBT -> new FilterConfig(config.allowTags(), !config.considerComponents(), config.ignoreDamage());
				case DAMAGE ->
						new FilterConfig(config.allowTags(), config.considerComponents(), !config.ignoreDamage());
			};
		}
	}
}
