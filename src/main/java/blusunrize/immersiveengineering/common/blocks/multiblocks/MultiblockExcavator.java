package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockExcavator implements IMultiblock
{
	public static MultiblockExcavator instance = new MultiblockExcavator();
	
	static ItemStack[][][] structure = new ItemStack[3][6][3];
	static{
		for(int l=0;l<6;l++)
			for(int w=0;w<3;w++)
				for(int h=0;h<3;h++)
				{
					if(l<5&&w==1)
						continue;
					if((l==2||l==0) && w==0 && h==2)
						continue;
					int m = -1;
					if(l==5)
					{
						if(w!=0 && (h!=1||w!=1))
							structure[h][l][w]=new ItemStack(IEContent.blockStorage,1,7);
						else
							m = h==1&&w==0?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_heavyEngineering;
					}
					else if(l==4)
					{
						if(w==0 && h==1)
							structure[h][l][w]=new ItemStack(IEContent.blockStorage,1,7);
						else
							m = w==2?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_heavyEngineering;
					}
					else if(l==3)
						structure[h][l][w]=new ItemStack(IEContent.blockStorage,1,7);
					else if(w==2)
						m = h<2?BlockMetalDecoration.META_heavyEngineering: BlockMetalDecoration.META_scaffolding;
					else if(l!=1)
						m = h==0?BlockMetalDecoration.META_heavyEngineering: BlockMetalDecoration.META_lightEngineering;
					else
						m = BlockMetalDecoration.META_lightEngineering;
					if(m>=0)
						structure[h][l][w]= new ItemStack(IEContent.blockMetalDecoration,1,m);
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
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		TileEntityExcavator te = new TileEntityExcavator();
		te.formed=true;
		te.pos=4;
		te.facing=3;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -.5D, -.5D, 2.5D, 0.0F);
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDecoration && meta==BlockMetalDecoration.META_heavyEngineering;
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

		boolean mirrored = false;
		boolean b = structureCheck(world,startX,startY,startZ, side, mirrored);
		if(!b)
		{
			mirrored = true;
			b = structureCheck(world,startX,startY,startZ, side, mirrored);
		}

		if(b)
		{
			for(int l=0;l<6;l++)
				for(int w=-1;w<=1;w++)
					for(int h=-1;h<=1;h++)
					{
						if(l>0&&w==0)
							continue;
						if((l==3||l==5) && w==-1 && h==1)
							continue;

						int ww = mirrored?-w:w;
						int xx = startX+ (side==4?l: side==5?-l: side==2?-ww : ww);
						int yy = startY+ h;
						int zz = startZ+ (side==2?l: side==3?-l: side==5?-ww : ww);

						world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_excavator, 0x3);
						if(world.getTileEntity(xx, yy, zz) instanceof TileEntityExcavator)
						{
							TileEntityExcavator tile = (TileEntityExcavator)world.getTileEntity(xx,yy,zz);
							tile.facing=side;
							tile.formed=true;
							tile.pos = l*9 + (h+1)*3 + (w+1);
							tile.offset = new int[]{(side==4?l: side==5?-l: side==2?-ww: ww),h,(side==2?l: side==3?-l: side==5?-ww: ww)};
							tile.mirrored = mirrored;
						}
					}
			player.triggerAchievement(IEAchievements.mbExcavator);


			int wheelX = startX+ (side==4?4: side==5?-4: 0);
			int wheelZ = startZ+ (side==2?4: side==3?-4: 0);
			if(MultiblockBucketWheel.instance.isBlockTrigger(world.getBlock(wheelX,startY,wheelZ), world.getBlockMetadata(wheelX, startY, wheelZ)))
				MultiblockBucketWheel.instance.createStructure(world, wheelX, startY, wheelZ, ForgeDirection.ROTATION_MATRIX[0][side], player);

		}
		return b;
	}


	boolean structureCheck(World world, int startX, int startY, int startZ, int side, boolean mirror)
	{
		for(int l=0;l<6;l++)
			for(int w=-1;w<=1;w++)
				for(int h=-1;h<=1;h++)
				{
					if(l>0&&w==0)
						continue;
					if((l==3||l==5) && w==-1 && h==1)
						continue;

					int ww = mirror?-w:w;
					int xx = startX+ (side==4?l: side==5?-l: side==2?-ww : ww);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-ww : ww);

					if(world.isAirBlock(xx, yy, zz))
						return false;
					ItemStack checkStack = new ItemStack(world.getBlock(xx,yy,zz),1,world.getBlockMetadata(xx,yy,zz));
					if(OreDictionary.itemMatches(structure[h+1][5-l][w+1], new ItemStack(IEContent.blockStorage,1,7), true))
					{
						//Steelblocks should have OreDict checks
						if(!Utils.compareToOreName(checkStack, "blockSteel"))
							return false;
					}
					else
						if(!OreDictionary.itemMatches(structure[h+1][5-l][w+1], checkStack, true))
							return false;
				}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockStorage,12,7),
				new ItemStack(IEContent.blockMetalDecoration,9,BlockMetalDecoration.META_lightEngineering),
				new ItemStack(IEContent.blockMetalDecoration,13,BlockMetalDecoration.META_heavyEngineering),
				new ItemStack(IEContent.blockMetalDecoration,3,BlockMetalDecoration.META_scaffolding)};
	}
}