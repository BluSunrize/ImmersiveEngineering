/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.RefineryMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class RefineryScreen extends IEContainerScreen<RefineryMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("refinery");

	public RefineryScreen(RefineryMenu container, Inventory inventoryPlayer, Component component)
	{
		super(container, inventoryPlayer, component, TEXTURE);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tanks.leftInput(), new Rect2i(leftPos+13, topPos+20, 16, 47), 177, 31, 20, 51, TEXTURE),
				new FluidInfoArea(menu.tanks.rightInput(), new Rect2i(leftPos+40, topPos+20, 16, 47), 177, 31, 20, 51, TEXTURE),
				new FluidInfoArea(menu.tanks.output(), new Rect2i(leftPos+109, topPos+20, 16, 47), 177, 31, 20, 51, TEXTURE),
				new EnergyInfoArea(leftPos+157, topPos+21, menu.energy)
		);
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		Slot s = this.menu.slots.get(0);
		if(!s.hasItem()&&mouseX > leftPos+s.x&&mouseX < leftPos+s.x+16&&mouseY > topPos+s.y&&mouseY < topPos+s.y+16)
			addLine.accept(Component.translatable(Lib.DESC_INFO+"refinery.slot.catalyst"));
	}
}