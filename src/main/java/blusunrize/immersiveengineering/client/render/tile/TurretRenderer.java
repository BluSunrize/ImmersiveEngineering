/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.List;

public class TurretRenderer extends TileEntityRenderer<TurretTileEntity>
{
	public TurretRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TurretTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;

		//Grab model + correct eextended state
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		BlockState state = tile.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=MetalDevices.turretChem&&state.getBlock()!=MetalDevices.turretGun)
			return;
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		//Outer GL Wrapping, initial translation
		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), tile.rotationYaw, true));
		matrixStack.rotate(new Quaternion(new Vector3f(tile.getFacing().getZOffset(), 0, -tile.getFacing().getXOffset()), tile.rotationPitch, true));

		renderModelPart(bufferIn, matrixStack, tile.getWorldNonnull(), state, model, tile.getPos(), true, combinedLightIn, "gun");
		if(tile instanceof TurretGunTileEntity)
		{
			if(((TurretGunTileEntity)tile).cycleRender > 0)
			{
				float cycle = 0;
				if(((TurretGunTileEntity)tile).cycleRender > 3)
					cycle = (5-((TurretGunTileEntity)tile).cycleRender)/2f;
				else
					cycle = ((TurretGunTileEntity)tile).cycleRender/3f;

				matrixStack.translate(-tile.getFacing().getXOffset()*cycle*.3125, 0, -tile.getFacing().getZOffset()*cycle*.3125);
			}
			renderModelPart(bufferIn, matrixStack, tile.getWorldNonnull(), state, model, tile.getPos(), false, combinedLightIn, "action");
		}

		matrixStack.pop();
	}

	public static void renderModelPart(IRenderTypeBuffer buffer, MatrixStack matrix, World world, BlockState state,
									   IBakedModel model, BlockPos pos, boolean isFirst, int light, String... parts)
	{
		pos = pos.up();

		IVertexBuilder solidBuilder = buffer.getBuffer(RenderType.getSolid());
		matrix.push();
		matrix.translate(-.5, 0, -.5);
		List<BakedQuad> quads = model.getQuads(state, null, Utils.RAND, new SinglePropertyModelData<>(
				new IEObjState(VisibilityList.show(parts)), Model.IE_OBJ_STATE));
		RenderUtils.renderModelTESRFancy(quads, new TransformingVertexBuilder(solidBuilder, matrix), world, pos, !isFirst, -1, light);
		matrix.pop();
	}

}