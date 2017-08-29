package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class GuiRevolver extends GuiIEContainerBase
{
	private int[] bullets = new int[2];
	private boolean otherRevolver = false;
	private int offset = 0;

	public GuiRevolver(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(new ContainerRevolver(inventoryPlayer, world, slot, revolver));
		if(!revolver.isEmpty() && revolver.getItem() instanceof IBulletContainer)
			this.bullets[0] = ((IBulletContainer)revolver.getItem()).getBulletCount(revolver);
		this.otherRevolver = !((ContainerRevolver)this.inventorySlots).secondRevolver.isEmpty();
		if(this.otherRevolver)
		{
			this.bullets[1] = ((IBulletContainer)((ContainerRevolver)this.inventorySlots).secondRevolver.getItem()).getBulletCount(((ContainerRevolver)this.inventorySlots).secondRevolver);
			this.offset = ((bullets[0]>=18?150:bullets[0]>8?136:74)+(bullets[1]>=18?150:bullets[1]>8?136:74)+4-176)/2;
			if(this.offset>0)
				this.xSize += this.offset*2;
		}
		else
			this.offset = ((bullets[0]>=18?150:bullets[0]>8?136:74)-176)/2;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
		this.drawTexturedModalRect(guiLeft+(offset>0?offset:0),guiTop+77, 0,125, 176,89);

		int off = (offset<0?-offset:0);
		for(int hand=0; hand<(otherRevolver?2:1); hand++)
		{
			int side = !otherRevolver?0: (hand==0)==(ImmersiveEngineering.proxy.getClientPlayer().getPrimaryHand()==EnumHandSide.RIGHT)?1:0;
			this.drawTexturedModalRect(guiLeft+off+00, guiTop+1, 00, 51, 74, 74);
			if(bullets[side] >= 18)
				this.drawTexturedModalRect(guiLeft+off+47, guiTop+1, 74, 51, 103, 74);
			else if(bullets[side] > 8)
				this.drawTexturedModalRect(guiLeft+off+57, guiTop+1, 57, 12, 79, 39);
			off += (bullets[side] >= 18?150: bullets[side] > 8?136: 74)+4;
		}
	}

}