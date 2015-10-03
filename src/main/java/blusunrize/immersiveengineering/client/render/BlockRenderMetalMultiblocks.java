package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavatorDemo;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSheetmetalTank;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSilo;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderMetalMultiblocks implements ISimpleBlockRenderingHandler
{
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		try{
			if(metadata==BlockMetalMultiblocks.META_lightningRod)
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
			else if(metadata==BlockMetalMultiblocks.META_dieselGenerator)
			{
				GL11.glTranslatef(1.5f, 1F, 1.1875f);
				GL11.glScalef(.3125f, .3125f, .3125f);
				MultiblockDieselGenerator.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_squeezer)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				renderer.setOverrideBlockTexture( ((BlockMetalMultiblocks)block).icons[metadata][0]);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.clearOverrideBlockTexture();
			}
			else if(metadata==BlockMetalMultiblocks.META_fermenter)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				renderer.setOverrideBlockTexture( ((BlockMetalMultiblocks)block).icons[metadata][0]);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.clearOverrideBlockTexture();
			}
			else if(metadata==BlockMetalMultiblocks.META_refinery)
			{
				GL11.glTranslatef(1.25f, 1.125F, 1.625f);
				GL11.glScalef(.3125f, .3125f, .3125f);
				GL11.glRotatef(180, 0, 1, 0);
				MultiblockRefinery.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_crusher)
			{
				GL11.glTranslatef(1.25f, 1.125F, 1.625f);
				GL11.glScalef(.3125f, .3125f, .3125f);
				GL11.glRotatef(180, 0, 1, 0);
				MultiblockCrusher.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_bucketWheel)
			{
				GL11.glTranslatef(1.5f, 1.125F, 1.5f);
				GL11.glScalef(.25f, .25f, .25f);
				GL11.glRotatef(180, 0, 1, 0);
				MultiblockBucketWheel.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_excavator)
			{
				GL11.glTranslatef(1.25f, 1.125F, 1.625f);
				GL11.glScalef(.25f, .25f, .25f);
				GL11.glRotatef(180, 0, 1, 0);
				MultiblockExcavatorDemo.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_arcFurnace)
			{
				GL11.glTranslatef(1.5f, 1.1875F, 1.5f);
				GL11.glScalef(.25f, .25f, .25f);
				GL11.glRotatef(90, 0, 1, 0);
				MultiblockArcFurnace.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_tank)
			{
				GL11.glTranslatef(1.5f, 1.25F, 1.5f);
				GL11.glScalef(.3125f, .3125f, .3125f);
				GL11.glRotatef(180, 0, 1, 0);
				MultiblockSheetmetalTank.instance.renderFormedStructure();
				GL11.glEnable(32826);
			}
			else if(metadata==BlockMetalMultiblocks.META_silo)
			{
				GL11.glTranslatef(1.5f, 1.25F, 1.5f);
				GL11.glScalef(.25f, .25f, .25f);
				GL11.glRotatef(180, 0, 1, 0);
				MultiblockSilo.instance.renderFormedStructure();
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
		TileEntity te = world.getTileEntity(x, y, z);
		if(metadata==BlockMetalMultiblocks.META_lightningRod)
		{
			IIcon iTop = block.getIcon(0, metadata);
			IIcon iSide = block.getIcon(2, metadata);
			int pos = 4;
			if(te instanceof TileEntityLightningRod && ((TileEntityLightningRod)te).formed )
				pos = ((TileEntityLightningRod)te).pos;
			double[][] uv = new double[6][4];
			for(int i=0; i<2; i++)
			{
				uv[i][0]=iTop.getMinU()+((iTop.getMaxU()-iTop.getMinU())/3f)*(pos%3);
				uv[i][1]=iTop.getMinU()+((iTop.getMaxU()-iTop.getMinU())/3f)*(pos%3+1);
				uv[i][2]=iTop.getMinV()+((iTop.getMaxV()-iTop.getMinV())/3f)*(pos/3);
				uv[i][3]=iTop.getMinV()+((iTop.getMaxV()-iTop.getMinV())/3f)*(pos/3+1);
			}
			for(int i=2; i<6; i++)
			{
				int off = pos==1||pos==3||pos==4||pos==5||pos==7?1: i==2||i==3?(pos==0||pos==6?0:2): (pos==0||pos==2?0:2);
				uv[i][0]=iSide.getMinU()+((iSide.getMaxU()-iSide.getMinU())/3f)*(off);
				uv[i][1]=iSide.getMinU()+((iSide.getMaxU()-iSide.getMinU())/3f)*(off+1);
				uv[i][2]=iSide.getMinV();
				uv[i][3]=iSide.getMinV()+(iSide.getMaxV()-iSide.getMinV())/3f;
			}
			return ClientUtils.drawWorldBlock(world, block, x, y, z, uv);
		}
		else if(metadata==BlockMetalMultiblocks.META_squeezer)
		{
			if(te instanceof TileEntitySqueezer && ((TileEntitySqueezer)te).formed)
			{
				if(((TileEntitySqueezer)te).pos==13)
					renderer.setRenderBounds(-1,-1,-1, 2,2,2);
				else
					return false;
			}
			else
				renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(metadata==BlockMetalMultiblocks.META_fermenter)
		{
			if(te instanceof TileEntityFermenter && ((TileEntityFermenter)te).formed)
			{
				if(((TileEntityFermenter)te).pos==13)
					renderer.setRenderBounds(-1,-1,-1, 2,2,2);
				else
					return false;
			}
			else
				renderer.setRenderBounds(0,0,0, 1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(te instanceof TileEntityDieselGenerator && ((TileEntityDieselGenerator)te).pos==31)
			ClientUtils.handleStaticTileRenderer(te);
		else if(te instanceof TileEntityRefinery && ((TileEntityRefinery)te).pos==17)
			ClientUtils.handleStaticTileRenderer(te);
		else if(te instanceof TileEntityCrusher && ((TileEntityCrusher)te).pos==17)
			ClientUtils.handleStaticTileRenderer(te);
		else if(te instanceof TileEntityExcavator && ((TileEntityExcavator)te).pos==4)
			ClientUtils.handleStaticTileRenderer(te);
		else if(te instanceof TileEntityArcFurnace && ((TileEntityArcFurnace)te).pos==62)
			ClientUtils.handleStaticTileRenderer(te);
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
