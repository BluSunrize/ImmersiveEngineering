package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockSheetmetalTank implements IMultiblock
{
	public static MultiblockSheetmetalTank instance = new MultiblockSheetmetalTank();

	static ItemStack[][][] structure = new ItemStack[5][3][3];
	static{
		for(int h=0;h<5;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
				{
					if(h==0)
					{
						if((l==0||l==2)&&(w==0||w==2))
							structure[h][l][w]=new ItemStack(IEContent.blockWoodenDecoration,1,1);
						else if(l==1&&w==1)
							structure[h][l][w]=new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_sheetMetal);
					}
					else if(h<1||h>3 || w!=1||l!=1)
						structure[h][l][w]=new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_sheetMetal);
				}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	public boolean overwriteBlockRender(ItemStack stack)
	{
		return false;
	}
	@Override
	public float getManualScale()
	{
		return 14;
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
		TileEntitySheetmetalTank te = new TileEntitySheetmetalTank();
		te.formed=true;
		te.pos=4;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -.5D, -2.5D, -.5D, 0.0F);
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDecoration && (meta==BlockMetalDecoration.META_sheetMetal);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
		int startX = x+(f==4?1:f==5?-1: 0);
		int startY = y-1;
		int startZ = z+(f==2?1:f==3?-1: 0);
		for(int yy=0;yy<=4;yy++)
			for(int xx=-1;xx<=1;xx++)
				for(int zz=-1;zz<=1;zz++)
					if(yy==0)
					{
						if(Math.abs(xx)==1&&Math.abs(zz)==1)
						{
							if(!world.getBlock(startX+xx, startY+yy, startZ+zz).equals(IEContent.blockWoodenDecoration) || world.getBlockMetadata(startX+xx, startY+yy, startZ+zz)!=1)
								return false;
						}
						else if(xx==0&&zz==0)
							if(!world.getBlock(startX+xx, startY+yy, startZ+zz).equals(IEContent.blockMetalDecoration) || world.getBlockMetadata(startX+xx, startY+yy, startZ+zz)!=BlockMetalDecoration.META_sheetMetal)
								return false;
					}
					else 
					{
						if(yy>0&&yy<4&&xx==0&&zz==0)
						{
							if(!world.isAirBlock(startX+xx, startY+yy, startZ+zz))
								return false;
						}
						else if(!world.getBlock(startX+xx, startY+yy, startZ+zz).equals(IEContent.blockMetalDecoration) || world.getBlockMetadata(startX+xx, startY+yy, startZ+zz)!=BlockMetalDecoration.META_sheetMetal)
							return false;
					}

		for(int yy=0;yy<=4;yy++)
			for(int xx=-1;xx<=1;xx++)
				for(int zz=-1;zz<=1;zz++)
					if(yy>0|| (Math.abs(xx)==1&&Math.abs(zz)==1) || (xx==0&&zz==0))
					{
						if(yy>0&&yy<4&&xx==0&&zz==0)
							continue;
						world.setBlock(startX+xx, startY+yy, startZ+zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_tank, 0x3);
						TileEntity curr = world.getTileEntity(startX+xx, startY+yy, startZ+zz);
						if(curr instanceof TileEntitySheetmetalTank)
						{
							TileEntitySheetmetalTank currTank = (TileEntitySheetmetalTank) curr;
							currTank.pos=(yy)*9 + (xx+1)*3 + (zz+1);
							currTank.formed=true;
							currTank.offset=new int[]{xx,yy,zz};
							currTank.markDirty();
						}
					}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{new ItemStack(IEContent.blockWoodenDecoration,4,1),new ItemStack(IEContent.blockMetalDecoration,34,BlockMetalDecoration.META_sheetMetal)};
	}
}