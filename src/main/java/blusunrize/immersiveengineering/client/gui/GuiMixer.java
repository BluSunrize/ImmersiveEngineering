/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import blusunrize.immersiveengineering.common.gui.ContainerMixer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class GuiMixer extends GuiIEContainerBase
{
	MixerTileEntity tile;

	public GuiMixer(PlayerInventory inventoryPlayer, MixerTileEntity tile)
	{
		super(new ContainerMixer(inventoryPlayer, tile));
		this.tile = tile;
		this.ySize = 167;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttons.clear();
		this.buttons.add(new GuiButtonState(0, guiLeft+106, guiTop+61, 30, 16, null, tile.outputAll, "immersiveengineering:textures/gui/mixer.png", 176, 82, 1)
		{
			@Override
			public void onClick(double mX, double mY)
			{
				CompoundNBT tag = new CompoundNBT();
				tile.outputAll = state;
				tag.putBoolean("outputAll", tile.outputAll);
				ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
				initGui();
			}
		});
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();

		if(mx >= guiLeft+76&&mx <= guiLeft+134&&my >= guiTop+11&&my <= guiTop+58)
		{
			float capacity = tile.tank.getCapacity();
			if(tile.tank.getFluidTypes()==0)
				tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
			else
			{

				int fluidUpToNow = 0;
				int lastY = 0;
				int myRelative = guiTop+58-my;
				for(int i = tile.tank.getFluidTypes()-1; i >= 0; i--)
				{
					FluidStack fs = tile.tank.fluids.get(i);
					if(fs!=null&&fs.getFluid()!=null)
					{
						fluidUpToNow += fs.amount;
						int newY = (int)(47*(fluidUpToNow/capacity));
						if(myRelative >= lastY&&myRelative < newY)
						{
							ClientUtils.addFluidTooltip(fs, tooltip, (int)capacity);
							break;
						}
						lastY = newY;
					}
				}
			}
		}
		if(mx >= guiLeft+158&&mx < guiLeft+165&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(mx >= guiLeft+106&&mx <= guiLeft+136&&my >= guiTop+61&&my <= guiTop+77)
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"mixer.output"+(tile.outputAll?"All": "Single")));
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/mixer.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(MultiblockProcess process : tile.processQueue)
			if(process instanceof PoweredMultiblockTileEntity.MultiblockProcessInMachine)
			{
				float mod = 1-(process.processTick/(float)process.maxTicks);
				for(int slot : ((MultiblockProcessInMachine)process).getInputSlots())
				{
					int h = (int)Math.max(1, mod*16);
					this.drawTexturedModalRect(guiLeft+24+slot%2*21, guiTop+7+slot/2*18+(16-h), 176, 16-h, 2, h);
				}
			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+158, guiTop+22+(46-stored), guiLeft+165, guiTop+68, 0xffb51500, 0xff600b00);

		float capacity = tile.tank.getCapacity();
		int fluidUpToNow = 0;
		int lastY = 0;
		for(int i = tile.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = tile.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				fluidUpToNow += fs.amount;
				int newY = (int)(47*(fluidUpToNow/capacity));
				ClientUtils.drawRepeatedFluidSprite(fs, guiLeft+76, guiTop+58-newY, 58, newY-lastY);
				lastY = newY;
			}
		}
	}
}
