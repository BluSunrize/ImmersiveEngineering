package blusunrize.immersiveengineering.common.blocks.wooden;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockWoodenDecoration extends BlockIEBase implements blusunrize.aquatweaks.api.IAquaConnectable
{
	public BlockWoodenDecoration()
	{
		super("woodenDecoration", Material.wood,2, ItemBlockWoodenDecoration.class, 
				"treatedWood","fence",
				"slab0","slab1","doubleSlab",
				"scaffolding","wallMount");
		this.setHardness(2.0F);
		this.setResistance(5.0F);
	}

	@Override
	public int getRenderType()
	{
		return BlockRenderWoodenDecoration.renderID;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int meta)
	{
		if(meta==3||meta==4)
			return 2;
		return super.damageDropped(meta);
	}
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		if(metadata==4)
			ret.add(new ItemStack(this,1,2));
		return ret;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==1)
			return side==UP;
		if(meta==2)
			return side==DOWN;
		if(meta==3)
			return side==UP;
		if(world.getTileEntity(x,y,z) instanceof TileEntityWallmount)
		{
			if(side==UP)
				return ((TileEntityWallmount)world.getTileEntity(x,y,z)).inverted;
			else if(side==DOWN)
				return !((TileEntityWallmount)world.getTileEntity(x,y,z)).inverted;
			else
				return true;
		}
		return true;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y ,int z, int side)
	{
		int meta = world.getBlockMetadata(x+(side==4?1:side==5?-1:0),y+(side==0?1:side==1?-1:0),z+(side==2?1:side==3?-1:0));
		if(meta==1||meta==2||meta==3)
			return true;
		if(meta==5)
			return (world.getBlock(x, y, z)==this&&world.getBlockMetadata(x,y,z)==5)?false:true;
		return super.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for(int i=0; i<subNames.length; i++)
			if(i!=0&&i!=3)
				list.add(new ItemStack(item, 1, i));
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		for(int i=0; i<subNames.length; i++)
			if(i==5)
			{
				icons[i][0] = iconRegister.registerIcon("immersiveengineering:scaffolding_top");
				icons[i][1] = iconRegister.registerIcon("immersiveengineering:scaffolding_side");
			}
			else if(i==6)
			{
				icons[i][0] = iconRegister.registerIcon("immersiveengineering:wood_wallmount");
				icons[i][1] = iconRegister.registerIcon("immersiveengineering:wood_wallmount");
			}
			else
			{
				icons[i][0] = iconRegister.registerIcon("immersiveengineering:treatedWood");
				icons[i][1] = iconRegister.registerIcon("immersiveengineering:treatedWood");
			}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z)==1)
			this.setBlockBounds(canConnectFenceTo(world,x-1,y,z)?0:.375f,0,canConnectFenceTo(world,x,y,z-1)?0:.375f, canConnectFenceTo(world,x+1,y,z)?1:.625f,1,canConnectFenceTo(world,x,y,z+1)?1:.625f);
		else if(world.getBlockMetadata(x, y, z)==2)
			this.setBlockBounds(0,0,0, 1,.5f,1);
		else if(world.getBlockMetadata(x, y, z)==3)
			this.setBlockBounds(0,.5f,0, 1,1,1);
		else if(world.getTileEntity(x, y, z) instanceof TileEntityWallmount)
		{
			TileEntityWallmount arm = (TileEntityWallmount)world.getTileEntity(x, y, z);
			int f = arm.facing;
			if(arm.sideAttached>0)
				this.setBlockBounds(f==4?0:f==5?.375f:.3125f,arm.inverted?.3125f:0,f==2?0:f==3?.375f:.3125f, f==5?1:f==4?.625f:.6875f,arm.inverted?1:.6875f,f==3?1:f==2?.625f:.6875f);
			else
				this.setBlockBounds(f==5?0:.3125f,arm.inverted?.375f:0,f==3?0:.3125f, f==4?1:.6875f,arm.inverted?1:.625f,f==2?1:.6875f);
		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		return block != this && block != Blocks.fence_gate ? (block.getMaterial().isOpaque() && block.renderAsNormalBlock() ? block.getMaterial() != Material.gourd : false) : true;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity ent)
	{
		super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	}
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z)==1)
			this.setBlockBounds(canConnectFenceTo(world,x-1,y,z)?0:.375f,0,canConnectFenceTo(world,x,y,z-1)?0:.375f, canConnectFenceTo(world,x+1,y,z)?1:.625f,1.5f,canConnectFenceTo(world,x,y,z+1)?1:.625f);
		else if(world.getBlockMetadata(x, y, z)==5)
			this.setBlockBounds(.0625f,0,.0625f, .9375f,1,.9375f);
		else
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
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent)
	{
		if(world.getBlockMetadata(x, y, z)==5)
		{
			float f5 = 0.15F;
			if (ent.motionX < (double)(-f5))
				ent.motionX = (double)(-f5);
			if (ent.motionX > (double)f5)
				ent.motionX = (double)f5;
			if (ent.motionZ < (double)(-f5))
				ent.motionZ = (double)(-f5);
			if (ent.motionZ > (double)f5)
				ent.motionZ = (double)f5;

			ent.fallDistance = 0.0F;
			if (ent.motionY < -0.15D)
				ent.motionY = -0.15D;

			if(ent.motionY<0 && ent instanceof EntityPlayer && ent.isSneaking())
			{
				ent.motionY=.05;
				return;
			}
			if(ent.isCollidedHorizontally)
				ent.motionY=.2;
		}
	}

	@Override
    public boolean hasTileEntity(int meta)
    {
        return meta==6;
    }
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case 6:
			return new TileEntityWallmount();
		}
		return null;
	}
	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}


	@Optional.Method(modid = "AquaTweaks")
	public boolean shouldRenderFluid(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==1
				|| meta==6;
	}
	@Optional.Method(modid = "AquaTweaks")
	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==1
				|| meta==6;
	}
}