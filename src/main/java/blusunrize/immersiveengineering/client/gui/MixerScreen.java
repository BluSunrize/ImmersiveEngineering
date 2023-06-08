/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.MultitankArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import blusunrize.immersiveengineering.common.gui.MixerMenu;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class MixerScreen extends IEContainerScreen<MixerMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("mixer");

	public MixerScreen(MixerMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageHeight = 167;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new EnergyInfoArea(leftPos+158, topPos+22, menu.energy),
				new TooltipArea(
						new Rect2i(leftPos+106, topPos+61, 30, 16),
						() -> Component.translatable(Lib.GUI_CONFIG+"mixer.output"+(menu.outputAll.get()?"All": "Single"))
				),
				new MultitankArea(new Rect2i(leftPos+76, topPos+11, 58, 47), MixerLogic.TANK_VOLUME, menu.tankContents)
		);
	}

	@Override
	public void init()
	{
		super.init();
		this.clearWidgets();
		this.addRenderableWidget(new GuiButtonBoolean(
				leftPos+106, topPos+61, 30, 16, "", menu.outputAll::get, TEXTURE, 176, 82, 1,
				btn -> {
					CompoundTag tag = new CompoundTag();
					tag.putBoolean("outputAll", !menu.outputAll.get());
					sendUpdateToServer(tag);
				}
		));
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		graphics.pose().pushPose();
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

		for(final var slotProgress : menu.progress.get())
		{
			final int slot = slotProgress.slot();
			final int h = (int)Math.max(1, slotProgress.progress()*16);
			graphics.blit(TEXTURE, leftPos+24+slot%2*21, topPos+7+slot/2*18+(16-h), 176, 16-h, 2, h);
		}

		buffers.endBatch();
	}
}
