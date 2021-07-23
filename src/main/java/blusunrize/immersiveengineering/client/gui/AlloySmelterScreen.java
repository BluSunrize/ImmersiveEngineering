/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.blocks.stone.FurnaceLikeTileEntity;
import blusunrize.immersiveengineering.common.gui.AlloySmelterContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class AlloySmelterScreen extends IEContainerScreen<AlloySmelterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("alloy_smelter");

	private final FurnaceLikeTileEntity<?, ?>.StateView state;

	public AlloySmelterScreen(AlloySmelterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.state = container.tile.stateView;
		clearIntArray(this.state);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
		if(state.getLastBurnTime() > 0)
		{
			int h = (int)(12*(state.getBurnTime()/(float)state.getLastBurnTime()));
			this.blit(transform, leftPos+56, topPos+37+12-h, 179, 1+12-h, 9, h);
		}
		if(state.getMaxProcess() > 0)
		{
			int w = (int)(22*((state.getMaxProcess()-state.getProcess())/(float)state.getMaxProcess()));
			this.blit(transform, leftPos+84, topPos+35, 177, 14, w, 16);
		}
	}
}
