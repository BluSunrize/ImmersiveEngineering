package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockBottlingMachine implements IMultiblock
{
	public static MultiblockBottlingMachine instance = new MultiblockBottlingMachine();
	static ItemStack[][][] structure = new ItemStack[2][2][3];
	static{
		for(int h=0;h<2;h++)
			for(int l=0;l<2;l++)
				for(int w=0;w<3;w++)
				{
					if(l==0 && w==1)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering);
					else if(h==0)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
					else
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_conveyorBelt);
				}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		structure = new ItemStack[2][2][3];
		for(int h=0;h<2;h++)
			for(int l=0;l<2;l++)
				for(int w=0;w<3;w++)
				{
					if(l==0 && w==1)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering);
					else if(h==0)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
					else
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_conveyorBelt);
				}
		return structure;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		if(iterator==6||iterator==9)
			GL11.glRotatef(90, 0, 1, 0);
		if(iterator==8)
			GL11.glRotatef(-90, 0, 1, 0);
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
		TileEntityBottlingMachine te = new TileEntityBottlingMachine();
		te.pos = 4;
		te.formed=true;
		ClientUtils.bindAtlas(0);
		GL11.glRotatef(180, 0, 1, 0);
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5f,-1.5f,-.5f);
		ClientUtils.handleStaticTileRenderer(te, false);
		ClientUtils.tes().draw();
		ClientUtils.tes().setTranslation(0,0,0);
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -.5D, -1.5D, -.5D, 0.0F);
	}
	@Override
	public float getManualScale()
	{
		return 15;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BottlingMachine";
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDevice && (meta==BlockMetalDevices.META_conveyorBelt);
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

		boolean mirrored = false;
		boolean b = structureCheck(world,startX,startY,startZ, side, mirrored);
		if(!b)
		{
			mirrored = true;
			b = structureCheck(world,startX,startY,startZ, side, mirrored);
		}

		if(b)
			for(int h=-1;h<=0;h++)
				for(int l=0;l<2;l++)
					for(int w=-1;w<=1;w++)
					{
						int ww = mirrored?-w:w;
						int xx = startX+ (side==4?l: side==5?-l: side==2?-ww : ww);
						int yy = startY+ h;
						int zz = startZ+ (side==2?l: side==3?-l: side==5?-ww : ww);

						world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_bottlingMachine, 0x3);
						TileEntity curr = world.getTileEntity(xx, yy, zz);
						if(curr instanceof TileEntityBottlingMachine)
						{
							TileEntityBottlingMachine tile = (TileEntityBottlingMachine)curr;
							tile.facing=side;
							tile.mirrored=mirrored;
							tile.formed=true;
							tile.pos = (h+1)*6 + l*3 + (w+1);
							tile.offset = new int[]{(side==4?l-1: side==5?1-l: side==2?-ww: ww),h+1,(side==2?l-1: side==3?1-l: side==5?-ww: ww)};
						}
					}
		return true;
	}

	boolean structureCheck(World world, int startX, int startY, int startZ, int side, boolean mirror)
	{
		for(int h=-1;h<=0;h++)
			for(int l=0;l<2;l++)
				for(int w=-1;w<=1;w++)
					if(structure[h+1][1-l][w+1]!=null)
					{
						int ww = mirror?-w:w;
						int xx = startX+ (side==4?l: side==5?-l: side==2?-ww : ww);
						int yy = startY+ h;
						int zz = startZ+ (side==2?l: side==3?-l: side==5?-ww : ww);

						if(world.isAirBlock(xx, yy, zz))
							return false;
						ItemStack checkStack = new ItemStack(world.getBlock(xx,yy,zz),1,world.getBlockMetadata(xx,yy,zz));
						if(!OreDictionary.itemMatches(structure[h+1][1-l][w+1], checkStack, true))
							return false;
						TileEntity tile = world.getTileEntity(xx, yy, zz);
						if(tile instanceof TileEntityConveyorBelt)
						{
							int f = ((TileEntityConveyorBelt)tile).facing;
							int fExpected = l==1?(w==-1?ForgeDirection.OPPOSITES[side]:side): w<1?ForgeDirection.ROTATION_MATRIX[mirror?0:1][side]:side;
							if(f != fExpected)
								return false;
						}
					}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockMetalDecoration,5,BlockMetalDecoration.META_scaffolding),
				new ItemStack(IEContent.blockMetalDecoration,2,BlockMetalDecoration.META_lightEngineering),
				new ItemStack(IEContent.blockMetalDevice,5,BlockMetalDevices.META_conveyorBelt)};
	}
}