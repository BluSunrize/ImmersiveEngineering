/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.client.TextUtils.applyFormat;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class FluidInfoArea extends InfoArea
{
	private final IFluidTank tank;
	private final Rectangle2d area;
	private final int overlayUMin;
	private final int overlayVMin;
	private final int overlayWidth;
	private final int overlayHeight;
	private final ResourceLocation overlayTexture;

	public FluidInfoArea(IFluidTank tank, Rectangle2d area, int overlayUMin, int overlayVMin, int overlayWidth, int overlayHeight, ResourceLocation overlayTexture)
	{
		super(area);
		this.tank = tank;
		this.area = area;
		this.overlayUMin = overlayUMin;
		this.overlayVMin = overlayVMin;
		this.overlayWidth = overlayWidth;
		this.overlayHeight = overlayHeight;
		this.overlayTexture = overlayTexture;
	}


	@Override
	public void fillTooltipOverArea(int mouseX, int mouseY, List<ITextComponent> tooltip)
	{
		fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
	}

	public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<ITextComponent> tooltip) {

		if(!fluid.isEmpty())
			tooltip.accept(applyFormat(
					fluid.getDisplayName(),
					fluid.getFluid().getAttributes().getRarity(fluid).color
			));
		else
			tooltip.accept(new TranslationTextComponent("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof IEFluid)
		{
			List<ITextComponent> temp = new ArrayList<>();
			((IEFluid)fluid.getFluid()).addTooltipInfo(fluid, null, temp);
			temp.forEach(tooltip);
		}

		if(mc().gameSettings.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!Screen.hasShiftDown())
				tooltip.accept(new TranslationTextComponent(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				//TODO translation keys
				tooltip.accept(applyFormat(new StringTextComponent("Fluid Registry: "+fluid.getFluid().getRegistryName()), TextFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new StringTextComponent("Density: "+fluid.getFluid().getAttributes().getDensity(fluid)), TextFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new StringTextComponent("Temperature: "+fluid.getFluid().getAttributes().getTemperature(fluid)), TextFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new StringTextComponent("Viscosity: "+fluid.getFluid().getAttributes().getViscosity(fluid)), TextFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new StringTextComponent("NBT Data: "+fluid.getTag()), TextFormatting.DARK_GRAY));
			}
		}

		if(tankCapacity > 0)
			tooltip.accept(applyFormat(new StringTextComponent(fluid.getAmount()+"/"+tankCapacity+"mB"), TextFormatting.GRAY));
		else
			tooltip.accept(applyFormat(new StringTextComponent(fluid.getAmount()+"mB"), TextFormatting.GRAY));
	}

	@Override
	public void draw(MatrixStack transform)
	{
		FluidStack fluid = tank.getFluid();
		float capacity = tank.getCapacity();
		transform.push();
		IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		if(!fluid.isEmpty())
		{
			int fluidHeight = (int)(area.getHeight()*(fluid.getAmount()/capacity));
			GuiHelper.drawRepeatedFluidSpriteGui(buffer, transform, fluid, area.getX(), area.getY()+area.getHeight()-fluidHeight, area.getWidth(), fluidHeight);
		}
		int xOff = (area.getWidth()-overlayWidth)/2;
		int yOff = (area.getHeight()-overlayHeight)/2;
		RenderType renderType = IERenderTypes.getGui(overlayTexture);
		GuiHelper.drawTexturedRect(
				buffer.getBuffer(renderType), transform,
				area.getX()+xOff, area.getY()+yOff, overlayWidth, overlayHeight,
				256f, overlayUMin, overlayUMin+overlayWidth, overlayVMin, overlayVMin+overlayHeight
		);
		buffer.finish();
		transform.pop();
	}
}
