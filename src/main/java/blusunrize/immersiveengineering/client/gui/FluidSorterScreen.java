/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.SorterScreen.ButtonSorter;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterTileEntity;
import blusunrize.immersiveengineering.common.gui.FluidSorterContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class FluidSorterScreen extends IEContainerScreen<FluidSorterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("sorter");

	public final FluidSorterTileEntity tile;
	private final Inventory playerInventory;

	public FluidSorterScreen(FluidSorterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.playerInventory = inventoryPlayer;
		this.imageHeight = 244;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<Component> tooltip = new ArrayList<>();
		for(AbstractWidget button : this.buttons)
		{
			if(button instanceof ButtonSorter)
				if(mx > button.x&&mx < button.x+18&&my > button.y&&my < button.y+18)
				{
					String[] split = I18n.get(Lib.DESC_INFO+"filter.nbt").split("<br>");
					for(int i = 0; i < split.length; i++)
					{
						Component component = new TextComponent(split[i]);
						TextUtils.applyFormat(
								component,
								i==0?ChatFormatting.WHITE: ChatFormatting.GRAY
						);
						tooltip.add(component);
					}
				}
		}
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
				if(tile.filters[side][i]!=null&&!tile.filters[side][i].isEmpty())
				{
					int x = leftPos+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
					int y = topPos+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
					if(mx > x&&mx < x+16&&my > y&&my < y+16)
						GuiHelper.addFluidTooltip(tile.filters[side][i], tooltip, 0);
				}
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
			{
				int x = leftPos+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
				int y = topPos+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
				if(mouseX > x&&mouseX < x+16&&mouseY > y&&mouseY < y+16)
				{
					ItemStack stack = inventory.getCarried();
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
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		{
			MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			VertexConsumer builder = buffers.getBuffer(IERenderTypes.getGui(InventoryMenu.BLOCK_ATLAS));
			for(int side = 0; side < 6; side++)
				for(int i = 0; i < 8; i++)
					if(!tile.filters[side][i].isEmpty())
					{
						TextureAtlasSprite sprite = ClientUtils.getSprite(tile.filters[side][i].getFluid().getAttributes().getStillTexture(tile.filters[side][i]));
						if(sprite!=null)
						{
							int x = leftPos+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
							int y = topPos+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
							int col = tile.filters[side][i].getFluid().getAttributes().getColor(tile.filters[side][i]);
							GuiHelper.drawTexturedColoredRect(
									builder, transform, x, y, 16, 16,
									(col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1,
									sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
						}
					}
			buffers.endBatch();
		}
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
		this.buttons.clear();
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
				ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
				fullInit();
			});
			b.active = this.tile.doNBT(side);
			this.addButton(b);
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
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
	}
}
