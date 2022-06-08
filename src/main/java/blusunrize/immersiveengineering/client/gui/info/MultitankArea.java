/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class MultitankArea extends InfoArea
{
	private final MultiFluidTank tank;

	public MultitankArea(Rect2i area, MultiFluidTank tank)
	{
		super(area);
		this.tank = tank;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		if(tank.getFluidTypes()==0)
			tooltip.add(Component.translatable("gui.immersiveengineering.empty"));
		else
		{
			float capacity = tank.getCapacity();
			int myRelative = area.getY()+area.getHeight()-mouseY;
			forEachFluid((fluid, lastY, newY) -> {
				if(myRelative >= lastY&&myRelative < newY)
					FluidInfoArea.fillTooltip(fluid, (int)capacity, tooltip::add);
			});
		}
	}

	@Override
	public void draw(PoseStack transform)
	{
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		forEachFluid((fluid, lastY, newY) -> GuiHelper.drawRepeatedFluidSpriteGui(
				buffers, transform, fluid, area.getX(), area.getY()+area.getHeight()-newY, area.getWidth(), newY-lastY
		));
		buffers.endBatch();
	}

	private void forEachFluid(TankVisitor visitor) {
		float capacity = tank.getCapacity();
		int fluidUpToNow = 0;
		int lastY = 0;
		for(int i = tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = tank.fluids.get(i);
			if(!fs.isEmpty())
			{
				fluidUpToNow += fs.getAmount();
				int newY = (int)(area.getHeight()*(fluidUpToNow/capacity));
				visitor.visit(fs, lastY, newY);
				lastY = newY;
			}
		}
	}

	private interface TankVisitor {
		void visit(FluidStack fluid, int lastY, int newY);
	}
}
