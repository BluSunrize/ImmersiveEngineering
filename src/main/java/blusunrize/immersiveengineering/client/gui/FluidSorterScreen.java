/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.SorterScreen.ButtonSorter;
import blusunrize.immersiveengineering.client.gui.SorterScreen.FilterBit;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterBlockEntity.FilterConfig;
import blusunrize.immersiveengineering.common.gui.FluidSorterMenu;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FluidSorterScreen extends IEContainerScreen<FluidSorterMenu>
{
	private final List<ButtonSorter> sorterButtons = new ArrayList<>(6);

	public FluidSorterScreen(FluidSorterMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("sorter"));
		this.imageHeight = 244;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
				if(!menu.getFilter(side, i).isEmpty())
					if(getSlotArea(side, i).contains(mouseX, mouseY))
						FluidInfoArea.fillTooltip(menu.getFilter(side, i), -1, addLine);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		final RegistryAccess registries = Minecraft.getInstance().level.registryAccess();
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
			{
				if(getSlotArea(side, i).contains((int)mouseX, (int)mouseY))
				{
					setFluidInSlot(side, i, Utils.deriveFluidStack(menu.getCarried()), registries);
					return true;
				}
			}
		return false;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		MultiBufferSource.BufferSource buffers = graphics.bufferSource();
		VertexConsumer builder = buffers.getBuffer(IERenderTypes.getGui(InventoryMenu.BLOCK_ATLAS));
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
			{
				FluidStack filter = menu.getFilter(side, i);
				if(!filter.isEmpty())
				{
					IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(filter.getFluid());
					TextureAtlasSprite sprite = ClientUtils.getSprite(props.getStillTexture(filter));
					Rect2i slotArea = getSlotArea(side, i);
					int col = props.getTintColor(filter);
					graphics.blit(
							slotArea.getX(), slotArea.getY(),
							0, slotArea.getWidth(), slotArea.getHeight(),
							sprite,
							(col>>16&255)/255.0f, (col>>8&255)/255.0f, (col&255)/255.0f, 1
					);
				}
			}
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+30+(side/2)*58;
			int y = topPos+44+(side%2)*76;
			String s = I18n.get("desc.immersiveengineering.info.blockSide."+Direction.from3DDataValue(side)).substring(0, 1);
			graphics.drawString(
					ClientUtils.font(), s, x-(ClientUtils.font().width(s)/2), y, 0xaacccccc, true
			);
		}
	}

	@Override
	public void init()
	{
		super.init();
		this.clearWidgets();
		this.sorterButtons.clear();
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+21+(side/2)*58;
			int y = topPos+3+(side%2)*76;
			final int sideFinal = side;
			final BooleanSupplier value = () -> menu.sortWithNBT.get()[sideFinal]!=0;
			ButtonSorter b = new ButtonSorter(
					x, y, FilterBit.NBT, () -> new FilterConfig(false, value.getAsBoolean(), false),
					btn -> {
						CompoundTag tag = new CompoundTag();
						tag.putInt("useNBT", value.getAsBoolean()?0: 1);
						tag.putInt("side", sideFinal);
						sendUpdateToServer(tag);
						fullInit();
					}
			);
			this.sorterButtons.add(b);
			this.addRenderableWidget(b);
		}
	}

	public void setFluidInSlot(int side, int slot, FluidStack fluid, Provider provider)
	{
		CompoundTag tag = new CompoundTag();
		tag.putInt("filter_side", side);
		tag.putInt("filter_slot", slot);
		if(fluid!=null)
			if(fluid.isEmpty())
				tag.remove("filter");
			else
				tag.put("filter", fluid.save(provider));
		sendUpdateToServer(tag);
	}

	protected Rect2i getSlotArea(int side, int i)
	{
		int x = leftPos+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
		int y = topPos+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
		return new Rect2i(x, y, 16, 16);
	}
}
