package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;

public class TileRenderWindmillAdvanced extends TileEntitySpecialRenderer<TileEntityWindmillAdvanced>
//public class TileRenderWindmillAdvanced extends FastTESR<TileEntityWindmillAdvanced>
{
	static IBakedModel staticModel;
	@Override
	public void renderTileEntityAt(TileEntityWindmillAdvanced tile, double x, double y, double z, float partialTicks, int destroyStage)
//	public void renderTileEntityFast(TileEntityWindmillAdvanced tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vertexBuffer)
	{
		if (!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		state = state.getActualState(getWorld(), blockPos);
//		IBakedModel model = blockRenderer.getModelFromBlockState(state, getWorld(), blockPos);
		if (staticModel==null)
			staticModel = ClientUtils.makeStaticBakedModel(blockRenderer.getBlockModelShapes().getModelForState(state), state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState) state).withProperty(Properties.AnimationProperty, new OBJState(Lists.newArrayList(Group.ALL), true));

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + .5, z + .5);

//		float dir = tile.facing == EnumFacing.SOUTH ? 180 : tile.facing == EnumFacing.NORTH ? 0 : tile.facing == EnumFacing.EAST ? 90 : 90;
		float rot = 360 * (tile.rotation - (!tile.canTurn || tile.rotation == 0 ? 0 : tile.facing.getAxis() == Axis.X ? -partialTicks : partialTicks)*tile.perTick);
		if(tile.facing.getAxisDirection() == AxisDirection.POSITIVE)
			rot *= -1;

		GlStateManager.rotate(180, tile.facing.getAxis() == Axis.Z ? 1 : 0, 0, tile.facing.getAxis() == Axis.X ? 1 : 0);
		GlStateManager.rotate(rot, tile.facing.getAxis() == Axis.X ? 1 : 0, 0, tile.facing.getAxis() == Axis.Z ? 1 : 0);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5 - blockPos.getX(), -.5 - blockPos.getY(), -.5 - blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		SmartLightingQuad.staticBrightness = tile.getWorld().getCombinedLight(blockPos, 0);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), staticModel, state, tile.getPos(), worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();

//		vertexBuffer.setTranslation(x - blockPos.getX(), y - blockPos.getY(), z - blockPos.getZ());
//		final Matrix4 mat = new Matrix4();
//		mat.rotate(Math.toRadians(90), 0, 0, 1);
//		mat.rotate(Math.toRadians(dir), 0, 0, 1);
//		mat.rotate(Math.toRadians(rot), tile.facing.getAxis() == Axis.X ? 1 : 0, 0, tile.facing.getAxis() == Axis.Z ? 1 : 0);
//		IVertexTransformer transformer = (quad, type, usage, data) ->
//		{
//			if(usage == EnumUsage.POSITION)
//			{
//				Vector3f pos = new Vector3f(data);
//				pos.sub(new Vector3f(0.5F, 0.5F, 0.5F));
//				mat.apply(pos);
//				pos.add(new Vector3f(0.5F, 0.5F, 0.5F));
//				pos.get(data);
//			}
//			return data;
//		};
//		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), BakedModelTransformer.transform(model, transformer, state, 0), state, tile.getPos(), vertexBuffer, true);
//		vertexBuffer.setTranslation(0, 0, 0);
	}
}