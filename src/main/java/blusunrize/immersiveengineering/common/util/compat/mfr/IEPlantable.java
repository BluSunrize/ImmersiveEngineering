package blusunrize.immersiveengineering.common.util.compat.mfr;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
import powercrystals.minefactoryreloaded.api.ReplacementBlock;

public class IEPlantable implements IFactoryPlantable
{
	protected Item seeds;
	protected Block plant;

	public IEPlantable(Item seeds, Block plant)
	{
		this.seeds = seeds;
		this.plant = plant;
	}

	@Override
	public boolean canBePlanted(ItemStack stack, boolean arg1)
	{
		return true;
	}
	@Override
	public boolean canBePlantedHere(World world, int x, int y, int z, ItemStack stack)
	{
		if (!world.isAirBlock(x, y, z))
			return false;
		Block ground = world.getBlock(x, y-1, z);
		return ground != null && (ground.equals(Blocks.dirt) || ground.equals(Blocks.grass) || ground.equals(Blocks.farmland));
	}

	@Override
	public ReplacementBlock getPlantedBlock(World world, int x, int y, int z, ItemStack stack)
	{
		if (stack.getItem() != seeds)
			return new ReplacementBlock(Blocks.air);
		return new ReplacementBlock(plant).setMeta(0);
	}
	@Override
	public Item getSeed()
	{
		return seeds;
	}


	@Override
	public void prePlant(World world, int x, int y, int z, ItemStack stack)
	{
		Block ground = world.getBlock(x, y-1, z);
		if(ground.equals(Blocks.grass) || ground.equals(Blocks.dirt))
			world.setBlock(x, y-1, z, Blocks.farmland);
	}
	@Override
	public void postPlant(World world, int x, int y, int z, ItemStack stack)
	{
	}

}
