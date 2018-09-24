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
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockIECrop<E extends Enum<E> & BlockIEBase.IBlockEnum> extends BlockBush implements IGrowable, IIEMetaBlock
{
	protected static IProperty[] tempProperties;
	protected static BlockRenderLayer currentRenderLayer;

	public final String name;
	public final PropertyEnum<E> property;
	public final E[] enumValues;

	public BlockIECrop(String name, PropertyEnum<E> mainProperty)
	{
		super(setTempProperties(Material.PLANTS, mainProperty));
		this.name = name;
		this.property = mainProperty;
		this.enumValues = mainProperty.getValueClass().getEnumConstants();
		this.setDefaultState(getInitDefaultState());
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setTickRandomly(true);
		this.setCreativeTab(null);
		this.setHardness(0.0F);
		this.setSoundType(SoundType.PLANT);
		this.disableStats();
//		ImmersiveEngineering.registerBlockByFullName(this, new ItemBlock(this), ImmersiveEngineering.MODID+":"+name);
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new ItemBlock(this));
	}

	@Override
	public String getIEBlockName()
	{
		return this.name;
	}

	@Override
	public Enum[] getMetaEnums()
	{
		return enumValues;
	}

	@Override
	public IBlockState getInventoryState(int meta)
	{
		IBlockState state = this.blockState.getBaseState().withProperty(this.property, enumValues[meta]);
		return state;
	}

	@Override
	public IProperty getMetaProperty()
	{
		return this.property;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return false;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public StateMapperBase getCustomMapper()
	{
		return null;
	}

	@Override
	public boolean appendPropertiesToState()
	{
		return true;
	}

	protected static Material setTempProperties(Material material, PropertyEnum<?> property)
	{
		tempProperties = new IProperty[1];
		tempProperties[0] = property;
		return material;
	}

	protected BlockStateContainer createNotTempBlockState()
	{
		IProperty[] array = new IProperty[1];
		array[0] = this.property;
		return new BlockStateContainer(this, array);
	}

	protected IBlockState getInitDefaultState()
	{
		IBlockState state = this.blockState.getBaseState().withProperty(this.property, enumValues[0]);
		return state;
	}

	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return this.property!=null?createNotTempBlockState(): new BlockStateContainer(this, tempProperties);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(this.property).getMeta();
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = this.getDefaultState().withProperty(this.property, fromMeta(meta));
		return state;
	}

	protected E fromMeta(int meta)
	{
		if(meta < 0||meta >= enumValues.length)
			meta = 0;
		return enumValues[meta];
	}

	public int getMinMeta(int meta)
	{
		return meta <= 4?0: 5;
	}

	public int getMaxMeta(int meta)
	{
		return meta <= 4?4: 5;
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state)
	{
		boolean b = super.canBlockStay(world, pos, state);
		if(this.getMetaFromState(state)==5)
		{
			IBlockState stateBelow = world.getBlockState(pos.add(0, -1, 0));
			b = stateBelow.getBlock().equals(this)&&this.getMetaFromState(stateBelow)==getMaxMeta(0);
		}
		return b;
	}

	@Override
	protected boolean canSustainBush(IBlockState state)
	{
		return state.getBlock()==Blocks.FARMLAND;
	}

	@Override
	public EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos)
	{
		return EnumPlantType.Crop;
	}

	static final AxisAlignedBB box0 = new AxisAlignedBB(0, 0, 0, 1, .375f, 1);
	static final AxisAlignedBB box1 = new AxisAlignedBB(0, 0, 0, 1, .625f, 1);
	static final AxisAlignedBB box2 = new AxisAlignedBB(0, 0, 0, 1, .875f, 1);

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		int meta = this.getMetaFromState(world.getBlockState(pos));
		return meta==0?box0: meta==1?box1: meta==2?box2: FULL_BLOCK_AABB;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		int meta = this.getMetaFromState(state);
		if(meta >= 4)
		{
			for(int i = 0; i < 3+fortune; ++i)
				if(Utils.RAND.nextInt(8) <= meta)
					drops.add(new ItemStack(IEContent.itemMaterial, 1, 4));
			drops.add(new ItemStack(IEContent.itemSeeds, 1, 0));
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos block)
	{
		super.onNeighborChange(world, pos, block);
		if(this.getMetaFromState(world.getBlockState(pos)) < getMaxMeta(0))
			//FIXME: TEST THIS.
			if(world instanceof World)
				((World)world).notifyNeighborsOfStateChange(pos.add(0, 1, 0), this, true);
	}


	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random)
	{
		this.checkAndDropBlock(world, pos, state);
		int light = world.getLight(pos);
		if(light >= 12)
		{
			int meta = this.getMetaFromState(state);
			if(meta > 4)
				return;
			float growth = this.getGrowthSpeed(world, pos, state, light);
			if(random.nextInt((int)(50F/growth)+1)==0)
			{
				if(this.getMaxMeta(meta)!=meta)
				{
					meta++;
					world.setBlockState(pos, this.getStateFromMeta(meta));
				}
				if(meta > 3&&world.isAirBlock(pos.add(0, 1, 0)))
					world.setBlockState(pos.add(0, 1, 0), this.getStateFromMeta(meta+1));
			}
		}
	}

	float getGrowthSpeed(World world, BlockPos pos, IBlockState sate, int light)
	{
		float growth = 0.125f*(light-11);
		if(world.canBlockSeeSky(pos))
			growth += 2f;
		IBlockState soil = world.getBlockState(pos.add(0, -1, 0));
		if(soil.getBlock().isFertile(world, pos.add(0, -1, 0)))
			growth *= 1.5f;
		return 1f+growth;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		IBlockState soil = worldIn.getBlockState(pos.down());
		return super.canPlaceBlockAt(worldIn, pos)&&soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
	}

	//isNotGrown
	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient)
	{
		int meta = this.getMetaFromState(state);
		if(meta < getMaxMeta(meta))
			return true;
		else
			return meta==4&&!world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(this);
	}

	//canBonemeal
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state)
	{
		int meta = this.getMetaFromState(state);
		if(meta < getMaxMeta(meta))
			return true;
		else
			return meta==4&&!world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(this);
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state)
	{
		int meta = this.getMetaFromState(state);
		if(meta < getMaxMeta(meta))
		{
			int span = getMaxMeta(meta)-meta;
			int newMeta = meta+rand.nextInt(span)+1;
			if(newMeta!=meta)
				world.setBlockState(pos, this.getStateFromMeta(newMeta));
			meta = newMeta;
		}
		if(meta==4&&world.isAirBlock(pos.add(0, 1, 0)))
			world.setBlockState(pos.add(0, 1, 0), this.getStateFromMeta(meta+1));
	}
}