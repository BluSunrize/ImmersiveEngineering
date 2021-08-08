/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.SorterScreen.ButtonSorter;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterBlockEntity;
import blusunrize.immersiveengineering.common.gui.FluidSorterContainer;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FluidSorterScreen extends IEContainerScreen<FluidSorterContainer>
{
	public final FluidSorterBlockEntity tile;

	public FluidSorterScreen(FluidSorterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("sorter"));
		this.tile = container.tile;
		this.imageHeight = 244;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
				if(tile.filters[side][i]!=null&&!tile.filters[side][i].isEmpty())
					if(getSlotArea(side, i).contains(mouseX, mouseY))
						FluidInfoArea.fillTooltip(tile.filters[side][i], 0, addLine);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
			{
				if(getSlotArea(side, i).contains((int) mouseX, (int) mouseY))
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
				if(!tile.filters[side][i].isEmpty())
				{
					TextureAtlasSprite sprite = ClientUtils.getSprite(tile.filters[side][i].getFluid().getAttributes().getStillTexture(tile.filters[side][i]));
					Rect2i slotArea = getSlotArea(side, i);
					int col = tile.filters[side][i].getFluid().getAttributes().getColor(tile.filters[side][i]);
					GuiHelper.drawTexturedColoredRect(
							builder, transform,
							slotArea.getX(), slotArea.getY(), slotArea.getWidth(), slotArea.getHeight(),
							(col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1,
							sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
				}
		buffers.endBatch();
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+30+(side/2)*58;
			int y = topPos+44+(side%2)*76;
			String s = I18n.get("desc.immersiveengineering.info.blockSide."+Direction.from3DDataValue(side).toString()).substring(0, 1);
			ClientUtils.font().drawShadow(transform, s, x-(ClientUtils.font().width(s)/2), y, 0xaacccccc);
		}
	}

	@Override
	public void init()
	{
		super.init();
		this.clearWidgets();
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+21+(side/2)*58;
			int y = topPos+3+(side%2)*76;
			final int sideFinal = side;
			ButtonSorter b = new ButtonSorter(x, y, 1, btn ->
			{
				tile.sortWithNBT[sideFinal] = (byte)(tile.sortWithNBT[sideFinal]==1?0: 1);

				CompoundTag tag = new CompoundTag();
				tag.putByteArray("sideConfig", tile.sortWithNBT);
				ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile, tag));
				fullInit();
			});
			b.active = this.tile.doNBT(side);
			this.addRenderableWidget(b);
		}
	}

	public void setFluidInSlot(int side, int slot, FluidStack fluid)
	{
		tile.filters[side][slot] = fluid;
		CompoundTag tag = new CompoundTag();
		tag.putInt("filter_side", side);
		tag.putInt("filter_slot", slot);
		if(fluid!=null)
			tag.put("filter", fluid.writeToNBT(new CompoundTag()));
		ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile, tag));
	}

	protected Rect2i getSlotArea(int side, int i) {
		int x = leftPos+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
		int y = topPos+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
		return new Rect2i(x, y, 16, 16);
	}
}
