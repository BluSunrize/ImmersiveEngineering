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
import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterTileEntity;
import blusunrize.immersiveengineering.common.gui.FluidSorterContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FluidSorterScreen extends IEContainerScreen<FluidSorterContainer>
{
	public final FluidSorterTileEntity tile;
	private final PlayerInventory playerInventory;

	public FluidSorterScreen(FluidSorterContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("sorter"));
		this.tile = container.tile;
		this.playerInventory = inventoryPlayer;
		this.ySize = 244;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<ITextComponent> addLine, Consumer<ITextComponent> addGray)
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
					ItemStack stack = playerInventory.getItemStack();
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
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder builder = buffers.getBuffer(IERenderTypes.getGui(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
				if(!tile.filters[side][i].isEmpty())
				{
					TextureAtlasSprite sprite = ClientUtils.getSprite(tile.filters[side][i].getFluid().getAttributes().getStillTexture(tile.filters[side][i]));
					Rectangle2d slotArea = getSlotArea(side, i);
					int col = tile.filters[side][i].getFluid().getAttributes().getColor(tile.filters[side][i]);
					GuiHelper.drawTexturedColoredRect(
							builder, transform,
							slotArea.getX(), slotArea.getY(), slotArea.getWidth(), slotArea.getHeight(),
							(col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1,
							sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
				}
		buffers.finish();
		for(int side = 0; side < 6; side++)
		{
			int x = guiLeft+30+(side/2)*58;
			int y = guiTop+44+(side%2)*76;
			String s = I18n.format("desc.immersiveengineering.info.blockSide."+Direction.byIndex(side).toString()).substring(0, 1);
			ClientUtils.font().drawStringWithShadow(transform, s, x-(ClientUtils.font().getStringWidth(s)/2), y, 0xaacccccc);
		}
	}

	@Override
	public void init()
	{
		super.init();
		this.buttons.clear();
		for(int side = 0; side < 6; side++)
		{
			int x = guiLeft+21+(side/2)*58;
			int y = guiTop+3+(side%2)*76;
			final int sideFinal = side;
			ButtonSorter b = new ButtonSorter(x, y, 1, btn ->
			{
				tile.sortWithNBT[sideFinal] = (byte)(tile.sortWithNBT[sideFinal]==1?0: 1);

				CompoundNBT tag = new CompoundNBT();
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
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("filter_side", side);
		tag.putInt("filter_slot", slot);
		if(fluid!=null)
			tag.put("filter", fluid.writeToNBT(new CompoundNBT()));
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
	}

	protected Rectangle2d getSlotArea(int side, int i) {
		int x = guiLeft+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
		int y = guiTop+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
		return new Rectangle2d(x, y, 16, 16);
	}
}
