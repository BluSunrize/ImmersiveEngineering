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
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.fmlclient.gui.GuiUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 05.07.2017
 */
public abstract class IEContainerScreen<C extends AbstractContainerMenu> extends AbstractContainerScreen<C>
{
	private final ResettableLazy<List<InfoArea>> infoAreas;
	protected final ResourceLocation background;

	public IEContainerScreen(C inventorySlotsIn, Inventory inv, Component title, ResourceLocation background)
	{
		super(inventorySlotsIn, inv, title);
		this.background = background;
		this.infoAreas = new ResettableLazy<>(this::makeInfoAreas);
	}

	@Override
	protected void init()
	{
		super.init();
		this.infoAreas.reset();
	}

	@Nonnull
	protected List<InfoArea> makeInfoAreas() {
		return ImmutableList.of();
	}

	@Override
	public void render(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		this.inventoryLabelY = this.imageHeight-94;
		this.renderBackground(transform);
		super.render(transform, mouseX, mouseY, partialTicks);
		List<Component> tooltip = new ArrayList<>();
		for (InfoArea area : infoAreas.get())
			area.fillTooltip(mouseX, mouseY, tooltip);
		for (GuiEventListener w : children())
			if (w.isMouseOver(mouseX, mouseY) && w instanceof ITooltipWidget ttw)
				ttw.gatherTooltip(mouseX, mouseY, tooltip);
		gatherAdditionalTooltips(
				mouseX, mouseY, tooltip::add, t -> tooltip.add(TextUtils.applyFormat(t, ChatFormatting.GRAY))
		);
		if (!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
		else
			this.renderTooltip(transform, mouseX, mouseY);
	}

	protected boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h)
	{
		return mouseX >= leftPos+x&&mouseY >= topPos+y
				&&mouseX < leftPos+x+w&&mouseY < topPos+y+h;
	}

	protected void clearIntArray(ContainerData ints)
	{
		// Clear GUI ints, the sync code assumes that 0 is the initial state
		for(int i = 0; i < ints.getCount(); ++i)
			ints.set(i, 0);
	}

	public void fullInit()
	{
		super.init(minecraft, width, height);
	}

	@Override
	protected final void renderBg(@Nonnull PoseStack transform, float partialTicks, int x, int y)
	{
		ClientUtils.bindTexture(background);
		drawBackgroundTexture(transform);
		drawContainerBackgroundPre(transform, partialTicks, x, y);
		for (InfoArea area : infoAreas.get())
			area.draw(transform);
	}

	protected void drawBackgroundTexture(PoseStack transform) {
		blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	protected void drawContainerBackgroundPre(@Nonnull PoseStack matrixStack, float partialTicks, int x, int y) {}

	protected void gatherAdditionalTooltips(
			int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray
	) {}

	public static ResourceLocation makeTextureLocation(String name)
	{
		return ImmersiveEngineering.rl("textures/gui/"+name+".png");
	}
}
