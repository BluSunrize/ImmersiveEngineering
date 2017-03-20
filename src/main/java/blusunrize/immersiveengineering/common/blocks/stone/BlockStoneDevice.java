package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockStoneDevice extends BlockIEMultiblock<BlockTypes_StoneDevices>
{
	public BlockStoneDevice()
	{
		super("stoneDevice",Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDevices.class), ItemBlockIEBase.class, IEProperties.BOOLEANS[0]);
		setHardness(2.0F);
		setResistance(20f);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBlastFurnaceAdvanced)
			return ((TileEntityBlastFurnaceAdvanced)te).pos==1 || ((TileEntityBlastFurnaceAdvanced)te).pos==4 || ((TileEntityBlastFurnaceAdvanced)te).pos==7 || (((TileEntityBlastFurnaceAdvanced)te).pos==31);
		return true;
	}

	private static final AxisAlignedBB AABB_CARPET = new AxisAlignedBB(0,0,0, 1,.0625,1);
	private static final AxisAlignedBB AABB_QUARTER = new AxisAlignedBB(0,0,0, 1,.25,1);
	private static final AxisAlignedBB AABB_THREEQUARTER = new AxisAlignedBB(0,0,0, 1,.75,1);
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		BlockTypes_StoneDevices meta = state.getValue(getMetaProperty());
		if(meta==BlockTypes_StoneDevices.CONCRETE_SHEET)
			return AABB_CARPET;
		else if(meta==BlockTypes_StoneDevices.CONCRETE_QUARTER)
			return AABB_QUARTER;
		else if(meta==BlockTypes_StoneDevices.CONCRETE_THREEQUARTER)
			return AABB_THREEQUARTER;
		return super.getBoundingBox(state, world, pos);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
	{
		super.getSubBlocks(item, tab, list);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
			case 0:
				return new TileEntityCokeOven();
			case 1:
				return new TileEntityBlastFurnace();
			case 2:
				return new TileEntityBlastFurnaceAdvanced();
		}
		return null;
	}
}