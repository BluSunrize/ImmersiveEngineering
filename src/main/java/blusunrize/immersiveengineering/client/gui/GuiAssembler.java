package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.gui.ContainerAssembler;

public class GuiAssembler extends GuiContainer
{
	TileEntityAssembler tile;
	public GuiAssembler(InventoryPlayer inventoryPlayer, TileEntityAssembler tile)
	{
		super(new ContainerAssembler(inventoryPlayer, tile));
		this.tile=tile;
		this.xSize = 230;
		this.ySize = 218;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		ArrayList<String> tooltip = new ArrayList<String>();
		if(mx>=guiLeft+187&&mx<guiLeft+194 && my>=guiTop+12&&my<guiTop+59)
			tooltip.add(tile.energyStorage.getEnergyStored()+"/"+tile.energyStorage.getMaxEnergyStored()+" RF");
		
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+204,guiTop+13,16,46, 250,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+182,guiTop+70,16,46, 250,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",tooltip);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+204,guiTop+70,16,46, 250,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",tooltip);
		
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx-guiLeft, my-guiTop, fontRendererObj, xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/assembler.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);
		
		int stored = (int)(46*(tile.energyStorage.getEnergyStored()/(float)tile.energyStorage.getMaxEnergyStored()));
		ClientUtils.drawGradientRect(guiLeft+187,guiTop+13+(46-stored), guiLeft+194,guiTop+59, 0xffb51500, 0xff600b00);
	
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+204,guiTop+13,16,46, 230,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+182,guiTop+70,16,46, 230,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",null);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+204,guiTop+70,16,46, 230,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",null);
	}
}
