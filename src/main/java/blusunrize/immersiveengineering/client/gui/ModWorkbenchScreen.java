/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonItem;
import blusunrize.immersiveengineering.client.gui.info.BlueprintOutputArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.IESlot.AlwaysEmptySlot;
import blusunrize.immersiveengineering.common.gui.IESlot.BlueprintOutput;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ModWorkbenchScreen extends ToolModificationScreen<ModWorkbenchContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("workbench");

	private final ModWorkbenchBlockEntity workbench;

	public ModWorkbenchScreen(ModWorkbenchContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		workbench = container.tile;
		this.imageHeight = 168;
	}

	@Override
	protected void sendMessage(CompoundTag data)
	{
		PacketDistributor.sendToServer(new MessageBlockEntitySync(this.workbench.getBlockPos(), data));
	}

	@Override
	public void init()
	{
		super.init();
		Slot s = menu.getSlot(0);
		if(s.hasItem()&&s.getItem().getItem() instanceof EngineersBlueprintItem)
		{
			List<RecipeHolder<BlueprintCraftingRecipe>> recipes = BlueprintCraftingRecipe.findRecipes(
					Minecraft.getInstance().level, ItemNBTHelper.getString(s.getItem(), "blueprint")
			);
			if(recipes!=null&&!recipes.isEmpty()&&recipes.size() > ModWorkbenchContainer.OUTPUTS_PER_PAGE)
			{
				this.addRenderableWidget(new GuiButtonIE(leftPos+118, topPos+9, 18, 12, Component.empty(), TEXTURE, 176, 0, b -> {
					sendButton(1);
				}));
				this.addRenderableWidget(new GuiButtonIE(leftPos+154, topPos+9, 18, 12, Component.empty(), TEXTURE, 176, 12, b -> {
					sendButton(2);
				}));
			}
		}
	}

	private void sendButton(int i)
	{
		this.menu.clickMenuButton(this.minecraft.player, i);
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = new ArrayList<>();
		for(int i = 0; i < menu.ownSlotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput)
				areas.add(new BlueprintOutputArea((BlueprintOutput)s, leftPos, topPos));
		}
		return areas;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(int i = 0; i < menu.ownSlotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(!(s instanceof AlwaysEmptySlot)&&(!(s instanceof BlueprintOutput bo)||bo.isOnPage()))
				GuiHelper.drawSlot(graphics, leftPos+s.x, topPos+s.y, 16, 16, 0x77222222, 0x77444444, 0x77999999);
		}
	}
}