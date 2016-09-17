package blusunrize.immersiveengineering.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class TileRenderWatermill extends TileEntitySpecialRenderer<TileEntityWatermill>
{
	private static IBakedModel normalModel;
	@Override
	public void renderTileEntityAt(TileEntityWatermill tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = tile.getWorld().getBlockState(tile.getPos());
		BlockPos blockPos = tile.getPos();
		if (normalModel==null)
			initModel(blockRenderer.getModelForState(state), state);
		IBakedModel model = normalModel;
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
		GlStateManager.rotate(90, 1, 0, 0);

		final float dir = tile.facing == EnumFacing.NORTH ? 180 : tile.facing == EnumFacing.SOUTH ? 0 : tile.facing == EnumFacing.WEST ? 90 : -90;
		float wheelRotation = 360 * (tile.rotation + (!tile.canTurn || tile.rotation == 0 ? 0 : partialTicks)*(float)tile.perTick);
		if(tile.facing.getAxisDirection() == AxisDirection.NEGATIVE)
			wheelRotation *= -1;
		final float rot = wheelRotation;
		GlStateManager.rotate(dir, 0, 0, 1);
		GlStateManager.rotate(rot, 0, 1, 0);

//		vertexBuffer.setTranslation(x - blockPos.getX(), y - blockPos.getY(), z - blockPos.getZ());
//		final Matrix4 mat = new Matrix4();
//		mat.rotate(Math.toRadians(90), 1, 0, 0);
//		mat.rotate(Math.toRadians(dir), 0, 0, 1);
//		mat.rotate(Math.toRadians(rot), 0, 1, 0);
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

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5 - blockPos.getX(), -.5 - blockPos.getY(), -.5 - blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		SmartLightingQuad.staticBrightness = tile.getWorld().getCombinedLight(blockPos, 0);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();

		//		TileEntityWatermill wheel = (TileEntityWatermill)tile;
		//		if(wheel.offset[0]!=0||wheel.offset[1]!=0)
		//			return;
		//
		//		GL11.glPushMatrix();
		//		GL11.glTranslated(x+.5, y+.5, z+.5);
		//
		//		if(wheel.facing==4||wheel.facing==5)
		//			GL11.glRotated(90, 0, 1, 0);
		//
		//		model.setRotateAngle(model.Axle, 0, 0, (float)Math.toRadians(rot));
		//		ClientUtils.bindTexture("immersiveengineering:textures/models/watermill.png");
		//		model.render(null, 0, 0, 0, 0, 0, .0625f);
		//
		//		GL11.glPopMatrix();
	}
	private static void initModel(IBakedModel base, IBlockState state)
	{
		try {
			List<BakedQuad> b = base.getQuads(state, null, 0);
			List<BakedQuad> newQuads = new ArrayList<>();
			for (BakedQuad quad:b)
				newQuads.add(new SmartLightingQuad(quad.getVertexData(), -1, quad.getFace(), quad.getSprite(), quad.getFormat()));
			normalModel = new ModelWrapper(ImmutableList.copyOf(newQuads), base.getParticleTexture(), base.getItemCameraTransforms(), base.getOverrides());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static class ModelWrapper implements IBakedModel
	{
		List<BakedQuad> quads;
		TextureAtlasSprite particle;
		ItemCameraTransforms transform;
		ItemOverrideList overrides;
		public ModelWrapper(List<BakedQuad> q, TextureAtlasSprite p, ItemCameraTransforms t, ItemOverrideList o) {
			quads = q;
			particle = p;
			transform = t;
			overrides = o;
		}
		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return quads;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return particle;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return transform;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return overrides;
		}
		
	}
}