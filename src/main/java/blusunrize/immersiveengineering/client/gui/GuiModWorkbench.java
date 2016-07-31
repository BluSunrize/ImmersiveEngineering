package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class GuiModWorkbench extends GuiContainer
{
	TileEntityModWorkbench workbench;
	public GuiModWorkbench(InventoryPlayer inventoryPlayer, TileEntityModWorkbench tile )
	{
		super(new ContainerModWorkbench(inventoryPlayer, tile));
		workbench = tile;
		this.ySize=168;
	}

	@Override
	public void initGui()
	{
		this.buttonList.clear();
		super.initGui();
		Slot s = inventorySlots.getSlot(0);
		if(s!=null && s.getHasStack() && s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			int buttonid = 0;
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			if(boolArray!=null)
				for(ToolConfigBoolean b : boolArray)
					this.buttonList.add(new GuiButtonCheckbox(buttonid++, guiLeft+b.x,guiTop+b.y, tool.fomatConfigName(stack,b), b.value));
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			if(floatArray!=null)
				for(ToolConfigFloat f : floatArray)
					this.buttonList.add(new GuiSliderIE(buttonid++, guiLeft+f.x,guiTop+f.y, 80, tool.fomatConfigName(stack,f), f.value));
		}
	}

	NBTTagCompound lastMessage;
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		Slot s = inventorySlots.getSlot(0);
		if(s!=null && s.getHasStack() && s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			NBTTagCompound message = new NBTTagCompound();
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			int iBool = 0;
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			int iFloat = 0;
			for(GuiButton button : this.buttonList)
			{
				if(button instanceof GuiButtonCheckbox && boolArray!=null)
					message.setBoolean("b_"+boolArray[iBool++].name,((GuiButtonCheckbox)button).state);
				if(button instanceof GuiSliderIE && floatArray!=null)
					message.setFloat("f_"+floatArray[iFloat++].name,(float)((GuiSliderIE)button).sliderValue);
			}
			if(lastMessage==null || !lastMessage.equals(message))//Only send packets when values have changed
				ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this.workbench, message));
			lastMessage = message;
		}
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		for(int i=0; i<((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput && !s.getHasStack())
			{
				BlueprintCraftingRecipe recipe = ((IESlot.BlueprintOutput)s).recipe;
				if(recipe!=null && recipe.output!=null)
					if(isPointInRegion(s.xDisplayPosition,s.yDisplayPosition, 16,16, mx,my))
					{
						ArrayList<String> tooltip = new ArrayList<String>();
						tooltip.add(recipe.output.getRarity().rarityColor+recipe.output.getDisplayName());
						ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();  
						for(Object o : recipe.inputs)
						{
							ItemStack toAdd = (o instanceof ItemStack)?(ItemStack)o :(o instanceof List)?((List<ItemStack>)o).get(ClientUtils.mc().thePlayer.ticksExisted/10 %((ArrayList)o).size()): null;
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
							tooltip.add(TextFormatting.GRAY.toString()+ss.stackSize+"x "+ ss.getDisplayName());

						ClientUtils.drawHoveringText(tooltip, mx, my, fontRendererObj);
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
					itemRender.renderItemAndEffectIntoGUI(ghostStack, guiLeft+s.xDisplayPosition, guiTop+s.yDisplayPosition);
					this.zLevel = 0.0F;
					itemRender.zLevel = 0.0F;

					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+0, guiTop+ s.yDisplayPosition+0, 16,16, 0x77444444);
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
			}
		}
	}
}