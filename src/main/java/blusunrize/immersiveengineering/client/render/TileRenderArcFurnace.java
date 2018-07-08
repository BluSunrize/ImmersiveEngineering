/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

public class TileRenderArcFurnace extends TileEntitySpecialRenderer<TileEntityArcFurnace>
{
	private TextureAtlasSprite hotMetal_flow = null;
	private TextureAtlasSprite hotMetal_still = null;

	@Override
	public void render(TileEntityArcFurnace te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(!te.formed||te.isDummy()||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;
		List<String> renderedParts = null;
		for(int i = 0; i < 3; i++)
			if(!te.getInventory().get(23+i).isEmpty())
			{
				if(renderedParts==null)
					renderedParts = Lists.newArrayList("electrode"+(i+1));
				else
					renderedParts.add("electrode"+(i+1));
			}
		if(renderedParts==null)
			return;
		if(te.shouldRenderAsActive())
			renderedParts.add("active");

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=IEContent.blockMetalMultiblock)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(renderedParts, true));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(.5, .5, .5);

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
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();
		if(te.pouringMetal > 0)
		{
			if(hotMetal_flow==null)
			{
				hotMetal_still = ApiUtils.getRegisterSprite(ClientUtils.mc().getTextureMapBlocks(), "immersiveengineering:blocks/fluid/hot_metal_still");
				hotMetal_flow = ApiUtils.getRegisterSprite(ClientUtils.mc().getTextureMapBlocks(), "immersiveengineering:blocks/fluid/hot_metal_flow");
			}
			GlStateManager.rotate(-te.facing.getHorizontalAngle()+180, 0, 1, 0);
			int process = 40;
			float speed = 5f;
			int pour = process-te.pouringMetal;
			Vector3f tmp = new Vector3f();
			float h = (pour > (process-speed)?((process-pour)/speed*27): pour > speed?27: (pour/speed*27))/16f;
			GlStateManager.translate(-.5f, 1.25-.6875f, 1.5f);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			GlStateManager.disableLighting();
			setLightmapDisabled(true);
			if(pour > (process-speed))
				addTranslation(tmp, worldRenderer, 0, -1.6875f+h, 0);
			if(h > 1)
			{
				addTranslation(tmp, worldRenderer, 0, -h, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .375, 0, .375, .625, 1, .625, hotMetal_flow, true);
				addTranslation(tmp, worldRenderer, 0, 1, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .375, 0, .375, .625, h-1, .625, hotMetal_flow, true);
				addTranslation(tmp, worldRenderer, 0, -1, 0);
				addTranslation(tmp, worldRenderer, 0, h, 0);
			}
			else
			{
				addTranslation(tmp, worldRenderer, 0, -h, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .375, 0, .375, .625, h, .625, hotMetal_flow, true);
				addTranslation(tmp, worldRenderer, 0, h, 0);
			}
			if(pour > (process-speed))
				addTranslation(tmp, worldRenderer, 0, 1.6875f-h, 0);
			if(pour > speed)
			{
				float h2 = (pour > (process-speed)?.625f: pour/(process-speed)*.625f);
				addTranslation(tmp, worldRenderer, 0, -1.6875f, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .125, 0, .125, .875, h2, .875, hotMetal_still, false);
				addTranslation(tmp, worldRenderer, 0, 1.6875f, 0);
			}
			worldRenderer.setTranslation(0, 0, 0);
			tessellator.draw();
			setLightmapDisabled(false);
			GlStateManager.enableLighting();
		}
		GlStateManager.popMatrix();
	}

	private void addTranslation(Vector3f tmp, BufferBuilder bb, float x, float y, float z)
	{
		tmp.translate(x, y, z);
		bb.setTranslation(tmp.x, tmp.y, tmp.z);
	}
}