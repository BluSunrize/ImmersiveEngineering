package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockFermenter implements IMultiblock
{
	public static MultiblockFermenter instance = new MultiblockFermenter();
	static ItemStack[][][] structure = new ItemStack[3][3][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
					structure[h][l][w]=(h==1&&(w!=1||l!=1))?new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_fermenter): new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering);
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		RenderBlocks rb = RenderBlocks.getInstance();
		Tessellator.instance.startDrawingQuads();
		rb.setRenderBounds(-1.5,-1.5,-1.5, 1.5,1.5,1.5);
		BlockIEBase b = IEContent.blockMetalMultiblocks;
		ClientUtils.bindAtlas(0);
		rb.renderFaceYNeg(b, 0,0,0, b.icons[3][1]);
		rb.renderFaceYPos(b, 0,0,0, b.icons[3][1]);
		rb.renderFaceXNeg(b, 0,0,0, b.icons[3][2]);
		rb.renderFaceXPos(b, 0,0,0, b.icons[3][2]);
		rb.renderFaceZNeg(b, 0,0,0, b.icons[3][3]);
		rb.renderFaceZPos(b, 0,0,0, b.icons[3][3]);
		Tessellator.instance.draw();
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Fermenter";
	}
	
	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalMultiblocks && (meta==BlockMetalMultiblocks.META_fermenter);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
			return false;

		int startX=x;
		int startY=y;
		int startZ=z;

		for(int l=0;l<3;l++)
			for(int w=-1;w<=1;w++)
				for(int h=-1;h<=1;h++)
				{
					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);
					if(h==0&&(w!=0||l!=1))
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalMultiblocks) && world.getBlockMetadata(xx, yy, zz)==BlockMetalMultiblocks.META_fermenter))
							return false;
					}
					else
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_lightEngineering))
							return false;
					}
				}

		for(int l=0;l<3;l++)
			for(int w=-1;w<=1;w++)
				for(int h=-1;h<=1;h++)
				{
					int xx = (side==4?l: side==5?-l: side==2?-w : w);
					int yy = h;
					int zz = (side==2?l: side==3?-l: side==5?-w : w);

					if(h==0&&(w!=0||l!=1))
						world.markBlockForUpdate(startX+xx, startY+yy, startZ+zz);
					else
						world.setBlock(startX+xx, startY+yy, startZ+zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_fermenter, 0x3);
					TileEntity curr = world.getTileEntity(startX+xx, startY+yy, startZ+zz);
					if(curr instanceof TileEntityFermenter)
					{
						TileEntityFermenter tile = (TileEntityFermenter)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = l*9 + (h+1)*3 + (w+1);
						tile.offset = new int[]{(side==4?l-1: side==5?1-l: side==2?-w: w),h,(side==2?l-1: side==3?1-l: side==5?-w: w)};
					}
				}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{new ItemStack(IEContent.blockMetalDecoration,19,BlockMetalDecoration.META_lightEngineering),new ItemStack(IEContent.blockMetalMultiblocks,8,BlockMetalMultiblocks.META_fermenter)};
	}
}