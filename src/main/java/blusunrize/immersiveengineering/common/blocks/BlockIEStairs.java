package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockIEStairs extends BlockStairs
{
	public boolean hasFlavour = false;
	public boolean isFlammable = false;
	public String name;
	
	public BlockIEStairs(String name, Block block, int meta)
	{
		super(block,meta);
		this.name = name;
		this.setBlockName(ImmersiveEngineering.MODID+"."+name);
		GameRegistry.registerBlock(this, ItemBlockIEStairs.class, name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.useNeighborBrightness = true;
	}
	
	public BlockIEStairs setFlammable(boolean b)
	{
		this.isFlammable = b;
		return this;
	}
	
	public BlockIEStairs setHasFlavour(boolean hasFlavour)
	{
		this.hasFlavour = hasFlavour;
		return this;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBounds(0,0,0, 1,1,1);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		return isFlammable;
	}
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		return isFlammable?20:0;
	}
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		return isFlammable?5:0;
	}
}