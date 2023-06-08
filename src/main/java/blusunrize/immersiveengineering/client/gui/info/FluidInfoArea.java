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
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

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

	public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<Component> tooltip)
	{

		if(!fluid.isEmpty())
			tooltip.accept(applyFormat(
					fluid.getDisplayName(),
					fluid.getFluid().getFluidType().getRarity(fluid).color
			));
		else
			tooltip.accept(Component.translatable("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof PotionFluid potion)
			potion.addInformation(fluid, tooltip);

		if(mc().options.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!Screen.hasShiftDown())
				tooltip.accept(Component.translatable(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				//TODO translation keys
				tooltip.accept(applyFormat(Component.literal("Fluid Registry: "+BuiltInRegistries.FLUID.getKey(fluid.getFluid())), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("Density: "+fluid.getFluid().getFluidType().getDensity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("Temperature: "+fluid.getFluid().getFluidType().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("Viscosity: "+fluid.getFluid().getFluidType().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("NBT Data: "+fluid.getTag()), ChatFormatting.DARK_GRAY));
			}
		}

		if(tankCapacity > 0)
			tooltip.accept(applyFormat(Component.literal(fluid.getAmount()+"/"+tankCapacity+"mB"), ChatFormatting.GRAY));
		else if(tankCapacity==0)
			tooltip.accept(applyFormat(Component.literal(fluid.getAmount()+"mB"), ChatFormatting.GRAY));
		//don't display amount for tankCapacity < 0, i.e. for ghost fluid stacks
	}

	@Override
	public void draw(GuiGraphics graphics)
	{
		FluidStack fluid = tank.getFluid();
		float capacity = tank.getCapacity();
		graphics.pose().pushPose();
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		if(!fluid.isEmpty())
		{
			int fluidHeight = (int)(area.getHeight()*(fluid.getAmount()/capacity));
			GuiHelper.drawRepeatedFluidSpriteGui(buffer, graphics.pose(), fluid, area.getX(), area.getY()+area.getHeight()-fluidHeight, area.getWidth(), fluidHeight);
		}
		int xOff = (area.getWidth()-overlayWidth)/2;
		int yOff = (area.getHeight()-overlayHeight)/2;
		RenderType renderType = IERenderTypes.getGui(overlayTexture);
		GuiHelper.drawTexturedRect(
				buffer.getBuffer(renderType), graphics.pose(),
				area.getX()+xOff, area.getY()+yOff, overlayWidth, overlayHeight,
				256f, overlayUMin, overlayUMin+overlayWidth, overlayVMin, overlayVMin+overlayHeight
		);
		buffer.endBatch();
		graphics.pose().popPose();
	}
}
