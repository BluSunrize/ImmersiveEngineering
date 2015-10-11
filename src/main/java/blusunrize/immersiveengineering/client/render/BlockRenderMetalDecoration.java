package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityStructuralArm;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityWallmountMetal;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderMetalDecoration implements ISimpleBlockRenderingHandler
{
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		try{

			if(metadata==BlockMetalDecoration.META_fence)
			{
				renderer.setRenderBounds(0,0,.375, .25,1,.625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(.75,0,.375, 1,1,.625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(-.125,.8125,.4375, 1.125,.9375,.5625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(-.125,.3125,.4375, 1.125,.4375,.5625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDecoration.META_scaffolding)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==BlockMetalDecoration.META_lantern)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityLantern());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDecoration.META_structuralArm)
			{
				Tessellator tes = ClientUtils.tes();
				IIcon iSide = block.getIcon(2, 3);
				IIcon iTop = block.getIcon(0, 3);

				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

				tes.startDrawing(6);
				tes.setNormal(0.0F, -1.0F, 0.0F);
				renderer.renderFaceYNeg(block, 0,0,0,iTop);
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(0.0F,1.0F, 0.0F);
				tes.setBrightness(0xff);
				tes.addVertexWithUV(1, 1, 1, iTop.getMaxU(), iTop.getMaxV());
				tes.addVertexWithUV(1, 1, 0, iTop.getMaxU(), iTop.getMinV());
				tes.addVertexWithUV(0, 0, 0, iTop.getMinU(), iTop.getMinV());
				tes.addVertexWithUV(0, 0, 1, iTop.getMinU(), iTop.getMaxV());
				tes.draw();


				tes.startDrawing(6);
				tes.setNormal(0.0F, 0.0F, -1.0F);
				tes.addVertexWithUV(0, 0, 0, iSide.getMinU(), iSide.getInterpolatedV(0*16));
				tes.addVertexWithUV(1, 1, 0, iSide.getMaxU(), iSide.getInterpolatedV(1*16));
				tes.addVertexWithUV(1, 0, 0, iSide.getMaxU(), iSide.getMinV());
				tes.addVertexWithUV(0, 0, 0, iSide.getMinU(), iSide.getMinV());
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(0.0F, 0.0F, 1.0F);
				tes.addVertexWithUV(1, 1, 1, iSide.getMaxU(), iSide.getInterpolatedV(1*16));
				tes.addVertexWithUV(0, 0, 1, iSide.getMinU(), iSide.getInterpolatedV(0*16));
				tes.addVertexWithUV(0, 0, 1, iSide.getMinU(), iSide.getMinV());
				tes.addVertexWithUV(1, 0, 1, iSide.getMaxU(), iSide.getMinV());
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(-1.0F, 0.0F, 0.0F);
				tes.addVertexWithUV(0, 0, 1, iSide.getMaxU(), iSide.getInterpolatedV(0*16));
				tes.addVertexWithUV(0, 0, 0, iSide.getMinU(), iSide.getInterpolatedV(0*16));
				tes.addVertexWithUV(0, 0, 0, iSide.getMinU(), iSide.getMinV());
				tes.addVertexWithUV(0, 0, 1, iSide.getMaxU(), iSide.getMinV());
				tes.draw();

				tes.startDrawing(6);
				tes.setNormal(1.0F, 0.0F, 0.0F);
				tes.addVertexWithUV(1, 1, 0, iSide.getMinU(), iSide.getInterpolatedV(1*16));
				tes.addVertexWithUV(1, 1, 1, iSide.getMaxU(), iSide.getInterpolatedV(1*16));
				tes.addVertexWithUV(1, 0, 1, iSide.getMaxU(), iSide.getMinV());
				tes.addVertexWithUV(1, 0, 0, iSide.getMinU(), iSide.getMinV());
				tes.draw();
			}
			else if(metadata==BlockMetalDecoration.META_connectorStructural)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityConnectorStructural());
				Tessellator.instance.draw();
			}
			else if(metadata==BlockMetalDecoration.META_wallMount)
			{
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityWallmountMetal());
				Tessellator.instance.draw();
			}
			else
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
		if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_fence)
		{
			renderer.setRenderBounds(.375,0,.375, .625,1,.625);
			renderer.renderStandardBlock(block, x, y, z);
			BlockMetalDecoration md = (BlockMetalDecoration)block;

			if(md.canConnectFenceTo(world, x+1, y, z))
			{
				renderer.setRenderBounds(.625,.375,.4375, 1,.5625,.5625);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.625,.75,.4375, 1,.9375,.5625);
				renderer.renderStandardBlock(block, x, y, z);
			}
			if(md.canConnectFenceTo(world, x-1, y, z))
			{
				renderer.setRenderBounds(0,.375,.4375, .375,.5625,.5625);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(0,.75,.4375, .375,.9375,.5625);
				renderer.renderStandardBlock(block, x, y, z);
			}
			if(md.canConnectFenceTo(world, x, y, z+1))
			{
				renderer.setRenderBounds(.4375,.375,.625, .5625,.5625,1);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.4375,.75,.625, .5625,.9375,1);
				renderer.renderStandardBlock(block, x, y, z);
			}
			if(md.canConnectFenceTo(world, x, y, z-1))
			{
				renderer.setRenderBounds(.4375,.375,0, .5625,.5625,.375);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.4375,.75,0, .5625,.9375,.375);
				renderer.renderStandardBlock(block, x, y, z);
			}
			return true;
		}
		else if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_scaffolding)
		{
			renderer.setRenderBoundsFromBlock(block);
			float f = .015625f;
			float f1 = 0;
			renderer.renderFromInside=true;
			renderer.renderMinX+=block.shouldSideBeRendered(world,x-1,y,z,4)?f:f1;
			renderer.renderMinY+=block.shouldSideBeRendered(world,x,y-1,z,0)?f:f1;
			renderer.renderMinZ+=block.shouldSideBeRendered(world,x,y,z-1,2)?f:f1;
			renderer.renderMaxX-=block.shouldSideBeRendered(world,x+1,y,z,5)?f:f1;
			renderer.renderMaxY-=block.shouldSideBeRendered(world,x,y+1,z,1)?f:f1;
			renderer.renderMaxZ-=block.shouldSideBeRendered(world,x,y,z+1,3)?f:f1;
			renderer.renderStandardBlock(block, x, y, z);
			renderer.renderMinX-=block.shouldSideBeRendered(world,x-1,y,z,4)?f:f1;
			renderer.renderMinY-=block.shouldSideBeRendered(world,x,y-1,z,0)?f:f1;
			renderer.renderMinZ-=block.shouldSideBeRendered(world,x,y,z-1,2)?f:f1;
			renderer.renderMaxX+=block.shouldSideBeRendered(world,x+1,y,z,5)?f:f1;
			renderer.renderMaxY+=block.shouldSideBeRendered(world,x,y+1,z,1)?f:f1;
			renderer.renderMaxZ+=block.shouldSideBeRendered(world,x,y,z+1,3)?f:f1;
			renderer.renderFromInside=false;
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_lantern)
		{
			TileEntityLantern tile = (TileEntityLantern)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			return true;
		}
		else if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_structuralArm)
		{
			Tessellator tes = ClientUtils.tes();
			IIcon iSide = block.getIcon(2, 3);
			IIcon iTop = block.getIcon(0, 3);
			TileEntity te = world.getTileEntity(x, y, z);
			
			int f = (te instanceof TileEntityStructuralArm)? ((TileEntityStructuralArm)te).facing : 0;
			boolean inv = (te instanceof TileEntityStructuralArm)? ((TileEntityStructuralArm)te).inverted : false;
			ForgeDirection fd = ForgeDirection.getOrientation(f);
			int rowTop = 0;
			while(rowTop<8)
			{
				TileEntity te2 = world.getTileEntity(x-fd.offsetX*(rowTop+1),y,z-fd.offsetZ*(rowTop+1));
				if(te2 instanceof TileEntityStructuralArm && ((TileEntityStructuralArm)te2).facing==f && ((TileEntityStructuralArm)te2).inverted==inv)
					rowTop++;
				else
					break;
			}
			int rowBot = 0;
			while(rowBot<8)
			{
				TileEntity te2 = world.getTileEntity(x+fd.offsetX*(rowBot+1),y,z+fd.offsetZ*(rowBot+1));
				if(te2 instanceof TileEntityStructuralArm && ((TileEntityStructuralArm)te2).facing==f && ((TileEntityStructuralArm)te2).inverted==inv)
					rowBot++;
				else
					break;
			}
			double rowTotal = rowTop+rowBot+1;
			double yTop = 1-rowTop/rowTotal;
			double yBot = rowBot/rowTotal;

			double d3 = iTop.getInterpolatedU(0);
			double d4 = iTop.getInterpolatedU(16);
			double d5 = iTop.getInterpolatedV(0);
			double d6 = iTop.getInterpolatedV(16);
			double d7 = d4;
			double d8 = d3;
			double d9 = d5;
			double d10 = d6;

			double y11 = f==5||f==3?yBot : yTop;
			double y10 = f==5||f==2?yBot : yTop;
			double y00 = f==4||f==2?yBot : yTop;
			double y01 = f==4||f==3?yBot : yTop; 


			// SIDE 0
			ClientUtils.BlockLightingInfo info = ClientUtils.calculateBlockLighting(0, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:0), z+1, d8, d10);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:0), z+0, d3, d5);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:0), z+0, d7, d9);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:0), z+1, d4, d6);

			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:0)+.0001, z+0, d7, d5);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:0)+.0001, z+1, d4, d10);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:0)+.0001, z+1, d8, d6);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:0)+.0001, z+0, d3, d9);

			// SIDE 1
			info = ClientUtils.calculateBlockLighting(1, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y11), z+1, d4, d6);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y10), z+0, d7, d9);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y00), z+0, d3, d5);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y01), z+1, d8, d10);

			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y10)-.0001, z+0, d3, d9);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y11)-.0001, z+1, d8, d6);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y01)-.0001, z+1, d4, d10);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y00)-.0001, z+0, d7, d5);

			// SIDE 2
			info = ClientUtils.calculateBlockLighting(2, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:y00), z+0, iSide.getMaxU(), iSide.getInterpolatedV((1-y00)*16));
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:y10), z+0, iSide.getMinU(), iSide.getInterpolatedV((1-y10)*16));
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+0, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+0, iSide.getMaxU(), iSide.getMaxV());

			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:y10), z+0+.0001, iSide.getMinU(), iSide.getInterpolatedV(y10*16));
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:y00), z+0+.0001, iSide.getMaxU(), iSide.getInterpolatedV(y00*16));
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+0+.0001, iSide.getMaxU(), iSide.getMinV());
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+0+.0001, iSide.getMinU(), iSide.getMinV());

			// SIDE 3
			info = ClientUtils.calculateBlockLighting(3, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:y01), z+1, iSide.getMinU(), iSide.getInterpolatedV((1-y01)*16));
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+1, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+1, iSide.getMaxU(), iSide.getMaxV());
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:y11), z+1, iSide.getMaxU(), iSide.getInterpolatedV((1-y11)*16));

			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+1-.0001, iSide.getMinU(), iSide.getMinV());
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:y01), z+1-.0001, iSide.getMinU(), iSide.getInterpolatedV(y01*16));
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:y11), z+1-.0001, iSide.getMaxU(), iSide.getInterpolatedV(y11*16));
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+1-.0001, iSide.getMaxU(), iSide.getMinV());

			// SIDE 4
			info = ClientUtils.calculateBlockLighting(4, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:y01), z+1, iSide.getMaxU(), iSide.getInterpolatedV((1-y01)*16));
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:y00), z+0, iSide.getMinU(), iSide.getInterpolatedV((1-y00)*16));
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+0, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+1, iSide.getMaxU(), iSide.getMaxV());

			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1-y00:y00), z+0, iSide.getMinU(), iSide.getInterpolatedV(y00*16));
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1-y01:y01), z+1, iSide.getMaxU(), iSide.getInterpolatedV(y01*16));
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1:0), z+1, iSide.getMaxU(), iSide.getMinV());
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1:0), z+0, iSide.getMinU(), iSide.getMinV());

			// SIDE 5
			info = ClientUtils.calculateBlockLighting(5, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+1, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+0, iSide.getMaxU(), iSide.getMaxV());
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:y10), z+0, iSide.getMaxU(), iSide.getInterpolatedV((1-y10)*16));
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:y11), z+1, iSide.getMinU(), iSide.getInterpolatedV((1-y11)*16));

			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1:0), z+0, iSide.getMaxU(), iSide.getMinV());
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1:0), z+1, iSide.getMinU(), iSide.getMinV());
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1-y11:y11), z+1, iSide.getMinU(), iSide.getInterpolatedV(y11*16));
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1-y10:y10), z+0, iSide.getMaxU(), iSide.getInterpolatedV(y10*16));

			return true;
		}
		else if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_connectorStructural)
		{
			TileEntityConnectorStructural tile = (TileEntityConnectorStructural)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_wallMount)
		{
			TileEntityWallmountMetal tile = (TileEntityWallmountMetal)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			return true;
		}
		else
		{
			renderer.setRenderBounds(0,0,0,1,1,1);
			return renderer.renderStandardBlock(block, x, y, z);
		}
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