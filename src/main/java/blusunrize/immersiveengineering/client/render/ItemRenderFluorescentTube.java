/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.ItemFluorescentTube;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class ItemRenderFluorescentTube extends TileEntityItemStackRenderer
{
	@Override
	public void renderByItem(ItemStack stack, float partialTicks)
	{
		Tessellator tes = ClientUtils.tes();
		EntityRenderFluorescentTube.drawTube(ItemFluorescentTube.isLit(stack), ItemFluorescentTube.getRGB(stack), .5,
				tes.getBuffer(), tes);
	}
}
