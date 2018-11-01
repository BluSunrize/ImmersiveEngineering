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
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMixer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.gui.ContainerMixer;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class GuiMixer extends GuiIEContainerBase
{
	TileEntityMixer tile;

	public GuiMixer(InventoryPlayer inventoryPlayer, TileEntityMixer tile)
	{
		super(new ContainerMixer(inventoryPlayer, tile));
		this.tile = tile;
		this.ySize = 167;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButtonState(0, guiLeft+106, guiTop+61, 30, 16, null, tile.outputAll, "immersiveengineering:textures/gui/mixer.png", 176, 82, 1));
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button.id==0)
		{
			NBTTagCompound tag = new NBTTagCompound();
			tile.outputAll = ((GuiButtonState)button).state;
			tag.setBoolean("outputAll", tile.outputAll);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();

		if(mx >= guiLeft+76&&mx <= guiLeft+134&&my >= guiTop+11&&my <= guiTop+58)
		{
			float capacity = tile.tank.getCapacity();
			if(tile.tank.getFluidTypes()==0)
				tooltip.add(I18n.format("gui.immersiveengineering.empty"));
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
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF");
		if(mx >= guiLeft+106&&mx <= guiLeft+136&&my >= guiTop+61&&my <= guiTop+77)
			tooltip.add(I18n.format(Lib.GUI_CONFIG+"mixer.output"+(tile.outputAll?"All": "Single")));
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/mixer.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(MultiblockProcess process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
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
