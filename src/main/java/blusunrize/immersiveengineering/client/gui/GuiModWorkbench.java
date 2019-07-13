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
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiModWorkbench extends GuiIEContainerBase
{
	ModWorkbenchTileEntity workbench;

	public GuiModWorkbench(PlayerInventory inventoryPlayer, World world, ModWorkbenchTileEntity tile)
	{
		super(new ContainerModWorkbench(inventoryPlayer, world, tile));
		workbench = tile;
		this.ySize = 168;
	}

	@Override
	public void initGui()
	{
		this.buttons.clear();
		super.initGui();
		Slot s = inventorySlots.getSlot(0);
		if(s!=null&&s.getHasStack()&&s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			int buttonid = 0;
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			if(boolArray!=null)
				for(ToolConfigBoolean b : boolArray)
					this.buttons.add(new GuiButtonCheckbox(buttonid++, guiLeft+b.x, guiTop+b.y, tool.fomatConfigName(stack, b), b.value));
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			if(floatArray!=null)
				for(ToolConfigFloat f : floatArray)
					this.buttons.add(new GuiSliderIE(buttonid++, guiLeft+f.x, guiTop+f.y, 80, tool.fomatConfigName(stack, f), f.value));
		}
	}

	CompoundNBT lastMessage;

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		Slot s = inventorySlots.getSlot(0);
		if(s!=null&&s.getHasStack()&&s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			CompoundNBT message = new CompoundNBT();
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			int iBool = 0;
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			int iFloat = 0;
			for(Button button : this.buttons)
			{
				if(button instanceof GuiButtonCheckbox&&boolArray!=null)
					message.putBoolean("b_"+boolArray[iBool++].name, ((GuiButtonCheckbox)button).state);
				if(button instanceof GuiSliderIE&&floatArray!=null)
					message.putFloat("f_"+floatArray[iFloat++].name, (float)((GuiSliderIE)button).sliderValue);
			}
			if(!message.equals(lastMessage))//Only send packets when values have changed
				ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this.workbench, message));
			lastMessage = message;
			return true;
		}
		return false;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		for(int i = 0; i < ((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput&&!s.getHasStack())
			{
				BlueprintCraftingRecipe recipe = ((IESlot.BlueprintOutput)s).recipe;
				if(recipe!=null&&!recipe.output.isEmpty())
					if(isPointInRegion(s.xPos, s.yPos, 16, 16, mx, my))
					{
						List<ITextComponent> tooltip = new ArrayList<>();
						tooltip.add(recipe.output.getDisplayName().createCopy().setStyle(new Style().setColor(recipe.output.getRarity().color)));
						ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();
						for(IngredientStack stack : recipe.inputs)
						{
							ItemStack toAdd = Utils.copyStackWithAmount(stack.getRandomizedExampleStack(this.mc.player.ticksExisted), stack.inputSize);
							if(toAdd.isEmpty())
								continue;
							boolean isNew = true;
							for(ItemStack ss : inputs)
								if(ItemStack.areItemStacksEqual(ss, toAdd))
								{
									ss.grow(toAdd.getCount());
									isNew = false;
									break;
								}
							if(isNew)
								inputs.add(toAdd.copy());
						}
						for(ItemStack ss : inputs)
							tooltip.add(new StringTextComponent(ss.getCount()+"x "+ss.getDisplayName())
									.setStyle(new Style().setColor(TextFormatting.GRAY)));

						ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer);
						RenderHelper.enableGUIStandardItemLighting();
					}
			}
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/workbench.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);


		for(int i = 0; i < ((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			ClientUtils.drawColouredRect(guiLeft+s.xPos-1, guiTop+s.yPos-1, 17, 1, 0x77222222);
			ClientUtils.drawColouredRect(guiLeft+s.xPos-1, guiTop+s.yPos+0, 1, 16, 0x77222222);
			ClientUtils.drawColouredRect(guiLeft+s.xPos+16, guiTop+s.yPos+0, 1, 17, 0x77999999);
			ClientUtils.drawColouredRect(guiLeft+s.xPos+0, guiTop+s.yPos+16, 16, 1, 0x77999999);
			ClientUtils.drawColouredRect(guiLeft+s.xPos+0, guiTop+s.yPos+0, 16, 16, 0x77444444);
		}

		for(int i = 0; i < ((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput&&!s.getHasStack())
			{
				ItemStack ghostStack = ((IESlot.BlueprintOutput)s).recipe.output;
				if(!ghostStack.isEmpty())
				{
					this.zLevel = 200.0F;
					itemRender.zLevel = 200.0F;
					FontRenderer font = ghostStack.getItem().getFontRenderer(ghostStack);
					if(font==null)
						font = fontRenderer;
					itemRender.renderItemAndEffectIntoGUI(ghostStack, guiLeft+s.xPos, guiTop+s.yPos);
					this.zLevel = 0.0F;
					itemRender.zLevel = 0.0F;
					GlStateManager.depthFunc(GL11.GL_GREATER);
					ClientUtils.drawColouredRect(guiLeft+s.xPos+0, guiTop+s.yPos+0, 16, 16, 0xbb333333);
					GlStateManager.depthFunc(GL11.GL_LEQUAL);
				}
			}
		}
	}
}