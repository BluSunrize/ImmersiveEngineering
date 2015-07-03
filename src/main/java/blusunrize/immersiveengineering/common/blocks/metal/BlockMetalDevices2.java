package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockMetalDevices2 extends BlockIEBase implements blusunrize.aquatweaks.api.IAquaConnectable
{
	public static int META_breakerSwitch=0;

	public BlockMetalDevices2()
	{
		super("metalDevice2", Material.iron, 1, ItemBlockMetalDevices2.class,
				"breakerSwitch");
		setHardness(3.0F);
		setResistance(15.0F);
	}


	@Override
	public boolean allowHammerHarvest(int meta)
	{
		return true;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		return super.getPickBlock(target, world, x, y, z, player);
	}
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	{
	}
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		return ret;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for(int i=0; i<subNames.length; i++)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.icons[META_breakerSwitch][0] = iconRegister.registerIcon("immersiveEngineering:metal2_breakerSwitch");
	}
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		return super.getIcon(world, x, y, z, side);
	}
	@Override
	public IIcon getIcon(int side, int meta)
	{
		return super.getIcon(side, meta);
	}
	@Override
	public int getRenderType()
	{
		return BlockRenderMetalDevices2.renderID;
	}
	@Override
	public int getRenderBlockPass()
	{
		return 0;
	}


	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if(!player.isSneaking() && world.getTileEntity(x, y, z) instanceof TileEntityBreakerSwitch)
		{
			if(!world.isRemote)
			{
				((TileEntityBreakerSwitch)world.getTileEntity(x, y, z)).toggle();
				world.getTileEntity(x, y, z).markDirty();
				world.func_147451_t(x, y, z);
			}
			return true;
		}

		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityBreakerSwitch)
		{
			int f = ((TileEntityBreakerSwitch)world.getTileEntity(x, y, z)).facing;
			int side = ((TileEntityBreakerSwitch)world.getTileEntity(x, y, z)).sideAttached;
			if(side==0)
				this.setBlockBounds(f<4?.25f:f==5?.8125f:0, .25f, f>3?.25f:f==3?.8125f:0, f<4?.75f:f==4?.1875f:1,.8125f,f>3?.75f:f==2?.1875f:1);
			else if(side==1)
				this.setBlockBounds(f==4?.1875f:.25f,0,f==2?.1875f:.25f, f==5?.8125f:.75f,.1875f,f==3?.8125f:.75f);
			else
				this.setBlockBounds(f==4?.1875f:.25f,.8125f,f==2?.1875f:.25f, f==5?.8125f:.75f,1,f==3?.8125f:.75f);
		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
//		int meta = world.getBlockMetadata(x, y, z);
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case 0://0 breakerSwitch
			return new TileEntityBreakerSwitch();
		}
		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block nbid)
	{
	}


	@Optional.Method(modid = "AquaTweaks")
	public boolean shouldRenderFluid(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_breakerSwitch;
	}
	@Optional.Method(modid = "AquaTweaks")
	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_breakerSwitch;
	}
}