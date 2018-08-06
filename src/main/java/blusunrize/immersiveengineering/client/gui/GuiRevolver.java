/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class GuiRevolver extends GuiIEContainerBase
{
	private int[] bullets = new int[2];
	private boolean otherRevolver = false;
	private int offset = 0;

	public GuiRevolver(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(new ContainerRevolver(inventoryPlayer, world, slot, revolver));
		if(!revolver.isEmpty()&&revolver.getItem() instanceof IBulletContainer)
			this.bullets[0] = ((IBulletContainer)revolver.getItem()).getBulletCount(revolver);
		this.otherRevolver = !((ContainerRevolver)this.inventorySlots).secondRevolver.isEmpty();
		if(this.otherRevolver)
		{
			this.bullets[1] = ((IBulletContainer)((ContainerRevolver)this.inventorySlots).secondRevolver.getItem()).getBulletCount(((ContainerRevolver)this.inventorySlots).secondRevolver);
			this.offset = ((bullets[0] >= 18?150: bullets[0] > 8?136: 74)+(bullets[1] >= 18?150: bullets[1] > 8?136: 74)+4-176)/2;
			if(this.offset > 0)
				this.xSize += this.offset*2;
		}
		else
			this.offset = ((bullets[0] >= 18?150: bullets[0] > 8?136: 74)-176)/2;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
		this.drawTexturedModalRect(guiLeft+(offset > 0?offset: 0), guiTop+77, 0, 125, 176, 89);

		int off = (offset < 0?-offset: 0);
		for(int hand = 0; hand < (otherRevolver?2: 1); hand++)
		{
			int side = !otherRevolver?0: (hand==0)==(ImmersiveEngineering.proxy.getClientPlayer().getPrimaryHand()==EnumHandSide.RIGHT)?1: 0;
			this.drawTexturedModalRect(guiLeft+off+00, guiTop+1, 00, 51, 74, 74);
			if(bullets[side] >= 18)
				this.drawTexturedModalRect(guiLeft+off+47, guiTop+1, 74, 51, 103, 74);
			else if(bullets[side] > 8)
				this.drawTexturedModalRect(guiLeft+off+57, guiTop+1, 57, 12, 79, 39);
			off += (bullets[side] >= 18?150: bullets[side] > 8?136: 74)+4;
		}
	}


	public static void drawExternalGUI(NonNullList<ItemStack> bullets, int bulletAmount)
	{
		ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
		GlStateManager.color(1, 1, 1, 1);

		RenderHelper.disableStandardItemLighting();

		ClientUtils.drawTexturedRect(0, 1, 74, 74, 0/256f, 74/256f, 51/256f, 125/256f);
		if(bulletAmount >= 18)
			ClientUtils.drawTexturedRect(47, 1, 103, 74, 74/256f, 177/256f, 51/256f, 125/256f);
		else if(bulletAmount > 8)
			ClientUtils.drawTexturedRect(57, 1, 79, 39, 57/256f, 136/256f, 12/256f, 51/256f);

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableDepth();

		RenderItem ir = ClientUtils.mc().getRenderItem();
		int[][] slots = ContainerRevolver.slotPositions[bulletAmount >= 18?2: bulletAmount > 8?1: 0];
		for(int i = 0; i < bulletAmount; i++)
		{
			ItemStack b = bullets.get(i);
			if(!b.isEmpty())
			{
				int x;
				int y;
				if(i==0)
				{
					x = 29;
					y = 3;
				}
				else if(i-1 < slots.length)
				{
					x = slots[i-1][0];
					y = slots[i-1][1];
				}
				else
				{
					int ii = i-(slots.length+1);
					x = ii==0?48: ii==1?29: ii==3?2: 10;
					y = ii==1?57: ii==3?30: ii==4?11: 49;
				}
				ir.renderItemIntoGUI(b, x, y);
			}
		}

		GlStateManager.disableDepth();
	}
}