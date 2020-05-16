/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class CoresampleRenderer extends TileEntityRenderer<CoresampleTileEntity>
{
	public CoresampleRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(CoresampleTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getWorldNonnull().isBlockLoaded(tile.getPos())||tile.coresample==null)
			return;

		matrixStack.push();
		GlStateManager.disableLighting();
		matrixStack.translate(.5, .54864, .52903);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.WEST?-90: tile.getFacing()==Direction.EAST?90: 0, true));
		matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), -45, true));
		ClientUtils.mc().getItemRenderer().renderItem(tile.coresample, TransformType.FIXED, combinedLightIn,
				combinedOverlayIn, matrixStack, bufferIn);
		GlStateManager.enableLighting();
		matrixStack.pop();
	}
}