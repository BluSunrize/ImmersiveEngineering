/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonItem;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerAutoWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class GuiAutoWorkbench extends GuiIEContainerBase
{
	TileEntityAutoWorkbench tile;

	public GuiAutoWorkbench(InventoryPlayer inventoryPlayer, TileEntityAutoWorkbench tile)
	{
		super(new ContainerAutoWorkbench(inventoryPlayer, tile));
		this.tile = tile;
		this.ySize = 184;
	}

	@Override
	public void initGui()
	{
		this.buttonList.clear();
		super.initGui();
		Slot s = inventorySlots.getSlot(0);
		if(s!=null&&s.getHasStack()&&s.getStack().getItem() instanceof ItemEngineersBlueprint)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(s.getStack(), "blueprint"));
			if(recipes!=null&&recipes.length > 0)
			{
				int l = recipes.length;
				int xx = guiLeft+121;
				int yy = guiTop+(l > 6?59-(l-3)/3*18: l > 3?59: 68);
				for(int i = 0; i < l; i++)
					if(recipes[i]!=null&&!recipes[i].output.isEmpty())
					{
						this.buttonList.add(new GuiButtonItem(i, xx+(i%3)*18, yy+(i/3)*18, recipes[i].output.copy(), i==tile.selectedRecipe));
					}
			}
//			ItemStack stack = s.getStack();
//			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
//			int buttonid = 0;
//			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
//			if(boolArray!=null)
//				for(ToolConfigBoolean b : boolArray)
//					this.buttonList.add(new GuiButtonCheckbox(buttonid++, guiLeft+b.x,guiTop+b.y, tool.fomatConfigName(stack,b), b.value));
//			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
//			if(floatArray!=null)
//				for(ToolConfigFloat f : floatArray)
//					this.buttonList.add(new GuiSliderIE(buttonid++, guiLeft+f.x,guiTop+f.y, 80, tool.fomatConfigName(stack,f), f.value));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button instanceof GuiButtonItem)
		{
			if(button.id==tile.selectedRecipe)//disable
				tile.selectedRecipe = -1;
			else
				tile.selectedRecipe = button.id;
			NBTTagCompound message = new NBTTagCompound();
			message.setInteger("recipe", tile.selectedRecipe);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this.tile, message));
			initGui();
		}
	}

	NBTTagCompound lastMessage;

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		Slot s = inventorySlots.getSlot(0);
//		if(s!=null && s.getHasStack() && s.getStack().getItem() instanceof IConfigurableTool)
//		{
//			ItemStack stack = s.getStack();
//			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
//			NBTTagCompound message = new NBTTagCompound();
//			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
//			int iBool = 0;
//			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
//			int iFloat = 0;
//			for(GuiButton button : this.buttonList)
//			{
//				if(button instanceof GuiButtonCheckbox && boolArray!=null)
//					message.setBoolean("b_"+boolArray[iBool++].name,((GuiButtonCheckbox)button).state);
//				if(button instanceof GuiSliderIE && floatArray!=null)
//					message.setFloat("f_"+floatArray[iFloat++].name,(float)((GuiSliderIE)button).sliderValue);
//			}
//			if(lastMessage==null || !lastMessage.equals(message))//Only send packets when values have changed
//				ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this.tile, message));
//			lastMessage = message;
//		}
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);

		ArrayList<String> tooltip = new ArrayList<String>();
		if(mx > guiLeft+80&&mx < guiLeft+87&&my > guiTop+36&&my < guiTop+82)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF");

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
		ClientUtils.bindTexture("immersiveengineering:textures/gui/auto_workbench.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+80, guiTop+36+(46-stored), guiLeft+87, guiTop+82, 0xffb51500, 0xff600b00);

//		for(int i=0; i<((ContainerAutoWorkbench)inventorySlots).slotCount; i++)
//		{
//			Slot s = inventorySlots.getSlot(i);
//
//			ClientUtils.drawColouredRect(guiLeft+ s.xPos-1, guiTop+ s.yPos-1, 17,1, 0x77222222);
//			ClientUtils.drawColouredRect(guiLeft+ s.xPos-1, guiTop+ s.yPos+0, 1,16, 0x77222222);
//			ClientUtils.drawColouredRect(guiLeft+ s.xPos+16, guiTop+ s.yPos+0, 1,17, 0x77999999);
//			ClientUtils.drawColouredRect(guiLeft+ s.xPos+0, guiTop+ s.yPos+16, 16,1, 0x77999999);
//			if( !(s instanceof IESlot.BlueprintOutput) || s.getHasStack() || ((IESlot.BlueprintOutput)s).recipe.output==null)
//				ClientUtils.drawColouredRect(guiLeft+ s.xPos+0, guiTop+ s.yPos+0, 16,16, 0x77444444);
//		}
//
//		for(int i=0; i<((ContainerModWorkbench)inventorySlots).slotCount; i++)
//		{
//			Slot s = inventorySlots.getSlot(i);
//			if(s instanceof IESlot.BlueprintOutput && !s.getHasStack())
//			{
//				ItemStack ghostStack = ((IESlot.BlueprintOutput)s).recipe.output;
//				if(ghostStack!=null)
//				{
//					this.zLevel = 200.0F;
//					itemRender.zLevel = 200.0F;
//					FontRenderer font = ghostStack.getItem().getFontRenderer(ghostStack);
//					if(font==null)
//						font = fontRenderer;
//					itemRender.renderItemAndEffectIntoGUI(ghostStack, guiLeft+s.xPos, guiTop+s.yPos);
//					this.zLevel = 0.0F;
//					itemRender.zLevel = 0.0F;
//
//					GlStateManager.disableLighting();
//					GlStateManager.disableDepth();
//					ClientUtils.drawColouredRect(guiLeft+ s.xPos+0, guiTop+ s.yPos+0, 16,16, 0x77444444);
//					GlStateManager.enableLighting();
//					GlStateManager.enableDepth();
//				}
//			}
//		}
	}
}