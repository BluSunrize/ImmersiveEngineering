package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFakeLight extends BlockIEBase
{
	public BlockFakeLight()
	{
		super("fakeLight", Material.air, 0, ItemBlockIEBase.class);
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
	public int getRenderType()
	{
		return -1;
	}
	@Override
	public boolean isAir(IBlockAccess world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return AxisAlignedBB.getBoundingBox(x,y,z,x,y,z);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return AxisAlignedBB.getBoundingBox(x,y,z,x,y,z);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowHammerHarvest(int metadata) {
		// TODO Auto-generated method stub
		return false;
	}
}