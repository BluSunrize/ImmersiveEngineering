/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.PlantType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class HempBlock extends BushBlock implements IGrowable
{
	public final String name;
	public final static EnumProperty<EnumHempGrowth> GROWTH = EnumProperty.create("growth", EnumHempGrowth.class);

	public HempBlock(String name)
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0));
		this.name = name;
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItem(this, new Item.Properties()));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(GROWTH);
	}

	public EnumHempGrowth getMinGrowth(EnumHempGrowth current)
	{
		if(current==EnumHempGrowth.TOP0)
			return EnumHempGrowth.TOP0;
		else
			return EnumHempGrowth.BOTTOM0;
	}

	public EnumHempGrowth getMaxGrowth(EnumHempGrowth current)
	{
		if(current==EnumHempGrowth.TOP0)
			return EnumHempGrowth.TOP0;
		else
			return EnumHempGrowth.BOTTOM4;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos)
	{
		boolean b = super.isValidPosition(state, world, pos);
		if(state.get(GROWTH)==EnumHempGrowth.TOP0)
		{
			BlockState stateBelow = world.getBlockState(pos.add(0, -1, 0));
			b = stateBelow.getBlock().equals(this)&&stateBelow.get(GROWTH)==getMaxGrowth(EnumHempGrowth.BOTTOM0);
		}
		return b;
	}

	@Override
	protected boolean isValidGround(BlockState state, IBlockReader world, BlockPos pos)
	{
		return state.canSustainPlant(world, pos, Direction.UP, this);
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos)
	{
		return PlantType.Crop;
	}

	private static final EnumMap<EnumHempGrowth, VoxelShape> shapes = new EnumMap<>(EnumHempGrowth.class);

	static
	{
		shapes.put(EnumHempGrowth.BOTTOM0, VoxelShapes.create(
				new AxisAlignedBB(0, 0, 0, 1, .375f, 1)));
		shapes.put(EnumHempGrowth.BOTTOM1, VoxelShapes.create(
				new AxisAlignedBB(0, 0, 0, 1, .625f, 1)));
		shapes.put(EnumHempGrowth.BOTTOM2, VoxelShapes.create(
				new AxisAlignedBB(0, 0, 0, 1, .875f, 1)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return shapes.getOrDefault(state.get(GROWTH), VoxelShapes.fullCube());
	}

	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		ItemStack tool = builder.get(LootParameters.TOOL);
		int fortune = (tool==null)?(0):(EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool));
		EnumHempGrowth growth = state.get(GROWTH);
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(IEContent.itemSeeds, 1));
		if(growth==getMaxGrowth(growth))
		{
			for(int i = 0; i < 3+fortune; ++i)
				if(Utils.RAND.nextInt(8) <= growth.ordinal())
					drops.add(new ItemStack(IEContent.itemHempFiber, 1));
		}
		return drops;
	}

	@OnlyIn(Dist.CLIENT)
	protected IItemProvider getSeedsItem()
	{
		return IEContent.itemSeeds;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor)
	{
		super.onNeighborChange(state, world, pos, neighbor);
		//TODO is this what this was intended to do?
		if(world.getBlockState(pos).get(GROWTH)!=EnumHempGrowth.TOP0)
			//FIXME: TEST THIS.
			if(world instanceof World)
				((World)world).notifyNeighborsOfStateChange(pos.add(0, 1, 0), this);
	}

	@Override
	public void tick(BlockState state, World world, BlockPos pos, Random random)
	{
		//TODO this.checkAndDropBlock(world, pos, state);
		int light = world.getLight(pos);
		if(light >= 12)
		{
			EnumHempGrowth growth = state.get(GROWTH);
			if(growth==EnumHempGrowth.TOP0)
				return;
			float speed = this.getGrowthSpeed(world, pos, state, light);
			if(random.nextInt((int)(50F/speed)+1)==0)
			{
				if(this.getMaxGrowth(growth)!=growth)
				{
					world.setBlockState(pos, state.with(GROWTH, growth.next()));
				}
				if(growth==getMaxGrowth(growth)&&world.isAirBlock(pos.add(0, 1, 0)))
					world.setBlockState(pos.add(0, 1, 0), state.with(GROWTH, EnumHempGrowth.TOP0));
			}
		}
	}

	private float getGrowthSpeed(World world, BlockPos pos, BlockState state, int light)
	{
		float growth = 0.125f*(light-11);
		if(world.canBlockSeeSky(pos))
			growth += 2f;
		BlockState soil = world.getBlockState(pos.add(0, -1, 0));
		if(soil.getBlock().isFertile(soil, world, pos.add(0, -1, 0)))
			growth *= 1.5f;
		return 1f+growth;
	}

	@Override
	public boolean canGrow(IBlockReader world, BlockPos pos, BlockState state, boolean isClient)
	{
		EnumHempGrowth growth = state.get(GROWTH);
		if(growth!=getMaxGrowth(growth))
			return true;
		else
			return growth==EnumHempGrowth.BOTTOM4&&world.getBlockState(pos.add(0, 1, 0)).getBlock()!=this;
	}

	//canBonemeal
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state)
	{
		return canGrow(world, pos, world.getBlockState(pos), world.isRemote);
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, BlockState state)
	{
		EnumHempGrowth growth = state.get(GROWTH);
		if(growth!=getMaxGrowth(growth))
		{
			int span = getMaxGrowth(growth).ordinal()-growth.ordinal();
			EnumHempGrowth newGrowth = growth;
			int growBy = RANDOM.nextInt(span)+1;
			for(int i = 0; i < growBy; ++i)
				newGrowth = newGrowth.next();
			world.setBlockState(pos, state.with(GROWTH, newGrowth));
			growth = newGrowth;
		}
		if(growth==EnumHempGrowth.BOTTOM4&&world.isAirBlock(pos.add(0, 1, 0)))
			world.setBlockState(pos.add(0, 1, 0), state.with(GROWTH, EnumHempGrowth.TOP0));
	}
}