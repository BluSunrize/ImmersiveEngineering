package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderMetalDevices implements ISimpleBlockRenderingHandler
{
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		try{

			if(metadata==BlockMetalDevices.META_connectorLV)
			{
				GL11.glTranslatef(-.5f, -.5F, -.5f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityConnectorLV(), 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalDevices.META_capacitorLV)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_connectorMV)
			{
				GL11.glTranslatef(-.5f, -.5F, -.5f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityConnectorMV(), 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalDevices.META_capacitorMV)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_transformer)
			{
				GL11.glTranslatef(-.5f, -.25F, -.5f);
				GL11.glScalef(.5f, .5f, .5f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityTransformer(), 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalDevices.META_relayHV)
			{
				GL11.glTranslatef(-.5f, -.5F, -.5f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityRelayHV(), 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalDevices.META_connectorHV)
			{
				GL11.glTranslatef(-.5f, -.5F, -.5f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityConnectorHV(), 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalDevices.META_capacitorHV)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_transformerHV)
			{
				GL11.glTranslatef(-.5f, -.25F, -.5f);
				GL11.glScalef(.5f, .5f, .5f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityTransformerHV(), 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalDevices.META_dynamo)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_thermoelectricGen)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_lightningRod)
			{
//				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//				renderer.setRenderBoundsFromBlock(block);
//				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				
				Tessellator tes = ClientUtils.tes();
				IIcon iSide = block.getIcon(2, metadata);
				IIcon iTop = block.getIcon(0, metadata);

				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

				double w = (iTop.getMaxU()-iTop.getMinU())/3f;
				double h = (iTop.getMaxV()-iTop.getMinV())/3f;
				tes.startDrawing(6);
				tes.setNormal(0.0F, -1.0F, 0.0F);
				tes.addVertexWithUV(1, 0, 1, iTop.getMinU()+w*2, iTop.getMinV()+h*2);
				tes.addVertexWithUV(1, 0, 0, iTop.getMinU()+w*2, iTop.getMinV()+h);
				tes.addVertexWithUV(0, 0, 0, iTop.getMinU()+w, iTop.getMinV()+h);
				tes.addVertexWithUV(0, 0, 1, iTop.getMinU()+w, iTop.getMinV()+h*2);
				tes.draw();
				tes.startDrawing(6);
				tes.setNormal(0.0F, 1.0F, 0.0F);
				tes.addVertexWithUV(1, 1, 1, iTop.getMinU()+w*2, iTop.getMinV()+h*2);
				tes.addVertexWithUV(1, 1, 0, iTop.getMinU()+w*2, iTop.getMinV()+h);
				tes.addVertexWithUV(0, 1, 0, iTop.getMinU()+w, iTop.getMinV()+h);
				tes.addVertexWithUV(0, 1, 1, iTop.getMinU()+w, iTop.getMinV()+h*2);
				tes.draw();


				w = (iSide.getMaxU()-iSide.getMinU())/3f;
				h = (iSide.getMaxV()-iSide.getMinV())/3f;
				tes.startDrawing(6);
				tes.setNormal(0.0F, 0.0F, -1.0F);
				tes.addVertexWithUV(0, 1, 0, iSide.getMinU()+w, iSide.getMinV()+h);
				tes.addVertexWithUV(1, 1, 0, iSide.getMinU()+w*2, iSide.getMinV()+h);
				tes.addVertexWithUV(1, 0, 0, iSide.getMinU()+w*2, iSide.getMinV());
				tes.addVertexWithUV(0, 0, 0, iSide.getMinU()+w, iSide.getMinV());
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(0.0F, 0.0F, 1.0F);
				tes.addVertexWithUV(1, 1, 1, iSide.getMinU()+w*2, iSide.getMinV()+h);
				tes.addVertexWithUV(0, 1, 1, iSide.getMinU()+w, iSide.getMinV()+h);
				tes.addVertexWithUV(0, 0, 1, iSide.getMinU()+w, iSide.getMinV());
				tes.addVertexWithUV(1, 0, 1, iSide.getMinU()+w*2, iSide.getMinV());
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(-1.0F, 0.0F, 0.0F);
				tes.addVertexWithUV(0, 1, 1, iSide.getMinU()+w*2, iSide.getMinV()+h);
				tes.addVertexWithUV(0, 1, 0, iSide.getMinU()+w, iSide.getMinV()+h);
				tes.addVertexWithUV(0, 0, 0, iSide.getMinU()+w, iSide.getMinV());
				tes.addVertexWithUV(0, 0, 1, iSide.getMinU()+w*2, iSide.getMinV());
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(1.0F, 0.0F, 0.0F);
				tes.addVertexWithUV(1, 1, 0, iSide.getMinU()+w, iSide.getMinV()+h);
				tes.addVertexWithUV(1, 1, 1, iSide.getMinU()+w*2, iSide.getMinV()+h);
				tes.addVertexWithUV(1, 0, 1, iSide.getMinU()+w*2, iSide.getMinV());
				tes.addVertexWithUV(1, 0, 0, iSide.getMinU()+w, iSide.getMinV());
				tes.draw();
			}
			else if(metadata==BlockMetalDevices.META_dieselGenerator)
			{
				GL11.glTranslatef(1.5f, 1.125F, 1.1875f);
				GL11.glScalef(.25f, .25f, .25f);
				TileEntityDieselGenerator gen = new TileEntityDieselGenerator();
				gen.pos=31;
				gen.formed=true;
				TileEntityRendererDispatcher.instance.renderTileEntityAt(gen, 0.0D, 0.0D, 0.0D, 0.0F);
				GL11.glEnable(32826);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		if(metadata==BlockMetalDevices.META_capacitorLV)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_capacitorMV)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_capacitorHV)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_dynamo)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_thermoelectricGen)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_lightningRod)
		{
			IIcon iTop = block.getIcon(0, metadata);
			IIcon iSide = block.getIcon(2, metadata);
			byte type = 4;
			if(world.getTileEntity(x,y,z) instanceof TileEntityLightningRod)
				type = ((TileEntityLightningRod)world.getTileEntity(x,y,z)).type;
			double[][] uv = new double[6][4];
			for(int i=0; i<2; i++)
			{
				uv[i][0]=iTop.getMinU()+((iTop.getMaxU()-iTop.getMinU())/3f)*(type%3);
				uv[i][1]=iTop.getMinU()+((iTop.getMaxU()-iTop.getMinU())/3f)*(type%3+1);
				uv[i][2]=iTop.getMinV()+((iTop.getMaxV()-iTop.getMinV())/3f)*(type/3);
				uv[i][3]=iTop.getMinV()+((iTop.getMaxV()-iTop.getMinV())/3f)*(type/3+1);
			}
			for(int i=2; i<6; i++)
			{
				int off = type==1||type==3||type==4||type==5||type==7?1: i==2||i==3?(type==0||type==6?0:2): (type==0||type==2?0:2);
				uv[i][0]=iSide.getMinU()+((iSide.getMaxU()-iSide.getMinU())/3f)*(off);
				uv[i][1]=iSide.getMinU()+((iSide.getMaxU()-iSide.getMinU())/3f)*(off+1);
				uv[i][2]=iSide.getMinV();
				uv[i][3]=iSide.getMinV()+(iSide.getMaxV()-iSide.getMinV())/3f;
			}
			return ClientUtils.drawWorldBlock(world, block, x, y, z, uv);
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelID)
	{
		return true;
	}
	@Override
	public int getRenderId()
	{
		return renderID;
	}

}
