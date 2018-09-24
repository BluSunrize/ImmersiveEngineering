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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiSorter.ButtonSorter;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityFluidSorter;
import blusunrize.immersiveengineering.common.gui.ContainerFluidSorter;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.ArrayList;

public class GuiFluidSorter extends GuiIEContainerBase
{
	public TileEntityFluidSorter tile;
	InventoryPlayer playerInventory;

	public GuiFluidSorter(InventoryPlayer inventoryPlayer, TileEntityFluidSorter tile)
	{
		super(new ContainerFluidSorter(inventoryPlayer, tile));
		this.tile = tile;
		this.playerInventory = inventoryPlayer;
		this.ySize = 244;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		for(GuiButton button : this.buttonList)
		{
			if(button instanceof ButtonSorter)
				if(mx > button.x&&mx < button.x+18&&my > button.y&&my < button.y+18)
				{
					int type = ((ButtonSorter)button).type;
					String[] split = I18n.format(Lib.DESC_INFO+"filter.nbt").split("<br>");
					for(int i = 0; i < split.length; i++)
						tooltip.add((i==0?TextFormatting.WHITE: TextFormatting.GRAY)+split[i]);
				}
		}
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
				if(tile.filters[side][i]!=null)
				{
					int x = guiLeft+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
					int y = guiTop+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
					if(mx > x&&mx < x+16&&my > y&&my < y+16)
						ClientUtils.addFluidTooltip(tile.filters[side][i], tooltip, 0);
				}
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < 8; i++)
			{
				int x = guiLeft+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
				int y = guiTop+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
				if(mouseX > x&&mouseX < x+16&&mouseY > y&&mouseY < y+16)
				{
					FluidStack fs = FluidUtil.getFluidContained(playerInventory.getItemStack());
					setFluidInSlot(side, i, fs);
				}
			}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/sorter.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		for(int side = 0; side < 6; side++)
		{
			ClientUtils.bindAtlas();
			for(int i = 0; i < 8; i++)
				if(tile.filters[side][i]!=null)
				{
					TextureAtlasSprite sprite = ClientUtils.getSprite(tile.filters[side][i].getFluid().getStill(tile.filters[side][i]));
					if(sprite!=null)
					{
						int x = guiLeft+4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
						int y = guiTop+22+(side%2)*76+(i < 3?0: i > 4?36: 18);
						int col = tile.filters[side][i].getFluid().getColor(tile.filters[side][i]);
						GlStateManager.color((col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1);
						ClientUtils.drawTexturedRect(x, y, 16, 16, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
					}
				}
			int x = guiLeft+30+(side/2)*58;
			int y = guiTop+44+(side%2)*76;
			String s = I18n.format("desc.immersiveengineering.info.blockSide."+EnumFacing.byIndex(side).toString()).substring(0, 1);
			GlStateManager.enableBlend();
			ClientUtils.font().drawString(s, x-(ClientUtils.font().getStringWidth(s)/2), y, 0xaacccccc, true);
		}
		ClientUtils.bindTexture("immersiveengineering:textures/gui/sorter.png");
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		for(int side = 0; side < 6; side++)
		{
			int x = guiLeft+21+(side/2)*58;
			int y = guiTop+3+(side%2)*76;
			ButtonSorter b = new ButtonSorter(side, x, y, 1);
			b.active = this.tile.doNBT(side);
			this.buttonList.add(b);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button instanceof ButtonSorter&&FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
		{
			int side = button.id;
			this.tile.sortWithNBT[side] = (byte)(this.tile.sortWithNBT[side]==1?0: 1);

			NBTTagCompound tag = new NBTTagCompound();
			tag.setByteArray("sideConfig", this.tile.sortWithNBT);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
			this.initGui();
		}
	}

	public void setFluidInSlot(int side, int slot, FluidStack fluid)
	{
		tile.filters[side][slot] = fluid;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("filter_side", side);
		tag.setInteger("filter_slot", slot);
		if(fluid!=null)
			tag.setTag("filter", fluid.writeToNBT(new NBTTagCompound()));
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
	}
}
