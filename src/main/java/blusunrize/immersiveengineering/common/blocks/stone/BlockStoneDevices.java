package blusunrize.immersiveengineering.common.blocks.stone;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.BlockRenderStoneDevices;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Lib;

public class BlockStoneDevices extends BlockIEBase
{
	IIcon[] iconsCokeOven = new IIcon[10];
	IIcon[] iconsBlastFurnace = new IIcon[2];

	public BlockStoneDevices()
	{
		super("stoneDevice", Material.rock, 1, ItemBlockStoneDevices.class, "hempcrete","cokeOven","blastFurnace","coalCoke","insulatorGlass");
		setHardness(2.0F);
		setResistance(20f);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		list.add(new ItemStack(item, 1, 4));
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
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		if(metadata==1||metadata==2)
			return new ArrayList<ItemStack>();
		return super.getDrops(world, x, y, z, metadata, fortune);
	}
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		return getOriginalBlock(world, x, y, z);
	}

	public ItemStack getOriginalBlock(World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockPart)
			return ((TileEntityMultiblockPart)te).getOriginalBlock();
		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
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
		for(int i=0; i<9; i++)
			iconsCokeOven[i] = iconRegister.registerIcon("immersiveengineering:"+name+"_cokeOven"+i+(i==4?"off":""));
		iconsCokeOven[9] = iconRegister.registerIcon("immersiveengineering:"+name+"_cokeOven4on");
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
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityCokeOven && ((TileEntityCokeOven)te).formed)
		{
			TileEntityCokeOven teco = ((TileEntityCokeOven)te);
			if(teco.master()==null)
				return iconsCokeOven[teco.active?9:4];
			if(side!=teco.facing)
				return super.getIcon(world, x, y, z, side);
			int[] off = teco.offset;
			int pos = (1-off[1])*3 + (teco.facing==2?(1-off[0]): teco.facing==3?(off[0]+1): teco.facing==5?(1-off[2]): (off[2]+1));
			return iconsCokeOven[pos];
		}
		if(te instanceof TileEntityBlastFurnace && ((TileEntityBlastFurnace)te).formed)
		{
			int[] off = ((TileEntityBlastFurnace)te).offset;
			if(off[1]!=0)
				return super.getIcon(world, x, y, z, side);
			TileEntityBlastFurnace tebf = ((TileEntityBlastFurnace)te);
			if(tebf.master()==null)
				return iconsBlastFurnace[tebf.active?1:0];
		}

		return super.getIcon(world, x, y, z, side);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y ,int z, int side)
	{
		return super.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity curr = world.getTileEntity(x, y, z);
		if(curr instanceof TileEntityCokeOven)
		{
			if(!player.isSneaking() && ((TileEntityCokeOven)curr).formed )
			{
				TileEntityCokeOven te = ((TileEntityCokeOven)curr).master();
				if(te==null)
					te = ((TileEntityCokeOven)curr);
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_CokeOven, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}

		}
		if(curr instanceof TileEntityBlastFurnace)
		{	
			if(!player.isSneaking() && ((TileEntityBlastFurnace)curr).formed )
			{
				TileEntityBlastFurnace te = ((TileEntityBlastFurnace)curr).master();
				if(te==null)
					te = ((TileEntityBlastFurnace)curr);
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_BlastFurnace, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasTileEntity(int meta)
	{
		return meta==1||meta==2;
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
