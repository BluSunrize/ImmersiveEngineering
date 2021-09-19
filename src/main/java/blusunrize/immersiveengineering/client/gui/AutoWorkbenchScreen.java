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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoWorkbenchScreen extends IEContainerScreen<AutoWorkbenchContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("auto_workbench");
	private AutoWorkbenchTileEntity tile;

	public AutoWorkbenchScreen(AutoWorkbenchContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.imageHeight = 184;
	}

	@Override
	public void init()
	{
		this.buttons.clear();
		super.init();
		Slot s = menu.getSlot(0);
		if(s!=null&&s.hasItem()&&s.getItem().getItem() instanceof EngineersBlueprintItem)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(s.getItem(), "blueprint"));
			if(recipes!=null&&recipes.length > 0)
			{
				int l = recipes.length;
				int xx = leftPos+121;
				int yy = topPos+(l > 6?59-(l-3)/3*18: l > 3?59: 68);
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
									CompoundTag message = new CompoundTag();
									message.putInt("recipe", tile.selectedRecipe);
									ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, message));
									fullInit();
								}));
					}
			}
		}
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);

		List<Component> tooltip = new ArrayList<>();
		if(mx > leftPos+80&&mx < leftPos+87&&my > topPos+36&&my < topPos+82)
			tooltip.add(new TextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}


	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, leftPos+80, topPos+36+(46-stored), leftPos+87, topPos+82, 0xffb51500, 0xff600b00);
	}
}