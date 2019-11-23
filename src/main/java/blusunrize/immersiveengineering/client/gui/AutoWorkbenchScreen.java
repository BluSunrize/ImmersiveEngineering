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
import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.AutoWorkbenchContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;

public class AutoWorkbenchScreen extends IEContainerScreen<AutoWorkbenchContainer>
{
	private AutoWorkbenchTileEntity tile;

	public AutoWorkbenchScreen(AutoWorkbenchContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.ySize = 184;
	}

	@Override
	public void init()
	{
		this.buttons.clear();
		super.init();
		Slot s = container.getSlot(0);
		if(s!=null&&s.getHasStack()&&s.getStack().getItem() instanceof EngineersBlueprintItem)
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
						final int id = i;
						this.addButton(new GuiButtonItem(xx+(i%3)*18, yy+(i/3)*18, recipes[i].output.copy(), i==tile.selectedRecipe,
								btn -> {
									if(id==tile.selectedRecipe)//disable
										tile.selectedRecipe = -1;
									else
										tile.selectedRecipe = id;
									CompoundNBT message = new CompoundNBT();
									message.putInt("recipe", tile.selectedRecipe);
									ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, message));
									fullInit();
								}));
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
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, font, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/auto_workbench.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+80, guiTop+36+(46-stored), guiLeft+87, guiTop+82, 0xffb51500, 0xff600b00);
	}
}