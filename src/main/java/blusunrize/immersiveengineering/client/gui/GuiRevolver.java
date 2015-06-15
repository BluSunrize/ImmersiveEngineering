package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.items.ItemRevolver;

public class GuiRevolver extends GuiContainer
{
	boolean extended = false;
	public GuiRevolver(InventoryPlayer inventoryPlayer, World world)
	{
		super(new ContainerRevolver(inventoryPlayer, world));
		if(inventoryPlayer.player.getCurrentEquippedItem()!=null && inventoryPlayer.player.getCurrentEquippedItem().getItem() instanceof ItemRevolver && ((ItemRevolver)inventoryPlayer.player.getCurrentEquippedItem().getItem()).getBulletSlotAmount(inventoryPlayer.player.getCurrentEquippedItem())>8)
		extended = true;
		if(extended)
			this.xSize=189;
		this.ySize=214;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0,0, 176,ySize);
		if(extended)
			this.drawTexturedModalRect(guiLeft+106,guiTop+28, 176,28, 80,76);
	}

}