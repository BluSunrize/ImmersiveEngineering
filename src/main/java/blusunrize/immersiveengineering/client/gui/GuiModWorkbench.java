package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.IESlot;

public class GuiModWorkbench extends GuiContainer
{
	//	TileEntityModWorkbench workbench;
	public GuiModWorkbench(InventoryPlayer inventoryPlayer, TileEntityModWorkbench tile )
	{
		super(new ContainerModWorkbench(inventoryPlayer, tile));
		//		workbench = tile;
		this.ySize=168;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		for(int i=0; i<((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput && !s.getHasStack())
			{
				BlueprintCraftingRecipe recipe = ((IESlot.BlueprintOutput)s).recipe;
				if(recipe!=null && recipe.output!=null)
					if(func_146978_c(s.xDisplayPosition,s.yDisplayPosition, 16,16, mx,my))
					{
						ArrayList<String> tooltip = new ArrayList<String>();
						tooltip.add(recipe.output.getRarity().rarityColor+recipe.output.getDisplayName());
						ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();  
						for(Object o : recipe.inputs)
						{
							ItemStack toAdd = (o instanceof ItemStack)?(ItemStack)o :(o instanceof ArrayList)?((ArrayList<ItemStack>)o).get(ClientUtils.mc().thePlayer.ticksExisted/10 %((ArrayList)o).size()): null;
							if(toAdd==null)
								continue;
							boolean isNew = true;
							for(ItemStack ss : inputs)
								if(OreDictionary.itemMatches(ss, toAdd, true))
								{
									ss.stackSize += toAdd.stackSize;
									isNew = false;
									break;
								}
							if(isNew)
								inputs.add(toAdd.copy());
						}
						for(ItemStack ss : inputs)
							tooltip.add(EnumChatFormatting.GRAY.toString()+ss.stackSize+"x "+ ss.getDisplayName());

						ClientUtils.drawHoveringText(tooltip, mx-guiLeft, my-guiTop, fontRendererObj);
						RenderHelper.enableGUIStandardItemLighting();
					}
			}
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/workbench.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);


		for(int i=0; i<((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);

			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition-1, guiTop+ s.yDisplayPosition-1, 17,1, 0x77222222);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition-1, guiTop+ s.yDisplayPosition+0, 1,16, 0x77222222);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+16, guiTop+ s.yDisplayPosition+0, 1,17, 0x77999999);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+0, guiTop+ s.yDisplayPosition+16, 16,1, 0x77999999);
			if( !(s instanceof IESlot.BlueprintOutput) || s.getHasStack() || ((IESlot.BlueprintOutput)s).recipe.output==null)
				ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+0, guiTop+ s.yDisplayPosition+0, 16,16, 0x77444444);
		}

		for(int i=0; i<((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput && !s.getHasStack())
			{
				ItemStack ghostStack = ((IESlot.BlueprintOutput)s).recipe.output;
				if(ghostStack!=null)
				{
					this.zLevel = 200.0F;
					itemRender.zLevel = 200.0F;
					FontRenderer font = ghostStack.getItem().getFontRenderer(ghostStack);
					if(font==null)
						font = fontRendererObj;
					itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), ghostStack, guiLeft+s.xDisplayPosition, guiTop+s.yDisplayPosition);
					this.zLevel = 0.0F;
					itemRender.zLevel = 0.0F;

					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+0, guiTop+ s.yDisplayPosition+0, 16,16, 0x77444444);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
		}
	}
}