package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockConveyor extends BlockIETileProvider
{
	public BlockConveyor()
	{
		super("conveyor",Material.iron, PropertyEnum.create("type", BlockTypes_Conveyor.class), ItemBlockIEBase.class,  IEProperties.FACING_ALL,IEProperties.CONVEYORWALLS[0],IEProperties.CONVEYORWALLS[1],IEProperties.CONVEYORUPDOWN);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
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
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityConveyorBelt)
		{
			for(int i=0; i<IEProperties.CONVEYORWALLS.length; i++)
				state = state.withProperty(IEProperties.CONVEYORWALLS[i], ((TileEntityConveyorBelt)tile).renderWall(i));
			state = state.withProperty(IEProperties.CONVEYORUPDOWN, ((TileEntityConveyorBelt)tile).transportUp?1: ((TileEntityConveyorBelt)tile).transportDown?2: 0);
		}
		return state;
	}

	@Override
	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
		super.onIEBlockPlacedBy(world, pos, state, side, hitX, hitY, hitZ, placer, stack);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityConveyorBelt)
		{
			TileEntityConveyorBelt conveyer = (TileEntityConveyorBelt)tile;
			EnumFacing f = conveyer.facing;
			tile = world.getTileEntity(pos.offset(f).add(0,1,0));
			if(tile instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)tile).facing!=f.getOpposite())
				conveyer.transportUp = true;
			tile = world.getTileEntity(pos.offset(f.getOpposite()).add(0,1,0));
			if(tile instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)tile).facing==f)
				conveyer.transportDown = true;
			if(conveyer.transportUp && conveyer.transportDown)
				conveyer.transportDown = false;
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return side==EnumFacing.DOWN;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_Conveyor.values()[meta])
		{
		case CONVEYOR:
			return new TileEntityConveyorBelt();
		case CONVEYOR_DROPPER:
			return new TileEntityConveyorBelt(true);
		}
		return null;
	}
}