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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.MultitankArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import blusunrize.immersiveengineering.common.gui.MixerContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class MixerScreen extends IEContainerScreen<MixerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("mixer");

	private final MixerTileEntity tile;

	public MixerScreen(MixerContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		this.ySize = 167;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new EnergyInfoArea(guiLeft+158, guiTop+22, tile),
				new TooltipArea(
						new Rectangle2d(guiLeft+106, guiTop+61, 30, 16),
						() -> new TranslationTextComponent(Lib.GUI_CONFIG+"mixer.output"+(tile.outputAll?"All": "Single"))
				),
				new MultitankArea(new Rectangle2d(guiLeft+76, guiTop+11, 58, 47), tile.tank)
		);
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
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		transform.push();
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

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

		buffers.finish();
	}
}
