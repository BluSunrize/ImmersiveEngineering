/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.SorterScreen.ButtonSorter;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.gui.FluidSorterMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FluidSorterScreen extends IEContainerScreen<FluidSorterMenu>
{
	private final List<ButtonSorter> sorterButtons = new ArrayList<>(6);

	public FluidSorterScreen(FluidSorterMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("sorter"));
		this.imageHeight = 244;
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
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
			{
				if(getSlotArea(side, i).contains((int)mouseX, (int)mouseY))
				{
					ItemStack stack = menu.getCarried();
					if(stack.isEmpty())
						setFluidInSlot(side, i, FluidStack.EMPTY);
					else
					{
						int finalSide = side;
						int finalI = i;
						FluidUtil.getFluidContained(stack)
								.ifPresent(fs -> setFluidInSlot(finalSide, finalI, fs));
					}
					return true;
				}
			}
		return false;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
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
					GuiHelper.drawTexturedColoredRect(
							builder, transform,
							slotArea.getX(), slotArea.getY(), slotArea.getWidth(), slotArea.getHeight(),
							(col>>16&255)/255.0f, (col>>8&255)/255.0f, (col&255)/255.0f, 1,
							sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
				}
			}
		buffers.endBatch();
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+30+(side/2)*58;
			int y = topPos+44+(side%2)*76;
			String s = I18n.get("desc.immersiveengineering.info.blockSide."+Direction.from3DDataValue(side)).substring(0, 1);
			ClientUtils.font().drawShadow(transform, s, x-(ClientUtils.font().width(s)/2), y, 0xaacccccc);
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
			ButtonSorter b = new ButtonSorter(x, y, 1, btn ->
			{
				CompoundTag tag = new CompoundTag();
				tag.putInt("useNBT", 1-menu.sortWithNBT.get()[sideFinal]);
				tag.putInt("side", sideFinal);
				sendUpdateToServer(tag);
				fullInit();
			});
			this.sorterButtons.add(b);
			this.addRenderableWidget(b);
		}
	}

	@Override
	protected void drawBackgroundTexture(PoseStack transform)
	{
		for(int i = 0; i < DirectionUtils.VALUES.length; ++i)
			sorterButtons.get(i).active = menu.sortWithNBT.get()[i] > 0;
		super.drawBackgroundTexture(transform);
	}

	public void setFluidInSlot(int side, int slot, FluidStack fluid)
	{
		CompoundTag tag = new CompoundTag();
		tag.putInt("filter_side", side);
		tag.putInt("filter_slot", slot);
		if(fluid!=null)
			tag.put("filter", fluid.writeToNBT(new CompoundTag()));
		sendUpdateToServer(tag);
	}

	protected Rect2i getSlotArea(int side, int i)
	{
		int x = leftPos+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
		int y = topPos+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
		return new Rect2i(x, y, 16, 16);
	}
}
