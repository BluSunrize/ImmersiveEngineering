package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
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

			if(metadata==BlockMetalDecoration.META_fence||metadata==BlockMetalDecoration.META_aluminiumFence)
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
			else if(metadata==BlockMetalDecoration.META_scaffolding||metadata==BlockMetalDecoration.META_scaffolding2||metadata==BlockMetalDecoration.META_aluminiumScaffolding||metadata==BlockMetalDecoration.META_aluminiumScaffolding2)
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
			else if(metadata==BlockMetalDecoration.META_structuralArm||metadata==BlockMetalDecoration.META_aluminiumStructuralArm)
			{
				Tessellator tes = ClientUtils.tes();
				IIcon iSide = block.getIcon(2, metadata);
				IIcon iTop = block.getIcon(0, metadata);

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
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==BlockMetalDecoration.META_fence||meta==BlockMetalDecoration.META_aluminiumFence)
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
		else if(meta==BlockMetalDecoration.META_scaffolding||meta==BlockMetalDecoration.META_scaffolding2||meta==BlockMetalDecoration.META_aluminiumScaffolding||meta==BlockMetalDecoration.META_aluminiumScaffolding2)
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
		else if(meta==BlockMetalDecoration.META_lantern)
		{
			TileEntityLantern tile = (TileEntityLantern)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			return true;
		}
		else if(meta==BlockMetalDecoration.META_structuralArm||meta==BlockMetalDecoration.META_aluminiumStructuralArm)
		{
			Tessellator tes = ClientUtils.tes();
			IIcon iSide = block.getIcon(2, meta);
			IIcon iTop = block.getIcon(0, meta);
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
			
			AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(0,0,0,1,1,1);
			ClientUtils.BlockLightingInfo[] info = 
				{
					ClientUtils.calculateBlockLighting(0, world, block, x,y,z, 1,1,1, bounds),
					ClientUtils.calculateBlockLighting(1, world, block, x,y,z, 1,1,1, bounds),
					ClientUtils.calculateBlockLighting(2, world, block, x,y,z, 1,1,1, bounds),
					ClientUtils.calculateBlockLighting(3, world, block, x,y,z, 1,1,1, bounds),
					ClientUtils.calculateBlockLighting(4, world, block, x,y,z, 1,1,1, bounds),
					ClientUtils.calculateBlockLighting(5, world, block, x,y,z, 1,1,1, bounds)
				};
			// SIDE 0
			tes.setColorOpaque_F(info[0].colorRedTopLeft, info[0].colorGreenTopLeft, info[0].colorBlueTopLeft);
			tes.setBrightness(info[0].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:0), z+1, d8, d10);
			tes.setColorOpaque_F(info[0].colorRedBottomLeft, info[0].colorGreenBottomLeft, info[0].colorBlueBottomLeft);
			tes.setBrightness(info[0].brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:0), z+0, d3, d5);
			tes.setColorOpaque_F(info[0].colorRedBottomRight, info[0].colorGreenBottomRight, info[0].colorBlueBottomRight);
			tes.setBrightness(info[0].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:0), z+0, d7, d9);
			tes.setColorOpaque_F(info[0].colorRedTopRight, info[0].colorGreenTopRight, info[0].colorBlueTopRight);
			tes.setBrightness(info[0].brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:0), z+1, d4, d6);

			tes.setColorOpaque_F(info[1].colorRedBottomLeft, info[1].colorGreenBottomLeft, info[1].colorBlueBottomLeft);
			tes.setBrightness(info[1].brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:0)+.0001, z+0, d7, d5);
			tes.setColorOpaque_F(info[1].colorRedTopLeft, info[1].colorGreenTopLeft, info[1].colorBlueTopLeft);
			tes.setBrightness(info[1].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:0)+.0001, z+1, d4, d10);
			tes.setColorOpaque_F(info[1].colorRedTopRight, info[1].colorGreenTopRight, info[1].colorBlueTopRight);
			tes.setBrightness(info[1].brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:0)+.0001, z+1, d8, d6);
			tes.setColorOpaque_F(info[1].colorRedBottomRight, info[1].colorGreenBottomRight, info[1].colorBlueBottomRight);
			tes.setBrightness(info[1].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:0)+.0001, z+0, d3, d9);

			// SIDE 1
			tes.setColorOpaque_F(info[1].colorRedTopLeft, info[1].colorGreenTopLeft, info[1].colorBlueTopLeft);
			tes.setBrightness(info[1].brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y11), z+1, d4, d6);
			tes.setColorOpaque_F(info[1].colorRedBottomLeft, info[1].colorGreenBottomLeft, info[1].colorBlueBottomLeft);
			tes.setBrightness(info[1].brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y10), z+0, d7, d9);
			tes.setColorOpaque_F(info[1].colorRedBottomRight, info[1].colorGreenBottomRight, info[1].colorBlueBottomRight);
			tes.setBrightness(info[1].brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y00), z+0, d3, d5);
			tes.setColorOpaque_F(info[1].colorRedTopRight, info[1].colorGreenTopRight, info[1].colorBlueTopRight);
			tes.setBrightness(info[1].brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y01), z+1, d8, d10);

			tes.setColorOpaque_F(info[0].colorRedBottomLeft, info[0].colorGreenBottomLeft, info[0].colorBlueBottomLeft);
			tes.setBrightness(info[0].brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y10)-.0001, z+0, d3, d9);
			tes.setColorOpaque_F(info[0].colorRedTopLeft, info[0].colorGreenTopLeft, info[0].colorBlueTopLeft);
			tes.setBrightness(info[0].brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:y11)-.0001, z+1, d8, d6);
			tes.setColorOpaque_F(info[0].colorRedTopRight, info[0].colorGreenTopRight, info[0].colorBlueTopRight);
			tes.setBrightness(info[0].brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y01)-.0001, z+1, d4, d10);
			tes.setColorOpaque_F(info[0].colorRedBottomRight, info[0].colorGreenBottomRight, info[0].colorBlueBottomRight);
			tes.setBrightness(info[0].brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+(inv?1:y00)-.0001, z+0, d7, d5);

			// SIDE 2
			tes.setColorOpaque_F(info[2].colorRedTopLeft, info[2].colorGreenTopLeft, info[2].colorBlueTopLeft);
			tes.setBrightness(info[2].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:y00), z+0, iSide.getMaxU(), iSide.getInterpolatedV((1-y00)*16));
			tes.setColorOpaque_F(info[2].colorRedBottomLeft, info[2].colorGreenBottomLeft, info[2].colorBlueBottomLeft);
			tes.setBrightness(info[2].brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:y10), z+0, iSide.getMinU(), iSide.getInterpolatedV((1-y10)*16));
			tes.setColorOpaque_F(info[2].colorRedBottomRight, info[2].colorGreenBottomRight, info[2].colorBlueBottomRight);
			tes.setBrightness(info[2].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+0, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info[2].colorRedTopRight, info[2].colorGreenTopRight, info[2].colorBlueTopRight);
			tes.setBrightness(info[2].brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+0, iSide.getMaxU(), iSide.getMaxV());

			tes.setColorOpaque_F(info[3].colorRedBottomLeft, info[3].colorGreenBottomLeft, info[3].colorBlueBottomLeft);
			tes.setBrightness(info[3].brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:y10), z+0+.0001, iSide.getMinU(), iSide.getInterpolatedV(y10*16));
			tes.setColorOpaque_F(info[3].colorRedTopLeft, info[3].colorGreenTopLeft, info[3].colorBlueTopLeft);
			tes.setBrightness(info[3].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:y00), z+0+.0001, iSide.getMaxU(), iSide.getInterpolatedV(y00*16));
			tes.setColorOpaque_F(info[3].colorRedTopRight, info[3].colorGreenTopRight, info[3].colorBlueTopRight);
			tes.setBrightness(info[3].brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+0+.0001, iSide.getMaxU(), iSide.getMinV());
			tes.setColorOpaque_F(info[3].colorRedBottomRight, info[3].colorGreenBottomRight, info[3].colorBlueBottomRight);
			tes.setBrightness(info[3].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+0+.0001, iSide.getMinU(), iSide.getMinV());

			// SIDE 3
			tes.setColorOpaque_F(info[3].colorRedTopLeft, info[3].colorGreenTopLeft, info[3].colorBlueTopLeft);
			tes.setBrightness(info[3].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:y01), z+1, iSide.getMinU(), iSide.getInterpolatedV((1-y01)*16));
			tes.setColorOpaque_F(info[3].colorRedBottomLeft, info[3].colorGreenBottomLeft, info[3].colorBlueBottomLeft);
			tes.setBrightness(info[3].brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+1, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info[3].colorRedBottomRight, info[3].colorGreenBottomRight, info[3].colorBlueBottomRight);
			tes.setBrightness(info[3].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+1, iSide.getMaxU(), iSide.getMaxV());
			tes.setColorOpaque_F(info[3].colorRedTopRight, info[3].colorGreenTopRight, info[3].colorBlueTopRight);
			tes.setBrightness(info[3].brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:y11), z+1, iSide.getMaxU(), iSide.getInterpolatedV((1-y11)*16));

			tes.setColorOpaque_F(info[2].colorRedBottomLeft, info[2].colorGreenBottomLeft, info[2].colorBlueBottomLeft);
			tes.setBrightness(info[2].brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+1-.0001, iSide.getMinU(), iSide.getMinV());
			tes.setColorOpaque_F(info[2].colorRedTopLeft, info[2].colorGreenTopLeft, info[2].colorBlueTopLeft);
			tes.setBrightness(info[2].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:y01), z+1-.0001, iSide.getMinU(), iSide.getInterpolatedV(y01*16));
			tes.setColorOpaque_F(info[2].colorRedTopRight, info[2].colorGreenTopRight, info[2].colorBlueTopRight);
			tes.setBrightness(info[2].brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:y11), z+1-.0001, iSide.getMaxU(), iSide.getInterpolatedV(y11*16));
			tes.setColorOpaque_F(info[2].colorRedBottomRight, info[2].colorGreenBottomRight, info[2].colorBlueBottomRight);
			tes.setBrightness(info[2].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+1-.0001, iSide.getMaxU(), iSide.getMinV());

			// SIDE 4
			tes.setColorOpaque_F(info[4].colorRedTopLeft, info[4].colorGreenTopLeft, info[4].colorBlueTopLeft);
			tes.setBrightness(info[4].brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y01:y01), z+1, iSide.getMaxU(), iSide.getInterpolatedV((1-y01)*16));
			tes.setColorOpaque_F(info[4].colorRedBottomLeft, info[4].colorGreenBottomLeft, info[4].colorBlueBottomLeft);
			tes.setBrightness(info[4].brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+(inv?1-y00:y00), z+0, iSide.getMinU(), iSide.getInterpolatedV((1-y00)*16));
			tes.setColorOpaque_F(info[4].colorRedBottomRight, info[4].colorGreenBottomRight, info[4].colorBlueBottomRight);
			tes.setBrightness(info[4].brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+0, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info[4].colorRedTopRight, info[4].colorGreenTopRight, info[4].colorBlueTopRight);
			tes.setBrightness(info[4].brightnessTopRight);
			tes.addVertexWithUV(x+0, y+(inv?1:0), z+1, iSide.getMaxU(), iSide.getMaxV());

			tes.setColorOpaque_F(info[5].colorRedBottomLeft, info[5].colorGreenBottomLeft, info[5].colorBlueBottomLeft);
			tes.setBrightness(info[5].brightnessBottomLeft);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1-y00:y00), z+0, iSide.getMinU(), iSide.getInterpolatedV(y00*16));
			tes.setColorOpaque_F(info[5].colorRedTopLeft, info[5].colorGreenTopLeft, info[5].colorBlueTopLeft);
			tes.setBrightness(info[5].brightnessTopLeft);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1-y01:y01), z+1, iSide.getMaxU(), iSide.getInterpolatedV(y01*16));
			tes.setColorOpaque_F(info[5].colorRedTopRight, info[5].colorGreenTopRight, info[5].colorBlueTopRight);
			tes.setBrightness(info[5].brightnessTopRight);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1:0), z+1, iSide.getMaxU(), iSide.getMinV());
			tes.setColorOpaque_F(info[5].colorRedBottomRight, info[5].colorGreenBottomRight, info[5].colorBlueBottomRight);
			tes.setBrightness(info[5].brightnessBottomRight);
			tes.addVertexWithUV(x+0+.0001, y+(inv?1:0), z+0, iSide.getMinU(), iSide.getMinV());

			// SIDE 5
			tes.setColorOpaque_F(info[5].colorRedTopLeft, info[5].colorGreenTopLeft, info[5].colorBlueTopLeft);
			tes.setBrightness(info[5].brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+1, iSide.getMinU(), iSide.getMaxV());
			tes.setColorOpaque_F(info[5].colorRedBottomLeft, info[5].colorGreenBottomLeft, info[5].colorBlueBottomLeft);
			tes.setBrightness(info[5].brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+(inv?1:0), z+0, iSide.getMaxU(), iSide.getMaxV());
			tes.setColorOpaque_F(info[5].colorRedBottomRight, info[5].colorGreenBottomRight, info[5].colorBlueBottomRight);
			tes.setBrightness(info[5].brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y10:y10), z+0, iSide.getMaxU(), iSide.getInterpolatedV((1-y10)*16));
			tes.setColorOpaque_F(info[5].colorRedTopRight, info[5].colorGreenTopRight, info[5].colorBlueTopRight);
			tes.setBrightness(info[5].brightnessTopRight);
			tes.addVertexWithUV(x+1, y+(inv?1-y11:y11), z+1, iSide.getMinU(), iSide.getInterpolatedV((1-y11)*16));

			tes.setColorOpaque_F(info[4].colorRedBottomLeft, info[4].colorGreenBottomLeft, info[4].colorBlueBottomLeft);
			tes.setBrightness(info[4].brightnessBottomLeft);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1:0), z+0, iSide.getMaxU(), iSide.getMinV());
			tes.setColorOpaque_F(info[4].colorRedTopLeft, info[4].colorGreenTopLeft, info[4].colorBlueTopLeft);
			tes.setBrightness(info[4].brightnessTopLeft);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1:0), z+1, iSide.getMinU(), iSide.getMinV());
			tes.setColorOpaque_F(info[4].colorRedTopRight, info[4].colorGreenTopRight, info[4].colorBlueTopRight);
			tes.setBrightness(info[4].brightnessTopRight);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1-y11:y11), z+1, iSide.getMinU(), iSide.getInterpolatedV(y11*16));
			tes.setColorOpaque_F(info[4].colorRedBottomRight, info[4].colorGreenBottomRight, info[4].colorBlueBottomRight);
			tes.setBrightness(info[4].brightnessBottomRight);
			tes.addVertexWithUV(x+1-.0001, y+(inv?1-y10:y10), z+0, iSide.getMaxU(), iSide.getInterpolatedV(y10*16));

			return true;
		}
		else if(meta==BlockMetalDecoration.META_connectorStructural)
		{
			TileEntityConnectorStructural tile = (TileEntityConnectorStructural)world.getTileEntity(x, y, z);
			ClientUtils.handleStaticTileRenderer(tile);
			ClientUtils.renderAttachedConnections(tile);
			return true;
		}
		else if(meta==BlockMetalDecoration.META_wallMount)
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