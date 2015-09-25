package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityElectricLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodLight;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderMetalDevices2 implements ISimpleBlockRenderingHandler
{
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		try{
			if(metadata==BlockMetalDevices2.META_breakerSwitch)
			{
				GL11.glTranslatef(-1f,-.75f,.25f);
				GL11.glScalef(1.25f, 1.25f, 1.25f);
				GL11.glRotatef(-90, 1,0,0);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityBreakerSwitch());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices2.META_energyMeter)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityEnergyMeter());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices2.META_electricLantern)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityElectricLantern());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices2.META_floodLight)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityFloodLight());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices2.META_fluidPipe)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityFluidPipe());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDevices2.META_fluidPump)
			{
				GL11.glPushMatrix();
				GL11.glScalef(0.65f, 0.65f, 0.65f);
				GL11.glTranslatef(0, -0.5f, 0);
				IEContent.blockMetalDevice2.setBlockBounds(0, 0, 0, 1, 1, 1);
				renderer.setRenderBoundsFromBlock(IEContent.blockMetalDevice2);
				Tessellator.instance.startDrawingQuads();
				Tessellator.instance.setNormal(1.0F, 0.0F, 0.0F);
				renderer.renderFaceXPos(IEContent.blockMetalDevice2, 0, 0, 0, IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_fluidPump));
				Tessellator.instance.draw();

				Tessellator.instance.startDrawingQuads();
				Tessellator.instance.setNormal(-1.0F, 0.0F, 0.0F);
				renderer.renderFaceXNeg(IEContent.blockMetalDevice2, 0, 0, 0, IEContent.blockMetalDevice2.getIcon(2, BlockMetalDevices2.META_fluidPump));
				Tessellator.instance.draw();

				Tessellator.instance.startDrawingQuads();
				Tessellator.instance.setNormal(0.0F, 0.0F, 1.0F);
				renderer.renderFaceZPos(IEContent.blockMetalDevice2, 0, 0, 0, IEContent.blockMetalDevice2.getIcon(1, BlockMetalDevices2.META_fluidPump));
				Tessellator.instance.draw();

				Tessellator.instance.startDrawingQuads();
				Tessellator.instance.setNormal(0.0F, 0.0F, -1.0F);
				renderer.renderFaceZNeg(IEContent.blockMetalDevice2, 0, 0, 0, IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_fluidPump));
				Tessellator.instance.draw();

				Tessellator.instance.startDrawingQuads();
				Tessellator.instance.setNormal(0.0F, -1.0f, 0.0F);
				renderer.renderFaceYNeg(IEContent.blockMetalDevice2, 0, 0, 0, IEContent.blockMetalDevice2.getIcon(3, BlockMetalDevices2.META_fluidPump));
				Tessellator.instance.draw();

				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityFluidPump());
				Tessellator.instance.draw();
				GL11.glPopMatrix();
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
		if(metadata==BlockMetalDevices2.META_breakerSwitch)
		{
			TileEntityBreakerSwitch tile = (TileEntityBreakerSwitch)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices2.META_skycrateDispenser)
		{
			renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalDevices2.META_energyMeter)
		{
			TileEntityEnergyMeter tile = (TileEntityEnergyMeter)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices2.META_electricLantern)
		{
			TileEntityElectricLantern tile = (TileEntityElectricLantern)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices2.META_floodLight)
		{
			TileEntityFloodLight tile = (TileEntityFloodLight) world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
		}
		else if(metadata==BlockMetalDevices2.META_fluidPipe)
		{
//			TileEntityFluidPipe_old tile = (TileEntityFluidPipe_old)world.getTileEntity(x, y, z);
			TileEntityFluidPipe tile = (TileEntityFluidPipe)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			return true;
		}
		else if(metadata==BlockMetalDevices2.META_fluidPump)
		{
			TileEntityFluidPump tile = (TileEntityFluidPump)world.getTileEntity(x, y, z);
			if (!tile.dummy) {
				ClientUtils.handleStaticTileRenderer(tile);
				renderer.renderFaceXPos(IEContent.blockMetalDevice2, x, y, z, IEContent.blockMetalDevice2.getIcon(tile.sideConfig[5], BlockMetalDevices2.META_fluidPump));
				renderer.renderFaceXNeg(IEContent.blockMetalDevice2, x, y, z, IEContent.blockMetalDevice2.getIcon(tile.sideConfig[4], BlockMetalDevices2.META_fluidPump));
				renderer.renderFaceZPos(IEContent.blockMetalDevice2, x, y, z, IEContent.blockMetalDevice2.getIcon(tile.sideConfig[3], BlockMetalDevices2.META_fluidPump));
				renderer.renderFaceZNeg(IEContent.blockMetalDevice2, x, y, z, IEContent.blockMetalDevice2.getIcon(tile.sideConfig[2], BlockMetalDevices2.META_fluidPump));
				renderer.renderFaceYNeg(IEContent.blockMetalDevice2, x, y, z, IEContent.blockMetalDevice2.getIcon(3 + tile.sideConfig[0], BlockMetalDevices2.META_fluidPump));
			}

			return true;
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
