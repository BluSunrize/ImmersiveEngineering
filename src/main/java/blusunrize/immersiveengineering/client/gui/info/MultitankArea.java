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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class MultitankArea extends InfoArea
{
	private final MultiFluidTank tank;

	public MultitankArea(Rectangle2d area, MultiFluidTank tank)
	{
		super(area);
		this.tank = tank;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<ITextComponent> tooltip)
	{
		if(tank.getFluidTypes()==0)
			tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
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
	public void draw(MatrixStack transform)
	{
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		forEachFluid((fluid, lastY, newY) -> GuiHelper.drawRepeatedFluidSpriteGui(
				buffers, transform, fluid, area.getX(), area.getY()+area.getHeight()-newY, area.getWidth(), newY-lastY
		));
		buffers.finish();
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
