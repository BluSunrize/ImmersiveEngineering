package blusunrize.immersiveengineering.common.blocks.stone;

import java.util.List;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockStoneDevice extends BlockIEMultiblock<BlockTypes_StoneDevices>
{
	public BlockStoneDevice()
	{
		super("stoneDevice",Material.rock, PropertyEnum.create("type", BlockTypes_StoneDevices.class), ItemBlockIEBase.class, IEProperties.BOOLEANS[0]);
		setHardness(2.0F);
		setResistance(20f);
		lightOpacity = 0;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
	{
		super.getSubBlocks(item, tab, list);
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
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