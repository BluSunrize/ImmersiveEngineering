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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IIntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 05.07.2017
 */
public abstract class IEContainerScreen<C extends Container> extends ContainerScreen<C>
{
	private final ResettableLazy<List<InfoArea>> infoAreas;
	protected final ResourceLocation background;

	public IEContainerScreen(C inventorySlotsIn, PlayerInventory inv, ITextComponent title, ResourceLocation background)
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
	public void render(@Nonnull MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		this.playerInventoryTitleY = this.ySize-94;
		this.renderBackground(transform);
		super.render(transform, mouseX, mouseY, partialTicks);
		List<ITextComponent> tooltip = new ArrayList<>();
		for (InfoArea area : infoAreas.get())
			area.fillTooltip(mouseX, mouseY, tooltip);
		for (Widget w : buttons)
			if (w.isMouseOver(mouseX, mouseY) && w instanceof ITooltipWidget)
				((ITooltipWidget)w).gatherTooltip(mouseX, mouseY, tooltip);
		gatherAdditionalTooltips(
				mouseX, mouseY, tooltip::add, t -> tooltip.add(TextUtils.applyFormat(t, TextFormatting.GRAY))
		);
		if (!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
		else
			this.renderHoveredTooltip(transform, mouseX, mouseY);
	}

	protected boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h)
	{
		return mouseX >= guiLeft+x&&mouseY >= guiTop+y
				&&mouseX < guiLeft+x+w&&mouseY < guiTop+y+h;
	}

	protected void clearIntArray(IIntArray ints)
	{
		// Clear GUI ints, the sync code assumes that 0 is the initial state
		for(int i = 0; i < ints.size(); ++i)
			ints.set(i, 0);
	}

	public void fullInit()
	{
		super.init(minecraft, width, height);
	}

	@Override
	protected final void drawGuiContainerBackgroundLayer(@Nonnull MatrixStack transform, float partialTicks, int x, int y)
	{
		ClientUtils.bindTexture(background);
		drawBackgroundTexture(transform);
		drawContainerBackgroundPre(transform, partialTicks, x, y);
		for (InfoArea area : infoAreas.get())
			area.draw(transform);
	}

	protected void drawBackgroundTexture(MatrixStack transform) {
		blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	protected void drawContainerBackgroundPre(@Nonnull MatrixStack matrixStack, float partialTicks, int x, int y) {}

	protected void gatherAdditionalTooltips(
			int mouseX, int mouseY, Consumer<ITextComponent> addLine, Consumer<ITextComponent> addGray
	) {}

	public static ResourceLocation makeTextureLocation(String name)
	{
		return ImmersiveEngineering.rl("textures/gui/"+name+".png");
	}
}
