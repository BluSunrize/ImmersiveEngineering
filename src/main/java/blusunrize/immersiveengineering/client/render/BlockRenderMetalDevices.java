package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
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
