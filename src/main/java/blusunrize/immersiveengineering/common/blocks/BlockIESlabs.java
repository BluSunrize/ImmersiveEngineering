package blusunrize.immersiveengineering.common.blocks;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockIESlabs extends BlockIEBase
{
	String iconKey;
	public BlockIESlabs(String name, String iconKey, Material material, String... subNames)
	{
		super(name,material,1,ItemBlockIESlabs.class,subNames);
		this.iconKey = iconKey;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		for(int i=0;i<subNames.length;i++)
			icons[i][0] = iconRegister.registerIcon("immersiveengineering:"+iconKey+subNames[i]);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityIESlab && !player.capabilities.isCreativeMode && willHarvest)
		{
			EntityItem drop = new EntityItem(world,x+.5,y+.5,z+.5, new ItemStack(this, ((TileEntityIESlab)world.getTileEntity(x,y,z)).slabType==2?2:1 ,world.getBlockMetadata(x,y,z)));
			if(!world.isRemote)
				world.spawnEntityInWorld(drop);
		}
		return super.removedByPlayer(world, player, x, y, z, willHarvest);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityIESlab)
		{
			int type = ((TileEntityIESlab)world.getTileEntity(x, y, z)).slabType;
			if(type==0)
				return side==DOWN;
			else if(type==1)
				return side==UP;
		}
		return true;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity ent)
	{
		this.setBlockBoundsBasedOnState(world, x, y, z);
		super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityIESlab)
		{
			int type = ((TileEntityIESlab)world.getTileEntity(x, y, z)).slabType;
			if(type==0)
				this.setBlockBounds(0,0,0, 1,.5f,1);
			else if(type==1)
				this.setBlockBounds(0,.5f,0, 1,1,1);
			else
				this.setBlockBounds(0,0,0,1,1,1);
		}
		else
			this.setBlockBounds(0,0,0,1,.5f,1);
	}
	@Override
	public void setBlockBoundsForItemRender()
	{
		this.setBlockBounds(0,0,0,1,.5f,1);
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
	//	@Override
	//	public int getRenderType()
	//	{
	//		return BlockRenderIESlab.renderID;
	//	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityIESlab();
	}
	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}
}