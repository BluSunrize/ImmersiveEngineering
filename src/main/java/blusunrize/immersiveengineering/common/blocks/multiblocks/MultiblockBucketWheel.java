package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockBucketWheel implements IMultiblock
{
	public static MultiblockBucketWheel instance = new MultiblockBucketWheel();
	static ItemStack[][][] structure = new ItemStack[7][7][1];
	static{
		for(int h=0;h<7;h++)
			for(int l=0;l<7;l++)
			{
				if((h==0||h==6) && l!=3)
					continue;
				if((l==0||l==6) && h!=3)
					continue;
				if(l==0||h==0||l==6||h==6 || ((l==1||l==5) && (h==1||h==5)) || (h==3&&l==3))
					structure[h][l][0]= new ItemStack(IEContent.blockStorage,1,7);
				else
					structure[h][l][0]= new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
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
		return 12;
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
		TileEntityBucketWheel te = new TileEntityBucketWheel();
		te.formed=true;
		te.pos=24;
		te.facing=4;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -.5D, 0.0D, -.5D, 0.0F);
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return Utils.compareToOreName(new ItemStack(b,1,meta), "blockSteel");
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
			return false;
		int startX=x;
		int startY=y;
		int startZ=z;

		//		if(world.getBlock(x,y-1,z).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(x,y-1,z)==BlockMetalDecoration.META_scaffolding
		//				&& world.getBlock(x+(side==4?2:side==5?-2:0),y-1,z+(side==2?2:side==3?-2:0)).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(x+(side==4?2:side==5?-2:0),y-1,z+(side==2?2:side==3?-2:0))==BlockMetalDecoration.META_lightEngineering)
		//		{
		//			startX = x+(side==4?2:side==5?-2:0);
		//			startZ = z+(side==2?2:side==3?-2:0);
		//			side = ForgeDirection.OPPOSITES[side];
		//		}

		for(int h=-3;h<=3;h++)
			for(int w=-3;w<=3;w++)
			{
				int xx = startX+ (side==2?w: side==3?-w: 0);
				int yy = startY+ h;
				int zz = startZ+ (side==4?w: side==5?-w: 0);

				if((h==-3||h==3) && w!=0)
					continue;
				if((w==-3||w==3) && h!=0)
					continue;
				if(w==-3||h==-3||w==3||h==3 || ((w==-2||w==2) && (h==-2||h==2)) || (h==0&&w==0))
				{
					if(!Utils.compareToOreName(new ItemStack(world.getBlock(xx,yy,zz),1,world.getBlockMetadata(xx,yy,zz)), "blockSteel"))
						return false;
					//					if(world.getBlock(xx,yy,zz)!=IEContent.blockStorage || world.getBlockMetadata(xx,yy,zz)!=7)
					//						return false;
				}
				else
				{
					if(world.getBlock(xx,yy,zz)!=IEContent.blockMetalDecoration || world.getBlockMetadata(xx,yy,zz)!=BlockMetalDecoration.META_scaffolding)
						return false;
				}
			}

		for(int h=-3;h<=3;h++)
			for(int w=-3;w<=3;w++)
			{
				int xx = startX+ (side==2?w: side==3?-w: 0);
				int yy = startY+ h;
				int zz = startZ+ (side==4?w: side==5?-w: 0);

				if((h==-3||h==3) && w!=0)
					continue;
				if((w==-3||w==3) && h!=0)
					continue;

				world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_bucketWheel, 0x3);
				if(world.getTileEntity(xx, yy, zz) instanceof TileEntityBucketWheel)
				{
					TileEntityBucketWheel tile = (TileEntityBucketWheel)world.getTileEntity(xx,yy,zz);
					tile.facing=side;
					tile.formed=true;
					tile.pos = (w+3) + (h+3)*7;
					tile.offset = new int[]{(side==2?w: side==3?-w: 0),h,(side==4?w: side==5?-w: 0)};
				}
			}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockStorage,9,7),
				new ItemStack(IEContent.blockMetalDecoration,20,BlockMetalDecoration.META_scaffolding)};
	}
}