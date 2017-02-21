package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMixer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class TileRenderMixer extends TileEntitySpecialRenderer<TileEntityMixer>
{
	@Override
	public void renderTileEntityAt(TileEntityMixer te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed || te.isDummy() || !te.getWorld().isBlockLoaded(te.getPos(), false))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock() != IEContent.blockMetalMultiblock)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		if(te.mirrored)
			GlStateManager.scale(te.facing.getFrontOffsetX()==0?-1:1,1,te.facing.getFrontOffsetZ()==0?-1:1);

		GlStateManager.pushMatrix();
		GlStateManager.translate(te.facing==EnumFacing.SOUTH||te.facing==EnumFacing.WEST?-.5:.5, 0, te.facing==EnumFacing.SOUTH||te.facing==EnumFacing.EAST?.5:-.5);
		float agitator = te.animation_agitator-(!te.shouldRenderAsActive()?0:(1-partialTicks)*9f);
		GlStateManager.rotate(agitator, 0,1,0);

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
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();

		switch(te.facing)
		{
			case NORTH:
				break;
			case SOUTH:
				GlStateManager.rotate(180, 0, 1, 0);
				break;
			case WEST:
				GlStateManager.rotate(90, 0, 1, 0);
				break;
			case EAST:
				GlStateManager.rotate(-90, 0, 1, 0);
				break;
		}

		GlStateManager.scale(.0625f,1,.0625f);
		GlStateManager.rotate(90, 1,0,0);
		GlStateManager.translate(8,-8,.625f);

		RenderHelper.disableStandardItemLighting();

		for(int i=te.tank.getFluidTypes()-1; i>=0; i--)
		{
			FluidStack fs = te.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				float yy = fs.amount/(float)te.tank.getCapacity()*1.125f;
				GlStateManager.translate(0, 0, -yy);
				float w = (i<te.tank.getFluidTypes()-1||yy>=.125)?26: 16+yy/.0125f;
				ClientUtils.drawRepeatedFluidSprite(fs.getFluid(), -w/2, -w/2, w, w);
			}
		}

		GlStateManager.popMatrix();
	}
}