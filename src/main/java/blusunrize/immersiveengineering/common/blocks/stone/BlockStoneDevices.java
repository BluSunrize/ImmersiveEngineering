package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.BlockRenderStoneDevices;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.util.Lib;

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
	public boolean isOpaqueCube()
	{
		return false;
	}
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	@Override
    public int getRenderBlockPass()
    {
        return 1;
    }

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
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
			TileEntityBlastFurnace tebf = ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).master();
			if(tebf==null)
				tebf=((TileEntityBlastFurnace)world.getTileEntity(x, y, z));
			int j = tebf.active?1:0;
			if(off[0]==0&&off[2]==0)
				return iconsBlastFurnace[j];
			switch(((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).facing)
			{
			case 2:
				if(off[0]==0&&off[2]==2)
					return iconsBlastFurnace[j];
				else if(off[0]!=0&&off[2]==1)
					return iconsBlastFurnace[j];
				return super.getIcon(world, x, y, z, side);
			case 3:
				if(off[0]==0&&off[2]==-2)
					return iconsBlastFurnace[j];
				else if(off[0]!=0&&off[2]==-1)
					return iconsBlastFurnace[j];
				return super.getIcon(world, x, y, z, side);
			case 4:
				if(off[0]==1&&off[2]!=0)
					return iconsBlastFurnace[j];
				else if(off[0]==2&&off[2]==0)
					return iconsBlastFurnace[j];
				return super.getIcon(world, x, y, z, side);
			case 5:
				if(off[0]==-1&&off[2]!=0)
					return iconsBlastFurnace[j];
				else if(off[0]==-2&&off[2]==0)
					return iconsBlastFurnace[j];
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
			if(!player.isSneaking() && ((TileEntityCokeOven)world.getTileEntity(x, y, z)).formed )
			{
				TileEntityCokeOven te = ((TileEntityCokeOven)world.getTileEntity(x, y, z)).master();
				if(te==null)
					te = ((TileEntityCokeOven)world.getTileEntity(x, y, z));
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_CokeOven, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}

		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace)
		{	
			if(!player.isSneaking() && ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).formed )
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
		if(world.getTileEntity(x, y, z) instanceof TileEntityCokeOven && ((TileEntityCokeOven)world.getTileEntity(x, y, z)).formed)
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
							world.addBlockEvent(xx+ix, yy+iy, zz+iz, this, 0,0);
						}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace && ((TileEntityBlastFurnace)world.getTileEntity(x, y, z)).formed)
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
							world.addBlockEvent(xx+ix, yy+iy, zz+iz, this, 0,0);
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
