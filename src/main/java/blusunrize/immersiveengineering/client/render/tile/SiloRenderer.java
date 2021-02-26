/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.metal.SiloTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.items.ItemHandlerHelper;

public class SiloRenderer extends TileEntityRenderer<SiloTileEntity>
{
	public SiloRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(SiloTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		matrixStack.push();

		matrixStack.translate(.5, 0, .5);

		if(!tile.identStack.isEmpty())
		{
			matrixStack.translate(0, 5, 0);
			float baseScale = .0625f;
			float itemScale = .5f;
			float flatScale = .001f;
			float textScale = .375f*0.75f;
			matrixStack.scale(baseScale, baseScale, baseScale);
			ItemStack stack = ItemHandlerHelper.copyStackWithSize(tile.identStack, tile.storageAmount);
			String s = ""+stack.getCount();
			float w = ClientUtils.mc().fontRenderer.getStringWidth(s);

			float zz = 1.501f;
			zz /= baseScale;
			w *= textScale;
			for(int i = 0; i < 4; i++)
			{
				matrixStack.push();
				matrixStack.translate(0, 0, zz);

				matrixStack.push();
				matrixStack.scale(itemScale/baseScale, itemScale/baseScale, flatScale);
				matrixStack.translate(0, -0.75, 0);
				ClientUtils.mc().getItemRenderer().renderItem(
						stack,
						TransformType.GUI,
						combinedLightIn,
						combinedOverlayIn,
						matrixStack,
						IERenderTypes.disableLighting(bufferIn)
				);
				matrixStack.pop();

				matrixStack.push();
				matrixStack.translate(-w/2, -11, .001f);
				matrixStack.scale(textScale, -textScale, 1);
				ClientUtils.font().renderString(
						""+stack.getCount(),
						0, 0,
						0x888888,
						true,
						matrixStack.getLast().getMatrix(),
						bufferIn,
						false,
						0,
						combinedLightIn
				);
				matrixStack.pop();

				matrixStack.pop();
				matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true));
			}
		}
		matrixStack.pop();
	}

}