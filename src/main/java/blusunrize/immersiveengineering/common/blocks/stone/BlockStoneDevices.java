package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.BlockRenderStoneDevices;
import blusunrize.immersiveengineering.common.Lib;
import blusunrize.immersiveengineering.common.Utils;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;

public class BlockStoneDevices extends BlockIEBase
{
	IIcon[] iconsCokeOven = new IIcon[2];
	IIcon[] iconsBlastFurnace = new IIcon[2];

	public BlockStoneDevices()
	{
		super("stoneDevice", Material.rock, 1, ItemBlockStoneDevices.class, "hempcrete","cokeOven","blastFurnace","coalCoke","insulatorGlass");
		setHardness(2.0F);
		setResistance(20f);
	}

	@Override
	public int getRenderType()
	{
        return BlockRenderStoneDevices.renderID;
	}
	@Override
	public boolean canRenderInPass(int pass)
	{
		BlockRenderStoneDevices.renderPass=pass;
		return true;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		for(int i=0; i<icons.length; i++)
			icons[i][0] = iconRegister.registerIcon("immersiveengineering:"+name+"_"+subNames[i]);
		iconsCokeOven[0] = iconRegister.registerIcon("immersiveengineering:"+name+"_cokeOven_off");
		iconsCokeOven[1] = iconRegister.registerIcon("immersiveengineering:"+name+"_cokeOven_on");
		iconsBlastFurnace[0] = iconRegister.registerIcon("immersiveengineering:"+name+"_blastFurnace_off");
		iconsBlastFurnace[1] = iconRegister.registerIcon("immersiveengineering:"+name+"_blastFurnace_on");
	}
	@Override
	public IIcon getIcon(int side, int meta)
	{
		return super.getIcon(side, meta);
	}
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCokeOven && ((TileEntityCokeOven)world.getTileEntity(x, y, z)).formed)
		{
			int[] off = ((TileEntityCokeOven)world.getTileEntity(x, y, z)).offset;
			if(off[1]!=0)
				return super.getIcon(world, x, y, z, side);
			TileEntityCokeOven teco = ((TileEntityCokeOven)world.getTileEntity(x, y, z)).master();
			if(teco==null)
				teco=((TileEntityCokeOven)world.getTileEntity(x, y, z));
			int j = teco.active?1:0;
			if(off[0]==0&&off[2]==0)
				return iconsCokeOven[j];
			switch(teco.facing)
			{
			case 2:
				if(off[0]==0&&off[2]==2)
					return iconsCokeOven[j];
				else if(off[0]!=0&&off[2]==1)
					return iconsCokeOven[j];
				return super.getIcon(world, x, y, z, side);
			case 3:
				if(off[0]==0&&off[2]==-2)
					return iconsCokeOven[j];
				else if(off[0]!=0&&off[2]==-1)
					return iconsCokeOven[j];
				return super.getIcon(world, x, y, z, side);
			case 4:
				if(off[0]==1&&off[2]!=0)
					return iconsCokeOven[j];
				else if(off[0]==2&&off[2]==0)
					return iconsCokeOven[j];
				return super.getIcon(world, x, y, z, side);
			case 5:
				if(off[0]==-1&&off[2]!=0)
					return iconsCokeOven[j];
				else if(off[0]==-2&&off[2]==0)
					return iconsCokeOven[j];
				return super.getIcon(world, x, y, z, side);
			}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace && ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).formed)
		{
			int[] off = ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).offset;
			if(off[1]!=0)
				return super.getIcon(world, x, y, z, side);
			if(off[0]==0&&off[2]==0)
				return iconsBlastFurnace[0];
			switch(((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).facing)
			{
			case 2:
				if(off[0]==0&&off[2]==2)
					return iconsBlastFurnace[0];
				else if(off[0]!=0&&off[2]==1)
					return iconsBlastFurnace[0];
				return super.getIcon(world, x, y, z, side);
			case 3:
				if(off[0]==0&&off[2]==-2)
					return iconsBlastFurnace[0];
				else if(off[0]!=0&&off[2]==-1)
					return iconsBlastFurnace[0];
				return super.getIcon(world, x, y, z, side);
			case 4:
				if(off[0]==1&&off[2]!=0)
					return iconsBlastFurnace[0];
				else if(off[0]==2&&off[2]==0)
					return iconsBlastFurnace[0];
				return super.getIcon(world, x, y, z, side);
			case 5:
				if(off[0]==-1&&off[2]!=0)
					return iconsBlastFurnace[0];
				else if(off[0]==-2&&off[2]==0)
					return iconsBlastFurnace[0];
				return super.getIcon(world, x, y, z, side);
			}
		}

		return super.getIcon(world, x, y, z, side);
		//		int meta = world.getBlockMetadata(x, y, z);
		//		if(meta<icons.length)
		//			return icons[meta][getSideForTexture(side)];
		//		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCokeOven)
		{
			if(Utils.isHammer(player.getCurrentEquippedItem()))
			{
				int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
				int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
				int xMin= f==5?-2: f==4?0: -1;
				int xMax= f==5?0: f==4?2: 1;
				int zMin= f==3?-2: f==2?0: -1;
				int zMax= f==3?0: f==2?2: 1;
				for(int yy=-1;yy<=1;yy++)
					for(int xx=xMin;xx<=xMax;xx++)
						for(int zz=zMin;zz<=zMax;zz++)
							if((yy!=0||xx!=0||zz!=0) && !(world.getTileEntity(x+xx, y+yy, z+zz) instanceof TileEntityCokeOven))
								return false;

				for(int yy=-1;yy<=1;yy++)
					for(int xx=xMin;xx<=xMax;xx++)
						for(int zz=zMin;zz<=zMax;zz++)
						{
							((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).offset=new int[]{xx,yy,zz};
							((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).facing=f;
							((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).formed=true;
							world.getTileEntity(x+xx, y+yy, z+zz).markDirty();
							world.markBlockForUpdate(x+xx,y+yy,z+zz);
						}
				return true;
			}
			else if(!player.isSneaking()&& ((TileEntityCokeOven)world.getTileEntity(x, y, z)).formed )
			{
				TileEntityCokeOven te = ((TileEntityCokeOven)world.getTileEntity(x, y, z)).master();
				if(te==null)
					te = ((TileEntityCokeOven)world.getTileEntity(x, y, z));
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_CokeOven, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}

		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace)
		{	
			if(Utils.isHammer(player.getCurrentEquippedItem()))
			{
				int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
				int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
				int xMin= f==5?-2: f==4?0: -1;
				int xMax= f==5?0: f==4?2: 1;
				int zMin= f==3?-2: f==2?0: -1;
				int zMax= f==3?0: f==2?2: 1;
				for(int yy=-1;yy<=1;yy++)
					for(int xx=xMin;xx<=xMax;xx++)
						for(int zz=zMin;zz<=zMax;zz++)
							if((yy!=0||xx!=0||zz!=0) && !(world.getTileEntity(x+xx, y+yy, z+zz) instanceof TileEntityBlastFurnace))
								return false;

				for(int yy=-1;yy<=1;yy++)
					for(int xx=xMin;xx<=xMax;xx++)
						for(int zz=zMin;zz<=zMax;zz++)
						{
							((TileEntityBlastFurnace)world.getTileEntity(x+xx, y+yy, z+zz)).offset=new int[]{xx,yy,zz};
							((TileEntityBlastFurnace)world.getTileEntity(x+xx, y+yy, z+zz)).facing=f;
							((TileEntityBlastFurnace)world.getTileEntity(x+xx, y+yy, z+zz)).formed=true;
							world.getTileEntity(x+xx, y+yy, z+zz).markDirty();
							world.markBlockForUpdate(x+xx,y+yy,z+zz);
						}
				return true;
			}
			else if( ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).formed )
			{
				TileEntityBlastFurnace te = ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).master();
				if(te==null)
					te = ((TileEntityBlastFurnace)world.getTileEntity(x, y, z));
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_BlastFurnace, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCokeOven)
		{
			int[] off = ((TileEntityCokeOven)world.getTileEntity(x, y, z)).offset;
			int xx = x - off[0];
			int yy = y - off[1];
			int zz = z - off[2];
			if(!(off[0]==0&&off[1]==0&&off[2]==0) && !(world.getTileEntity(xx, yy, zz) instanceof TileEntityCokeOven))
				return;
			int facing = ((TileEntityCokeOven)world.getTileEntity(xx, yy, zz)).facing;

			int xMin= facing==5?-2: facing==4?0:-1;
			int xMax= facing==5? 0: facing==4?2: 1;
			int zMin= facing==3?-2: facing==2?0:-1;
			int zMax= facing==3? 0: facing==2?2: 1;
			for(int iy=-1;iy<=1;iy++)
				for(int ix=xMin;ix<=xMax;ix++)
					for(int iz=zMin;iz<=zMax;iz++)
						if(world.getTileEntity(xx+ix, yy+iy, zz+iz) instanceof TileEntityCokeOven)
						{
							((TileEntityCokeOven)world.getTileEntity(xx+ix, yy+iy, zz+iz)).formed=false;
							((TileEntityCokeOven)world.getTileEntity(xx+ix, yy+iy, zz+iz)).offset=new int[3];
							world.getTileEntity(xx+ix, yy+iy, zz+iz).markDirty();
							world.markBlockForUpdate(xx+ix, yy+iy, zz+iz);
						}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace)
		{
			int[] off = ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).offset;
			int xx = x - off[0];
			int yy = y - off[1];
			int zz = z - off[2];
			if(!(off[0]==0&&off[1]==0&&off[2]==0) && !(world.getTileEntity(xx, yy, zz) instanceof TileEntityBlastFurnace))
				return;
			int facing = ((TileEntityBlastFurnace)world.getTileEntity(xx, yy, zz)).facing;

			int xMin= facing==5?-2: facing==4?0:-1;
			int xMax= facing==5? 0: facing==4?2: 1;
			int zMin= facing==3?-2: facing==2?0:-1;
			int zMax= facing==3? 0: facing==2?2: 1;
			for(int iy=-1;iy<=1;iy++)
				for(int ix=xMin;ix<=xMax;ix++)
					for(int iz=zMin;iz<=zMax;iz++)
						if(world.getTileEntity(xx+ix, yy+iy, zz+iz) instanceof TileEntityBlastFurnace)
						{
							((TileEntityBlastFurnace)world.getTileEntity(xx+ix, yy+iy, zz+iz)).formed=false;
							((TileEntityBlastFurnace)world.getTileEntity(xx+ix, yy+iy, zz+iz)).offset=new int[3];
							world.getTileEntity(xx+ix, yy+iy, zz+iz).markDirty();
							world.markBlockForUpdate(xx+ix, yy+iy, zz+iz);
						}
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case 1:
			return new TileEntityCokeOven();
		case 2:
			return new TileEntityBlastFurnace();
		}
		return null;
	}
	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}

}
