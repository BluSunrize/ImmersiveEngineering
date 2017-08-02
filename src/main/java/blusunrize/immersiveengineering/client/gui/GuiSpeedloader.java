package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ContainerSpeedloader;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class GuiSpeedloader extends GuiIEContainerBase
{
	public GuiSpeedloader(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(new ContainerSpeedloader(inventoryPlayer, world, slot, revolver));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
		this.drawTexturedModalRect(guiLeft,guiTop+77, 0,125, 176,89);
		this.drawTexturedModalRect(guiLeft+51,guiTop+1, 00,51, 74,74);
	}

}