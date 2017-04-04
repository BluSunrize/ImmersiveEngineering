package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class TileRenderBelljar extends TileEntitySpecialRenderer<TileEntityBelljar>
{
	@Override
	public void renderTileEntityAt(TileEntityBelljar tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.dummy!=0 || !tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock() != IEContent.blockMetalDevice1)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(Arrays.asList("glass"), true));

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		VertexBuffer worldRenderer = Tessellator.getInstance().getBuffer();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, blockPos, worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		Tessellator.getInstance().draw();

		IPlantHandler plantHandler = tile.getCurrentPlantHandler();
		if(plantHandler==null)
		{
			GlStateManager.popMatrix();
			RenderHelper.enableStandardItemLighting();
			return;
		}

		GlStateManager.enableCull();
		GlStateManager.translate(0, 1.0625, 0);
		GlStateManager.rotate(-90,0,1,0);
		GlStateManager.color(1,1,1,1);
		float scale = plantHandler.getRenderSize(tile.getInventory()[1], tile.getInventory()[0], tile.renderGrowth, tile);
		GlStateManager.translate((1-scale)/2, 0, -(1-scale)/2);
		GlStateManager.scale(scale,scale,scale);
		if(!plantHandler.overrideRender(tile.getInventory()[1], tile.getInventory()[0], tile.renderGrowth, tile, blockRenderer))
		{
			IBlockState[] states = plantHandler.getRenderedPlant(tile.getInventory()[1], tile.getInventory()[0], tile.renderGrowth, tile);
			if(states==null || states.length<1)
				return;
			for(IBlockState s : states)
			{
				model = blockRenderer.getModelForState(s);
				GlStateManager.pushMatrix();
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, s, 1, true);
				GlStateManager.popMatrix();
				GlStateManager.translate(0, 1, 0);
			}
		}
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}
}