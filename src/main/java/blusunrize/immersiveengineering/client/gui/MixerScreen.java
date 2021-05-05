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
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import blusunrize.immersiveengineering.common.gui.MixerContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class MixerScreen extends IEContainerScreen<MixerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("mixer");

	private final MixerTileEntity tile;

	public MixerScreen(MixerContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.ySize = 167;
	}

	@Override
	public void init()
	{
		super.init();
		this.buttons.clear();
		this.addButton(new GuiButtonBoolean(guiLeft+106, guiTop+61, 30, 16, "", tile.outputAll, TEXTURE, 176, 82, 1,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					tile.outputAll = !btn.getState();
					tag.putBoolean("outputAll", tile.outputAll);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
					fullInit();
				}));
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();

		if(mx >= guiLeft+76&&mx <= guiLeft+134&&my >= guiTop+11&&my <= guiTop+58)
		{
			float capacity = tile.tank.getCapacity();
			if(tile.tank.getFluidTypes()==0)
				tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
			else
			{

				int fluidUpToNow = 0;
				int lastY = 0;
				int myRelative = guiTop+58-my;
				for(int i = tile.tank.getFluidTypes()-1; i >= 0; i--)
				{
					FluidStack fs = tile.tank.fluids.get(i);
					if(fs!=null&&fs.getFluid()!=null)
					{
						fluidUpToNow += fs.getAmount();
						int newY = (int)(47*(fluidUpToNow/capacity));
						if(myRelative >= lastY&&myRelative < newY)
						{
							GuiHelper.addFluidTooltip(fs, tooltip, (int)capacity);
							break;
						}
						lastY = newY;
					}
				}
			}
		}
		if(mx >= guiLeft+158&&mx < guiLeft+165&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(mx >= guiLeft+106&&mx <= guiLeft+136&&my >= guiTop+61&&my <= guiTop+77)
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"mixer.output"+(tile.outputAll?"All": "Single")));
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		transform.push();
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);

		for(MultiblockProcess<MixerRecipe> process : tile.processQueue)
			if(process instanceof PoweredMultiblockTileEntity.MultiblockProcessInMachine)
			{
				float mod = 1-(process.processTick/(float)process.maxTicks);
				for(int slot : ((MultiblockProcessInMachine<?>)process).getInputSlots())
				{
					int h = (int)Math.max(1, mod*16);
					this.blit(transform, guiLeft+24+slot%2*21, guiTop+7+slot/2*18+(16-h), 176, 16-h, 2, h);
				}
			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, guiLeft+158, guiTop+22+(46-stored), guiLeft+165, guiTop+68, 0xffb51500, 0xff600b00);

		float capacity = tile.tank.getCapacity();
		int fluidUpToNow = 0;
		int lastY = 0;
		for(int i = tile.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = tile.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				fluidUpToNow += fs.getAmount();
				int newY = (int)(47*(fluidUpToNow/capacity));
				GuiHelper.drawRepeatedFluidSpriteGui(buffers, transform, fs, guiLeft+76, guiTop+58-newY, 58, newY-lastY);
				lastY = newY;
			}
		}
		buffers.finish();
	}
}
