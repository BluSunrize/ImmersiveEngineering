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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonItem;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.AutoWorkbenchContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class AutoWorkbenchScreen extends IEContainerScreen<AutoWorkbenchContainer>
{
	private final AutoWorkbenchTileEntity tile;

	public AutoWorkbenchScreen(AutoWorkbenchContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("auto_workbench"));
		this.tile = container.tile;
		this.ySize = 184;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(new EnergyInfoArea(guiLeft+80, guiTop+36, tile));
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
}