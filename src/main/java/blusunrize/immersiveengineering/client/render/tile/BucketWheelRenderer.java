/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class BucketWheelRenderer extends TileEntityRenderer<BucketWheelTileEntity>
{
	private static IBakedModel model = null;

	@Override
	public void render(BucketWheelTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!tile.formed||!tile.getWorldNonnull().isBlockLoaded(tile.getPos())||tile.isDummy())
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
		if(state.getBlock()!=Multiblocks.bucketWheel)
			return;
		if(model==null)
		{
			//TODO state = state.with(IEProperties.DYNAMICRENDER, true);
			state = state.with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
			model = blockRenderer.getModelForState(state);
		}
		OBJState objState = null;
		Map<String, String> texMap = new HashMap<>();
		List<String> list = Lists.newArrayList("bucketWheel");
		synchronized(tile.digStacks)
		{
			for(int i = 0; i < tile.digStacks.size(); i++)
				if(!tile.digStacks.get(i).isEmpty())
				{
					list.add("dig"+i);
					Block b = Block.getBlockFromItem(tile.digStacks.get(i).getItem());
					BlockState digState = b!=Blocks.AIR?b.getDefaultState(): Blocks.COBBLESTONE.getDefaultState();
					IBakedModel digModel = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(digState);
					digModel.getParticleTexture();
					texMap.put("dig"+i, digModel.getParticleTexture().getName().toString());
				}
		}
		objState = new OBJState(list, true);

		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.pushMatrix();

		GlStateManager.translated(x+.5, y+.5, z+.5);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		Direction facing = tile.getFacing();
		if(tile.isMirrored())
		{
			GlStateManager.scalef(facing.getAxis()==Axis.X?-1: 1, 1, facing.getAxis()==Axis.Z?-1: 1);
			GlStateManager.disableCull();
		}
		float dir = tile.getFacing()==Direction.SOUTH?90: tile.getFacing()==Direction.NORTH?-90: tile.getFacing()==Direction.EAST?180: 0;
		GlStateManager.rotatef(dir, 0, 1, 0);
		float rot = tile.rotation+(float)(tile.active?IEConfig.MACHINES.excavator_speed.get()*partialTicks: 0);
		GlStateManager.rotatef(rot, 1, 0, 0);

		RenderHelper.disableStandardItemLighting();
		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		IModelData modelData = new SinglePropertyModelData<>(objState, Model.OBJ_STATE);
		List<BakedQuad> quads;
		if(model instanceof IESmartObjModel)
			quads = ((IESmartObjModel)model).getQuads(state, null, 0, objState, texMap, true,
					modelData);
		else
			quads = model.getQuads(state, null, Utils.RAND, modelData);
		ClientUtils.renderModelTESRFast(quads, worldRenderer, tile.getWorldNonnull(), tile.getPos());
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		if(tile.isMirrored())
		{
			GlStateManager.enableCull();
		}
	}

	//	@Override
	//	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	//	{
	//		BucketWheelTileEntity wheel = (BucketWheelTileEntity)tile;
	//		if(!wheel.formed || wheel.pos!=24)
	//			return;
	//		GL11.glPushMatrix();
	//
	//		GlStateManager.translated(x+.5, y+.5, z+.5);
	//		GL11.glRotatef(wheel.facing==3?180: wheel.facing==5?-90: wheel.facing==4?90: 0, 0,1,0);
	//
	//		if(wheel.mirrored)
	//		{
	//			GlStateManager.scalef(1,1,-1);
	//			GL11.glDisable(GL11.GL_CULL_FACE);
	//		}
	//
	//		float rot =  wheel.rotation+(float)(wheel.active?Config.getDouble("excavator_speed")*f:0);
	//		GL11.glRotatef(rot, 0,0,-1);
	//		ClientUtils.bindTexture("immersiveengineering:textures/models/bucketWheel.png");
	//		model.renderOnly("bucketWheel");
	//
	//		for(int i=0; i<8; i++)
	//		{
	//			ItemStack stack = wheel.digStacks[i];
	//			//			String ss = ClientUtils.getResourceNameForItemStack(stack);
	//			//			if(!ss.isEmpty())
	//			if(stack==null || stack.getItem()==null)
	//				continue;
	//			IIcon ic = null;
	//			Block b = Block.getBlockFromItem(stack.getItem());
	//			if(b!=null&&b!=Blocks.air)
	//				ic = b.getIcon(2, stack.getItemDamage());
	//			else
	//				ic = stack.getIconIndex();
	//			if(ic!=null)
	//			{
	//				ClientUtils.bindAtlas(stack.getItemSpriteNumber());
	//				ClientUtils.tes().startDrawingQuads();
	//				for(GroupObject go : model.groupObjects)
	//				{
	//					if(go.name.equals("dig"+i))
	//					{
	//						for(Face face : go.faces)
	//						{
	//							float minU = ic.getMinU();
	//							float sizeU = ic.getMaxU() - minU;
	//							float minV = ic.getMinV();
	//							float sizeV = ic.getMaxV() - minV;
	//
	//							TextureCoordinate[] oldUVs = new TextureCoordinate[face.textureCoordinates.length];
	//							for(int v=0; v<face.vertices.length; ++v)
	//							{
	//								oldUVs[v] = face.textureCoordinates[v]; 
	//								TextureCoordinate textureCoordinate = face.textureCoordinates[v];
	//								face.textureCoordinates[v] = new TextureCoordinate(
	//										minU + sizeU * textureCoordinate.u,
	//										minV + sizeV * textureCoordinate.v
	//										);
	//							}
	//							face.addFaceForRender(ClientUtils.tes(),0);
	//							for(int v=0; v<face.vertices.length; ++v)
	//								face.textureCoordinates[v] = new TextureCoordinate(oldUVs[v].u,oldUVs[v].v);
	////							face.textureCoordinates = oldUVs;
	//						}
	////						go.render();
	//					}
	//				}
	//				ClientUtils.tes().draw();
	//			}
	//		}
	//
	//		if(wheel.mirrored)
	//		{
	//			GlStateManager.scalef(1,1,-1);
	//			GL11.glEnable(GL11.GL_CULL_FACE);
	//		}
	//
	//		GlStateManager.popMatrix();
	//	}

}