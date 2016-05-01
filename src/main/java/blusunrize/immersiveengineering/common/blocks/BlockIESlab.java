package blusunrize.immersiveengineering.common.blocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockIESlab extends BlockIETileProvider
{
	public static final PropertyInteger prop_SlabType = PropertyInteger.create("slabtype", 0,2);

	public BlockIESlab(String name, Material material, PropertyEnum property)
	{
		super(name, material, property, ItemBlockIESlabs.class, prop_SlabType);
	}

	//	@Override
	//	public IBlockState getInventoryState(int meta)
	//	{
	//		return super.getInventoryState(meta).withProperty(prop_SlabType, 0);
	//	}
	//	
	//	@Override
	//	protected BlockState createNotTempBlockState()
	//	{
	//		return new BlockState(this, new IProperty[] {this.property, prop_SlabType});
	//	}
	//	@Override
	//	protected IBlockState getInitDefaultState()
	//	{
	//		return super.getInitDefaultState().withProperty(prop_SlabType, 0);
	//	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityIESlab)
			return state.withProperty(prop_SlabType, ((TileEntityIESlab)tile).slabType);
		return state;
	}
	//	@Override
	//	public IBlockState getStateFromMeta(int meta)
	//	{
	//		return super.getStateFromMeta(meta).withProperty(prop_SlabType, 0);
	//	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityIESlab();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile)
	{
		if(tile instanceof TileEntityIESlab && !player.capabilities.isCreativeMode)
		{
			spawnAsEntity(world, pos, new ItemStack(this, ((TileEntityIESlab)tile).slabType==2?2:1 , this.getMetaFromState(state)));
			return;
		}
		super.harvestBlock(world, player, pos, state, tile);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityIESlab)
		{
			int type = ((TileEntityIESlab)te).slabType;
			if(type==0)
				return side==EnumFacing.DOWN;
			else if(type==1)
				return side==EnumFacing.UP;
		}
		return true;
	}

	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity ent)
	{
		this.setBlockBoundsBasedOnState(world, pos);
		super.addCollisionBoxesToList(world, pos, state, mask, list, ent);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityIESlab)
		{
			int type = ((TileEntityIESlab)te).slabType;
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
	public boolean isFullBlock()
	{
		return false;
	}
	@Override
	public boolean isFullCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}