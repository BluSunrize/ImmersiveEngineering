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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class MixerScreen extends IEContainerScreen<MixerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("mixer");

	private final MixerTileEntity tile;

	public MixerScreen(MixerContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.imageHeight = 167;
	}

	@Override
	public void init()
	{
		super.init();
		this.buttons.clear();
		this.addButton(new GuiButtonBoolean(leftPos+106, topPos+61, 30, 16, "", tile.outputAll, TEXTURE, 176, 82, 1,
				btn -> {
					CompoundTag tag = new CompoundTag();
					tile.outputAll = !btn.getState();
					tag.putBoolean("outputAll", tile.outputAll);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
					fullInit();
				}));
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<Component> tooltip = new ArrayList<>();

		if(mx >= leftPos+76&&mx <= leftPos+134&&my >= topPos+11&&my <= topPos+58)
		{
			float capacity = tile.tank.getCapacity();
			if(tile.tank.getFluidTypes()==0)
				tooltip.add(new TranslatableComponent("gui.immersiveengineering.empty"));
			else
			{

				int fluidUpToNow = 0;
				int lastY = 0;
				int myRelative = topPos+58-my;
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
		if(mx >= leftPos+158&&mx < leftPos+165&&my > topPos+22&&my < topPos+68)
			tooltip.add(new TextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(mx >= leftPos+106&&mx <= leftPos+136&&my >= topPos+61&&my <= topPos+77)
			tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"mixer.output"+(tile.outputAll?"All": "Single")));
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		transform.pushPose();
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		for(MultiblockProcess<MixerRecipe> process : tile.processQueue)
			if(process instanceof PoweredMultiblockTileEntity.MultiblockProcessInMachine)
			{
				float mod = 1-(process.processTick/(float)process.maxTicks);
				for(int slot : ((MultiblockProcessInMachine<?>)process).getInputSlots())
				{
					int h = (int)Math.max(1, mod*16);
					this.blit(transform, leftPos+24+slot%2*21, topPos+7+slot/2*18+(16-h), 176, 16-h, 2, h);
				}
			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, leftPos+158, topPos+22+(46-stored), leftPos+165, topPos+68, 0xffb51500, 0xff600b00);

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
				GuiHelper.drawRepeatedFluidSpriteGui(buffers, transform, fs, leftPos+76, topPos+58-newY, 58, newY-lastY);
				lastY = newY;
			}
		}
		buffers.endBatch();
	}
}
