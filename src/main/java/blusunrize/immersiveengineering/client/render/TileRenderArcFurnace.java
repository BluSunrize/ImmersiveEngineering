package blusunrize.immersiveengineering.client.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TileRenderArcFurnace extends TileEntitySpecialRenderer<TileEntityArcFurnace>
{
	@Override
	public void renderTileEntityAt(TileEntityArcFurnace te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;
		List<String> renderedParts = null;
		for(int i=0; i<3; i++)
			if(te.getInventory()[23+i]!=null)
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
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(OBJProperty.instance, new OBJState(renderedParts, true));

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(.5, .5, .5);
		GlStateManager.rotate(te.facing==EnumFacing.NORTH?0: te.facing==EnumFacing.EAST?-90: te.facing==EnumFacing.WEST?90: 180, 0, 1, 0);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation( -.5-blockPos.getX(), -.5- blockPos.getY(),  -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		if(model instanceof ISmartBlockModel)
			model = ((ISmartBlockModel) model).handleBlockState(state);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}
}