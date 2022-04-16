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
import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.gui.AutoWorkbenchContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.List;

public class AutoWorkbenchScreen extends IEContainerScreen<AutoWorkbenchContainer>
{
	private final AutoWorkbenchBlockEntity tile;

	public AutoWorkbenchScreen(AutoWorkbenchContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("auto_workbench"));
		this.tile = container.tile;
		this.imageHeight = 184;
		this.inventoryLabelY = 93;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(new EnergyInfoArea(leftPos+80, topPos+36, tile.energyStorage));
	}

	@Override
	public void init()
	{
		this.clearWidgets();
		super.init();
		Slot s = menu.getSlot(0);
		if(s!=null&&s.hasItem()&&s.getItem().getItem() instanceof EngineersBlueprintItem)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(tile.getLevel(), ItemNBTHelper.getString(s.getItem(), "blueprint"));
			if(recipes!=null&&recipes.length > 0)
			{
				int l = recipes.length;
				int xx = leftPos+121;
				int yy = topPos+(l > 6?59-(l-3)/3*18: l > 3?59: 68);
				for(int i = 0; i < l; i++)
					if(recipes[i]!=null&&!recipes[i].output.get().isEmpty())
					{
						final int id = i;
						this.addRenderableWidget(new GuiButtonItem(xx+(i%3)*18, yy+(i/3)*18, recipes[i].output.get().copy(), i==tile.selectedRecipe,
								btn -> {
									if(id==tile.selectedRecipe)//disable
										tile.selectedRecipe = -1;
									else
										tile.selectedRecipe = id;
									CompoundTag message = new CompoundTag();
									message.putInt("recipe", tile.selectedRecipe);
									ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile, message));
									fullInit();
								}));
					}
			}
		}
	}
}