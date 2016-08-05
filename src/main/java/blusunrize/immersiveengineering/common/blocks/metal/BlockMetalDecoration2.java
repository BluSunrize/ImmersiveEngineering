package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;

public class BlockMetalDecoration2 extends BlockIETileProvider<BlockTypes_MetalDecoration2> implements IPostBlock
{
	public BlockMetalDecoration2()
	{
		super("metalDecoration2", Material.iron, PropertyEnum.create("type", BlockTypes_MetalDecoration2.class), ItemBlockIEBase.class, IEProperties.FACING_ALL,IEProperties.MULTIBLOCKSLAVE,IEProperties.INT_4,OBJProperty.instance);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
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

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		if(this.getMetaFromState(state)==BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta()||this.getMetaFromState(state)==BlockTypes_MetalDecoration2.STEEL_POST.getMeta())
			return new ArrayList<>();
		return super.getDrops(world, pos, state, fortune);
	}
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityWoodenPost)
		{
			if(!((TileEntityWoodenPost)tileEntity).isDummy() && !world.isRemote && world.getGameRules().getBoolean("doTileDrops") && !world.restoringBlockSnapshots)
				world.spawnEntityInWorld(new EntityItem(world, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, new ItemStack(this,1,this.getMetaFromState(state))));
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityWoodenPost)
		{
			return ((TileEntityWoodenPost)te).dummy==0?side==EnumFacing.DOWN: ((TileEntityWoodenPost)te).dummy==3?side==EnumFacing.UP: ((TileEntityWoodenPost)te).dummy>3?side.getAxis()==Axis.Y: side.getAxis()!=Axis.Y;
		}
		return super.isSideSolid(world, pos, side);
	}

	@Override
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return (world.getTileEntity(pos) instanceof TileEntityWoodenPost);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		switch(BlockTypes_MetalDecoration2.values()[meta])
		{
		case STEEL_POST:
			return new TileEntityWoodenPost();
		case STEEL_WALLMOUNT:
			return new TileEntityWallmount();
		case ALUMINUM_POST:
			return new TileEntityWoodenPost();
		case ALUMINUM_WALLMOUNT:
			return new TileEntityWallmount();
		case LANTERN:
			return new TileEntityLantern();
		}
		return null;
	}

	@Override
	public boolean canConnectTransformer(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		BlockTypes_MetalDecoration2 type = state.getValue(property);
		boolean slave = state.getValue(IEProperties.MULTIBLOCKSLAVE);
		return slave&&(type==BlockTypes_MetalDecoration2.STEEL_POST||type==BlockTypes_MetalDecoration2.ALUMINUM_POST);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}