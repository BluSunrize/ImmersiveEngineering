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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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
	private final Rect2i area;
	private final int overlayUMin;
	private final int overlayVMin;
	private final int overlayWidth;
	private final int overlayHeight;
	private final ResourceLocation overlayTexture;

	public FluidInfoArea(IFluidTank tank, Rect2i area, int overlayUMin, int overlayVMin, int overlayWidth, int overlayHeight, ResourceLocation overlayTexture)
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
	public void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
	}

	public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<Component> tooltip) {

		if(!fluid.isEmpty())
			tooltip.accept(applyFormat(
					fluid.getDisplayName(),
					fluid.getFluid().getAttributes().getRarity(fluid).color
			));
		else
			tooltip.accept(new TranslatableComponent("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof IEFluid)
		{
			List<Component> temp = new ArrayList<>();
			((IEFluid)fluid.getFluid()).addTooltipInfo(fluid, null, temp);
			temp.forEach(tooltip);
		}

		if(mc().options.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!Screen.hasShiftDown())
				tooltip.accept(new TranslatableComponent(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				//TODO translation keys
				tooltip.accept(applyFormat(new TextComponent("Fluid Registry: "+fluid.getFluid().getRegistryName()), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new TextComponent("Density: "+fluid.getFluid().getAttributes().getDensity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new TextComponent("Temperature: "+fluid.getFluid().getAttributes().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new TextComponent("Viscosity: "+fluid.getFluid().getAttributes().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(new TextComponent("NBT Data: "+fluid.getTag()), ChatFormatting.DARK_GRAY));
			}
		}

		if(tankCapacity > 0)
			tooltip.accept(applyFormat(new TextComponent(fluid.getAmount()+"/"+tankCapacity+"mB"), ChatFormatting.GRAY));
		else if (tankCapacity == 0)
			tooltip.accept(applyFormat(new TextComponent(fluid.getAmount()+"mB"), ChatFormatting.GRAY));
		//don't display amount for tankCapacity < 0, i.e. for ghost fluid stacks
	}

	@Override
	public void draw(PoseStack transform)
	{
		FluidStack fluid = tank.getFluid();
		float capacity = tank.getCapacity();
		transform.pushPose();
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
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
		buffer.endBatch();
		transform.popPose();
	}
}
