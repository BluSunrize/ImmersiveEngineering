package blusunrize.immersiveengineering.common.blocks.multiblocks;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalPress;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MultiblockMetalPress implements IMultiblock
{
	public static MultiblockMetalPress instance = new MultiblockMetalPress();
	static ItemStack[][][] structure = new ItemStack[3][3][1];
	private static final TileEntityMetalPress press = new TileEntityMetalPress();
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				if(h==0)
					structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
				else if(h==1)
				{
					if(l==1)
						structure[h][l][0] = new ItemStack(Blocks.piston,1,0);
					else
						structure[h][l][0] = new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_conveyorBelt);
				}
				else if(h==2&&l==1)
					structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering);
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
		if(iterator==4)
			GL11.glRotatef(-180, 1, 0, 0);
		if(iterator==3||iterator==5)
			GL11.glRotatef(90, 0, 1, 0);
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
		ClientUtils.bindAtlas(0);
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5f,-.5f,-.5f);
		ClientUtils.handleStaticTileRenderer(press, false);
		ClientUtils.tes().draw();
		ClientUtils.tes().setTranslation(0,0,0);
	}
	@Override
	public float getManualScale()
	{
		return 13;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:MetalPress";
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==Blocks.piston && meta==0;
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
		{
			int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
			side = f;
		}
		int startX=x;
		int startY=y;
		int startZ=z;
		int dir = ForgeDirection.ROTATION_MATRIX[0][side];
		if(world.getTileEntity(startX+(dir==4?-1:dir==5?1:0),startY,startZ+(dir==2?-1:dir==3?1:0)) instanceof TileEntityConveyorBelt)
			dir = ForgeDirection.OPPOSITES[((TileEntityConveyorBelt)world.getTileEntity(startX+(dir==4?-1:dir==5?1:0),startY,startZ+(dir==2?-1:dir==3?1:0))).facing];

		for(int l=-1;l<=1;l++)
			for(int h=-1;h<=1;h++)
			{
				if(h==1&&l!=0)
					continue;
				int xx = startX+ (dir==4?-l: dir==5?l: 0);
				int yy = startY+ h;
				int zz = startZ+ (dir==2?-l: dir==3?l: 0);

				if(h==-1)
				{
					if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_scaffolding))
						return false;
				}
				else if(h==0)
				{
					if(l==0)
					{
						if(!(world.getBlock(xx, yy, zz).equals(Blocks.piston) && world.getBlockMetadata(xx, yy, zz)==0))
							return false;
					}
					else
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDevice) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDevices.META_conveyorBelt))
							return false;
						if( ((TileEntityConveyorBelt)world.getTileEntity(xx,yy,zz)).facing!=ForgeDirection.OPPOSITES[dir] )
							return false;
					}
				}
				else 
				{
					if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_heavyEngineering))
						return false;
				}
			}

		for(int l=-1;l<=1;l++)
			for(int h=-1;h<=1;h++)
			{
				if(h==1&&l!=0)
					continue;
				int xx = startX+ (dir==4?-l: dir==5?l: 0);
				int yy = startY+ h;
				int zz = startZ+ (dir==2?-l: dir==3?l: 0);

				world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_metalPress, 0x3);
				TileEntity curr = world.getTileEntity(xx, yy, zz);
				if(curr instanceof TileEntityMetalPress)
				{
					TileEntityMetalPress tile = (TileEntityMetalPress)curr;
					tile.facing=dir;
					tile.formed=true;
					tile.pos = (h+1)*3 + (l+1);
					tile.offset = new int[]{(dir==4?-l: dir==5?l: 0),h,(dir==2?-l: dir==3?l: 0)};
				}
			}
		//			player.triggerAchievement(IEAchievements.mbCrusher);
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockMetalDecoration,3,BlockMetalDecoration.META_scaffolding),
				new ItemStack(Blocks.piston),
				new ItemStack(IEContent.blockMetalDevice,2,BlockMetalDevices.META_conveyorBelt),
				new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering)};
	}
}