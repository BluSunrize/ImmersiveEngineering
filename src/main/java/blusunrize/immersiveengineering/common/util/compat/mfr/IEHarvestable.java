package blusunrize.immersiveengineering.common.util.compat.mfr;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.HarvestType;
import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;

public class IEHarvestable implements IFactoryHarvestable
{
	@Override
	public Block getPlant()
	{
		return IEContent.blockCrop;
	}

	@Override
	public boolean canBeHarvested(World world, Map<String, Boolean> arg1, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return ((BlockIECrop)IEContent.blockCrop).getMaxMeta(meta)<=meta;
	}

	@Override
	public List<ItemStack> getDrops(World world, Random rand, Map<String, Boolean> arg2, int x, int y, int z)
	{
		return IEContent.blockCrop.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
	}

	@Override
	public HarvestType getHarvestType()
	{
		return HarvestType.Column;
	}

	@Override
	public boolean breakBlock()
	{
		return true;
	}
	@Override
	public void postHarvest(World world, int x, int y, int z)
	{
	}

	@Override
	public void preHarvest(World world, int x, int y, int z)
	{
	}

}