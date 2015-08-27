package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
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
			if(metadata==BlockMetalDevices2.META_energyMeter)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityEnergyMeter());
				Tessellator.instance.draw();
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
