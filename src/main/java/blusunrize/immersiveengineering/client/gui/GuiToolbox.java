package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ContainerToolbox;
import blusunrize.immersiveengineering.common.util.Lib;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class GuiToolbox extends GuiContainer
{
	public GuiToolbox(InventoryPlayer inventoryPlayer, World world)
	{
		super(new ContainerToolbox(inventoryPlayer, world));
		this.ySize=238;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		int slot = -1;
		for(int i=0; i<((ContainerToolbox)this.inventorySlots).internalSlots; i++)
		{
			Slot s = (Slot)this.inventorySlots.inventorySlots.get(i);
			if(!s.getHasStack() && mx>guiLeft+s.xDisplayPosition&&mx<guiLeft+s.xDisplayPosition+16 && my>guiTop+s.yDisplayPosition&&my<guiTop+s.yDisplayPosition+16)
				slot = i;
		}
		String ss = null;
		if(slot>=0)
			ss = slot<3?"food": slot<10?"tool": slot<16?"wire": "any";
		if(ss!=null)
			tooltip.add(EnumChatFormatting.GRAY+StatCollector.translateToLocal(Lib.DESC_INFO+"toolbox."+ss));
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRendererObj, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/toolbox.png");
		this.drawTexturedModalRect(guiLeft,guiTop-17, 0,0, 176,ySize+17);
	}

}