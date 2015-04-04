package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;

public class GuiRevolver extends GuiContainer
{
	public GuiRevolver(InventoryPlayer inventoryPlayer, World world)
	{
		super(new ContainerRevolver(inventoryPlayer, world));
		this.ySize=214;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);
	}

}
