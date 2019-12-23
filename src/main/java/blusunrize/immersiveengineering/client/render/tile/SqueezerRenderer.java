/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.DynamicModelLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.SqueezerTileEntity;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class SqueezerRenderer extends TileEntityRenderer<SqueezerTileEntity>
{
	private static final Map<Direction, ModelResourceLocation> DYNAMIC_NAMES = new HashMap<>();
	private static final ResourceLocation DYNAMIC_LOC = new ResourceLocation(ImmersiveEngineering.MODID,
			"block/metal_multiblock/squeezer_piston.obj");

	static
	{
		ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/squeezer");
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			DYNAMIC_NAMES.put(d, new ModelResourceLocation(baseLoc, d.getName()));
	}

	public SqueezerRenderer()
	{
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
		{
			ConfiguredModel model = new ConfiguredModel(new ExistingModelFile(DYNAMIC_LOC), 0,
					(int)d.getHorizontalAngle()+180, false, ImmutableMap.of("flip-v", true));
			DynamicModelLoader.requestModel(model, DYNAMIC_NAMES.get(d));
		}
	}

	@Override
	public void render(SqueezerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.squeezer)
			return;
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelManager().getModel(DYNAMIC_NAMES.get(te.getFacing()));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);
		if(te.getIsMirrored())
			GlStateManager.scalef(te.getFacing().getXOffset()==0?-1: 1, 1, te.getFacing().getZOffset()==0?-1: 1);

		float piston = te.animation_piston;
		//Smoothstep! TODO partial ticks?
		piston = piston*piston*(3.0f-2.0f*piston);

		GlStateManager.translated(0, piston, 0);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-blockPos.getX(), -.5-blockPos.getY(), -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorldNonnull(), model, state, blockPos, worldRenderer, true,
				getWorld().rand, 0, EmptyModelData.INSTANCE);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}
}