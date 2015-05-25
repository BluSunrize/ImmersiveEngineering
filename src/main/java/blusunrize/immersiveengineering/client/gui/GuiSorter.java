package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.gui.ContainerSorter;

public class GuiSorter extends GuiContainer
{
	TileEntityConveyorSorter tile;
	public GuiSorter(InventoryPlayer inventoryPlayer, TileEntityConveyorSorter tile)
	{
		super(new ContainerSorter(inventoryPlayer, tile));
		this.tile=tile;
		this.ySize = 208;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		//		ArrayList<String> tooltip = null;
		//		if(mx>guiLeft+98&&mx<guiLeft+98+16 && my>guiTop+21&&my<guiTop+21+47)
		//		{
		//			tooltip = new ArrayList<String>();
		//			if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		//				tooltip.add(tile.tank.getFluid().getLocalizedName());
		//			else
		//				tooltip.add(StatCollector.translateToLocal("gui.ImmersiveEngineering.empty"));
		//			tooltip.add(tile.tank.getFluidAmount()+"/"+tile.tank.getCapacity()+"mB");
		//		}
		//		if(mx>guiLeft+157&&mx<guiLeft+164 && my>guiTop+22&&my<guiTop+68)
		//		{
		//			tooltip = new ArrayList<String>();
		//			tooltip.add(tile.energyStorage.getEnergyStored()+"/"+tile.energyStorage.getMaxEnergyStored()+" RF");
		//		}
		//
		//		if(tooltip!=null)
		//		{
		//			this.drawHoveringText(tooltip, mx-guiLeft, my-guiTop, fontRendererObj);
		//			RenderHelper.enableGUIStandardItemLighting();
		//		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/sorter.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		for(int side=0; side<6; side++)
		{
			int x = guiLeft+ 30+ (side/2)*58;
			int y = guiTop+ 26+ (side%2)*58;
			String s = StatCollector.translateToLocal("desc.ImmersiveEngineering.info.blockSide."+ForgeDirection.getOrientation(side).toString()).substring(0, 1);
			GL11.glEnable(3042);
			ClientUtils.font().drawString(s, x-(ClientUtils.font().getStringWidth(s)/2), y, 0xaacccccc, true);
		}
	}
}
