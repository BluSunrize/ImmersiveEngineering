package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
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
				GL11.glScalef(1.25f, 1.25f, 1.25f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityConnectorLV());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices.META_capacitorLV)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_connectorMV)
			{
				GL11.glScalef(1.25f, 1.25f, 1.25f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityConnectorMV());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices.META_capacitorMV)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_transformer)
			{
				GL11.glScalef(.5f,.5f,.5f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityTransformer());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices.META_relayHV)
			{
				GL11.glScalef(1f, 1f, 1f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityRelayHV());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices.META_connectorHV)
			{
				GL11.glScalef(1.25f, 1.25f, 1.25f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityConnectorHV());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices.META_capacitorHV)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_transformerHV)
			{
				GL11.glScalef(.5f,.5f,.5f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityTransformerHV());
				Tessellator.instance.draw();
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
			else if(metadata==BlockMetalDevices.META_conveyorBelt)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_furnaceHeater)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_sorter)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDevices.META_sampleDrill)
			{
				GL11.glScalef(.5f,.5f,.5f);
				GL11.glTranslatef(0,-1f,0);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntitySampleDrill(), 0.0D, 0.0D, 0.0D, 0.0F);
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
		if(metadata==BlockMetalDevices.META_connectorLV)
		{
			TileEntityConnectorLV tile = (TileEntityConnectorLV)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices.META_capacitorLV)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_transformer)
		{
			TileEntityTransformer tile = (TileEntityTransformer)world.getTileEntity(x, y, z);
			if(!tile.dummy)
			{
				ClientUtils.handleStaticTileRenderer(tile);
				return true;
			}
		}
		else if(metadata==BlockMetalDevices.META_connectorMV)
		{
			TileEntityConnectorMV tile = (TileEntityConnectorMV)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices.META_capacitorMV)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_relayHV)
		{
			TileEntityRelayHV tile = (TileEntityRelayHV)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices.META_connectorHV)
		{
			TileEntityConnectorHV tile = (TileEntityConnectorHV)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices.META_capacitorHV)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_transformerHV)
		{
			TileEntityTransformer tile = (TileEntityTransformer)world.getTileEntity(x, y, z);
			if(!tile.dummy)
			{
				ClientUtils.handleStaticTileRenderer(tile);
				return true;
			}
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
		else if(metadata==BlockMetalDevices.META_conveyorBelt)
		{
			renderer.enableAO = true;
			int f = 2;
			double y00=0;
			double y01=0;
			double y11=0;
			double y10=0;
			TileEntity te1 = world.getTileEntity(x, y, z);
			if(te1 instanceof TileEntityConveyorBelt)
			{
				TileEntityConveyorBelt tile = (TileEntityConveyorBelt)te1;
				renderer.uvRotateTop = tile.facing==2?0: tile.facing==3?3: tile.facing==4?2: 1;
				renderer.uvRotateBottom = tile.facing==2?3: tile.facing==3?0: tile.facing==4?2: 1;
				f = tile.facing;
				y00= tile.transportUp?(f==5||f==3?1: 0): tile.transportDown?(f==4||f==2?1: 0): 0;
				y01= tile.transportUp?(f==5||f==2?1: 0): tile.transportDown?(f==4||f==3?1: 0): 0;
				y11= tile.transportUp?(f==4||f==2?1: 0): tile.transportDown?(f==5||f==3?1: 0): 0;
				y10= tile.transportUp?(f==4||f==3?1: 0): tile.transportDown?(f==5||f==2?1: 0): 0;
			}

			boolean[] connectedBelts = new boolean[4];
			for(int i=0; i<connectedBelts.length; i++)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(2+i);
				TileEntity te = world.getTileEntity(x+fd.offsetX,y,z+fd.offsetZ);
				if(te instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)te).facing == i+2)
					connectedBelts[i] = true;
				else 
				{
					te = world.getTileEntity(x+fd.offsetX,y-1,z+fd.offsetZ);
					if(te instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)te).facing == i+2 && ((TileEntityConveyorBelt)te).transportUp)
						connectedBelts[i] = true;
					else
					{
						te = world.getTileEntity(x+fd.offsetX,y+1,z+fd.offsetZ);
						if(te instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)te).facing == i+2 && ((TileEntityConveyorBelt)te).transportDown)
							connectedBelts[i] = true;
					}
				}
			}

			Vec3[] vs = {
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[2]?0:.0625, y00+.000, f==2||f==3||connectedBelts[0]?0:.0625),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[2]?0:.0625, y01+.000, f==2||f==3||connectedBelts[1]?1:.9375),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[3]?1:.9375, y10+.000, f==2||f==3||connectedBelts[0]?0:.0625),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[3]?1:.9375, y11+.000, f==2||f==3||connectedBelts[1]?1:.9375),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[2]?0:.0625, y00+.125, f==2||f==3||connectedBelts[0]?0:.0625),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[2]?0:.0625, y01+.125, f==2||f==3||connectedBelts[1]?1:.9375),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[3]?1:.9375, y10+.125, f==2||f==3||connectedBelts[0]?0:.0625),
					Vec3.createVectorHelper(f==4||f==5||connectedBelts[3]?1:.9375, y11+.125, f==2||f==3||connectedBelts[1]?1:.9375)
			};
			ClientUtils.drawWorldSubBlock(renderer, world, block, x, y, z, vs);

			renderer.setOverrideBlockTexture(block.getIcon(f==2||f==3?2:4,BlockMetalDevices.META_conveyorBelt));
			if(f==2||f==3)
			{
				if(!connectedBelts[2])
				{
					vs = new Vec3[]{
							Vec3.createVectorHelper(0	 , y00, 0),
							Vec3.createVectorHelper(0	 , y01, 1),
							Vec3.createVectorHelper(.0625, y10, 0),
							Vec3.createVectorHelper(.0625, y11, 1),
							Vec3.createVectorHelper(0	 , y00+.1875, 0),
							Vec3.createVectorHelper(0	 , y01+.1875, 1),
							Vec3.createVectorHelper(.0625, y10+.1875, 0),
							Vec3.createVectorHelper(.0625, y11+.1875, 1)
					};
					ClientUtils.drawWorldSubBlock(renderer, world, block, x, y, z, vs);
				}
				if(!connectedBelts[3])
				{
					vs = new Vec3[]{
							Vec3.createVectorHelper(.9375, y00, 0),
							Vec3.createVectorHelper(.9375, y01, 1),
							Vec3.createVectorHelper(1	 , y10, 0),
							Vec3.createVectorHelper(1	 , y11, 1),
							Vec3.createVectorHelper(.9375, y00+.1875, 0),
							Vec3.createVectorHelper(.9375, y01+.1875, 1),
							Vec3.createVectorHelper(1	 , y10+.1875, 0),
							Vec3.createVectorHelper(1	 , y11+.1875, 1)
					};
					ClientUtils.drawWorldSubBlock(renderer, world, block, x, y, z, vs);
				}
			}
			else if(f==4||f==5)
			{
				if(!connectedBelts[0])
				{
					vs = new Vec3[]{
							Vec3.createVectorHelper(0, y00, 0),
							Vec3.createVectorHelper(0, y01, .0625),
							Vec3.createVectorHelper(1, y10, 0),
							Vec3.createVectorHelper(1, y11, .0625),
							Vec3.createVectorHelper(0, y00+.1875, 0),
							Vec3.createVectorHelper(0, y01+.1875, .0625),
							Vec3.createVectorHelper(1, y10+.1875, 0),
							Vec3.createVectorHelper(1, y11+.1875, .0625)
					};
					ClientUtils.drawWorldSubBlock(renderer, world, block, x, y, z, vs);
				}
				if(!connectedBelts[1])
				{
					vs = new Vec3[]{
							Vec3.createVectorHelper(0, y00, .9375),
							Vec3.createVectorHelper(0, y01, 1),
							Vec3.createVectorHelper(1, y10, .9375),
							Vec3.createVectorHelper(1, y11, 1),
							Vec3.createVectorHelper(0, y00+.1875, .9375),
							Vec3.createVectorHelper(0, y01+.1875, 1),
							Vec3.createVectorHelper(1, y10+.1875, .9375),
							Vec3.createVectorHelper(1, y11+.1875, 1)
					};
					ClientUtils.drawWorldSubBlock(renderer, world, block, x, y, z, vs);
				}
			}
			renderer.clearOverrideBlockTexture();
			renderer.uvRotateTop = 0;
			renderer.uvRotateBottom = 0;
			renderer.enableAO = false;
			return true;
		}
		else if(metadata==BlockMetalDevices.META_furnaceHeater)
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices.META_sorter)
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
