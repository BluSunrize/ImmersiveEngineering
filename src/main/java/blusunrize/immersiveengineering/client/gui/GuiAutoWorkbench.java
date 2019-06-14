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
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

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
		this.buttons.clear();
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
						this.buttons.add(new GuiButtonItem(i, xx+(i%3)*18, yy+(i/3)*18, recipes[i].output.copy(), i==tile.selectedRecipe)
						{
							@Override
							public void onClick(double mouseX, double mouseY)
							{
								super.onClick(mouseX, mouseY);
								if(id==tile.selectedRecipe)//disable
									tile.selectedRecipe = -1;
								else
									tile.selectedRecipe = id;
								NBTTagCompound message = new NBTTagCompound();
								message.setInt("recipe", tile.selectedRecipe);
								ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, message));
								initGui();
							}
						});
					}
			}
		}
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);

		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		if(mx > guiLeft+80&&mx < guiLeft+87&&my > guiTop+36&&my < guiTop+82)
			tooltip.add(new TextComponentString(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/auto_workbench.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+80, guiTop+36+(46-stored), guiLeft+87, guiTop+82, 0xffb51500, 0xff600b00);
	}
}