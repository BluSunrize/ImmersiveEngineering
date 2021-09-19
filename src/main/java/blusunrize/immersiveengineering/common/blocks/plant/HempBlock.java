/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;

import java.util.EnumMap;
import java.util.Random;

public class HempBlock extends BushBlock implements BonemealableBlock
{
	public final String name;
	public final static EnumProperty<EnumHempGrowth> GROWTH = EnumProperty.create("growth", EnumHempGrowth.class);

	public HempBlock(String name)
	{
		super(Block.Properties.of(Material.PLANT)
				.sound(SoundType.CROP)
				.noCollission()
				.strength(0)
				.randomTicks());
		this.name = name;
		setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		//NOP
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(GROWTH);
	}

	public EnumHempGrowth getMinGrowth(EnumHempGrowth current)
	{
		if(current==EnumHempGrowth.TOP0)
			return EnumHempGrowth.TOP0;
		else
			return EnumHempGrowth.BOTTOM0;
	}

	public static EnumHempGrowth getMaxGrowth(EnumHempGrowth current)
	{
		if(current==EnumHempGrowth.TOP0)
			return EnumHempGrowth.TOP0;
		else
			return EnumHempGrowth.BOTTOM4;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		boolean b = super.canSurvive(state, world, pos);
		if(state.getValue(GROWTH)==EnumHempGrowth.TOP0)
		{
			BlockState stateBelow = world.getBlockState(pos.below());
			b = stateBelow.getBlock().equals(this)&&stateBelow.getValue(GROWTH)==getMaxGrowth(EnumHempGrowth.BOTTOM0);
		}
		return b;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter world, BlockPos pos)
	{
		//TODO improve once CropsBlock is improved
		return state.getBlock()==Blocks.FARMLAND;
	}

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos)
	{
		return PlantType.CROP;
	}

	private static final EnumMap<EnumHempGrowth, VoxelShape> shapes = new EnumMap<>(EnumHempGrowth.class);

	static
	{
		shapes.put(EnumHempGrowth.BOTTOM0, Shapes.create(
				new AABB(0, 0, 0, 1, .375f, 1)));
		shapes.put(EnumHempGrowth.BOTTOM1, Shapes.create(
				new AABB(0, 0, 0, 1, .625f, 1)));
		shapes.put(EnumHempGrowth.BOTTOM2, Shapes.create(
				new AABB(0, 0, 0, 1, .875f, 1)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return shapes.getOrDefault(state.getValue(GROWTH), Shapes.block());
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor)
	{
		super.onNeighborChange(state, world, pos, neighbor);
		//TODO is this what this was intended to do?
		if(world.getBlockState(pos).getValue(GROWTH)!=EnumHempGrowth.TOP0)
			//FIXME: TEST THIS.
			if(world instanceof Level)
				((Level)world).updateNeighborsAt(pos.offset(0, 1, 0), this);
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random)
	{
		int light = world.getMaxLocalRawBrightness(pos);
		if(light >= 12)
		{
			EnumHempGrowth growth = state.getValue(GROWTH);
			if(growth==EnumHempGrowth.TOP0)
				return;
			float speed = this.getGrowthSpeed(world, pos, state, light);
			if(random.nextInt((int)(50F/speed)+1)==0)
			{
				if(this.getMaxGrowth(growth)!=growth)
				{
					world.setBlockAndUpdate(pos, state.setValue(GROWTH, growth.next()));
				}
				if(growth==getMaxGrowth(growth)&&world.isEmptyBlock(pos.offset(0, 1, 0)))
					world.setBlockAndUpdate(pos.offset(0, 1, 0), state.setValue(GROWTH, EnumHempGrowth.TOP0));
			}
		}
	}

	private float getGrowthSpeed(Level world, BlockPos pos, BlockState state, int light)
	{
		float growth = 0.125f*(light-11);
		if(world.canSeeSkyFromBelowWater(pos))
			growth += 2f;
		BlockState soil = world.getBlockState(pos.offset(0, -1, 0));
		if(soil.getBlock().isFertile(soil, world, pos.offset(0, -1, 0)))
			growth *= 1.5f;
		return 1f+growth;
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter world, BlockPos pos, BlockState state, boolean isClient)
	{
		EnumHempGrowth growth = state.getValue(GROWTH);
		if(growth!=getMaxGrowth(growth))
			return true;
		else
			return growth==EnumHempGrowth.BOTTOM4&&world.getBlockState(pos.offset(0, 1, 0)).getBlock()!=this;
	}

	//canBonemeal
	@Override
	public boolean isBonemealSuccess(Level world, Random rand, BlockPos pos, BlockState state)
	{
		return isValidBonemealTarget(world, pos, world.getBlockState(pos), world.isClientSide);
	}

	@Override
	public void performBonemeal(ServerLevel world, Random rand, BlockPos pos, BlockState state)
	{
		EnumHempGrowth growth = state.getValue(GROWTH);
		if(growth!=getMaxGrowth(growth))
		{
			int span = getMaxGrowth(growth).ordinal()-growth.ordinal();
			EnumHempGrowth newGrowth = growth;
			int growBy = RANDOM.nextInt(span)+1;
			for(int i = 0; i < growBy; ++i)
				newGrowth = newGrowth.next();
			world.setBlockAndUpdate(pos, state.setValue(GROWTH, newGrowth));
			growth = newGrowth;
		}
		if(growth==EnumHempGrowth.BOTTOM4&&world.isEmptyBlock(pos.offset(0, 1, 0)))
			world.setBlockAndUpdate(pos.offset(0, 1, 0), state.setValue(GROWTH, EnumHempGrowth.TOP0));
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
	{
		return new ItemStack(Misc.hempSeeds);
	}
}