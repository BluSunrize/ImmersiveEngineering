/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

public class SampleDrillRenderer extends TileEntityRenderer<SampleDrillTileEntity>
{
	private final DynamicModel<Void> drill = DynamicModel.createSimple(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_device/core_drill_center.obj"),
			"sample_drill", ModelType.OBJ
	);

	public SampleDrillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}


	@Override
	public void render(SampleDrillTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
		BlockPos blockPos = tile.getPos();
		IBakedModel model = drill.get(null);
		if(state.getBlock()!=MetalDevices.sampleDrill)
			return;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		int max = IEConfig.MACHINES.coredrill_time.get();
		if(tile.process > 0&&tile.process < max)
		{
			matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), ((tile.process+partialTicks)*22.5f)%360f, true));
			float push = tile.process/(float)max;
			if(tile.process > max/2)
				push = 1-push;
			matrixStack.translate(0, -2.8f*push, 0);
		}

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		matrixStack.translate(-0.5, -0.5, -0.5);
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorldNonnull(), model, state, tile.getPos(), matrixStack,
				bufferIn.getBuffer(RenderType.getSolid()), true, tile.getWorld().rand, 0, combinedOverlayIn, EmptyModelData.INSTANCE);
		tessellator.draw();
		matrixStack.pop();
		RenderHelper.enableStandardItemLighting();
	}
}