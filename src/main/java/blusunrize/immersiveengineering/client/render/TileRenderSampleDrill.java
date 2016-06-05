package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TileRenderSampleDrill extends TileEntitySpecialRenderer<TileEntitySampleDrill>
{
	@Override
	public void renderTileEntityAt(TileEntitySampleDrill tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = tile.getWorld().getBlockState(tile.getPos());
		BlockPos blockPos = tile.getPos();
		IBakedModel model = blockRenderer.getModelFromBlockState(state, getWorld(), blockPos);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(OBJProperty.instance, new OBJState(Lists.newArrayList("drill"), true));

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		bindTexture(TextureMap.locationBlocksTexture);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		//		float rot = 360*tile.rotation-(!tile.canTurn||tile.rotation==0||tile.rotation-tile.prevRotation<4?0:tile.facing.getAxis()==Axis.X?-f:f);
		//		GlStateManager.rotate(rot, 0,0,1);

		int max = Config.getInt("coredrill_time");
		if(tile.process>0 && tile.process<max)
		{
			GL11.glRotatef( ((tile.process+partialTicks)*22.5f)%360f, 0,1,0);
			float push = tile.process/(float)max;
			if(tile.process>max/2)
				push = 1-push;
			GL11.glTranslated(0,-2.8f*push,0);
		}

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation( -.5-blockPos.getX(), -.5- blockPos.getY(),  -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		if(model instanceof ISmartBlockModel)
			model = ((ISmartBlockModel) model).handleBlockState(state);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), worldRenderer);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}
}