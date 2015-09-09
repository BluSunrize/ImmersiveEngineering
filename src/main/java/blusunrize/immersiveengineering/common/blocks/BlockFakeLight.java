package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockFakeLight extends Block
{
	public BlockFakeLight()
	{
		super(Material.air);
		this.setBlockName(ImmersiveEngineering.MODID+".fakeLight");
		GameRegistry.registerBlock(this, "fakeLight");
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
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
}