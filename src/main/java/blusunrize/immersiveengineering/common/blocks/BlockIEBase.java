package blusunrize.immersiveengineering.common.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIEBase<E extends Enum<E> & BlockIEBase.IBlockEnum> extends Block implements IIEMetaBlock
{
	protected static IProperty[] tempProperties;
	protected static IUnlistedProperty[] tempUnlistedProperties;

	public final String name;
	public final PropertyEnum<E> property;
	public final IProperty[] additionalProperties;
	public final IUnlistedProperty[] additionalUnlistedProperties;
	public final E[] enumValues;
	boolean[] isMetaHidden;
	boolean[] hasFlavour;
	protected Set<EnumWorldBlockLayer> renderLayers = Sets.newHashSet(EnumWorldBlockLayer.SOLID);
	protected Set<EnumWorldBlockLayer>[] metaRenderLayers;
	protected Map<Integer, Integer> metaLightOpacities = new HashMap<>();
	public BlockIEBase(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockIEBase> itemBlock, Object... additionalProperties)
	{
		super(setTempProperties(material, mainProperty, additionalProperties));
		this.name = name;
		this.property = mainProperty;
		this.enumValues = mainProperty.getValueClass().getEnumConstants();
		this.isMetaHidden = new boolean[this.enumValues.length];
		this.hasFlavour = new boolean[this.enumValues.length];
		this.metaRenderLayers = new Set[this.enumValues.length];

		ArrayList<IProperty> propList = new ArrayList<IProperty>();
		ArrayList<IUnlistedProperty> unlistedPropList = new ArrayList<IUnlistedProperty>();
		for(Object o : additionalProperties)
		{
			if(o instanceof IProperty)
				propList.add((IProperty)o);
			if(o instanceof IProperty[])
				for(IProperty p : ((IProperty[])o))
					propList.add(p);
			if(o instanceof IUnlistedProperty)
				unlistedPropList.add((IUnlistedProperty)o);
			if(o instanceof IUnlistedProperty[])
				for(IUnlistedProperty p : ((IUnlistedProperty[])o))
					unlistedPropList.add(p);
		}
		this.additionalProperties = propList.toArray(new IProperty[propList.size()]);
		this.additionalUnlistedProperties = unlistedPropList.toArray(new IUnlistedProperty[unlistedPropList.size()]);
		this.setDefaultState(getInitDefaultState());
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.adjustSound();
		GameRegistry.registerBlock(this, itemBlock, name);
		IEContent.registeredIEBlocks.add(this);
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
		//		for(int i=0; i<this.additionalProperties.length; i++)
		//			if(this.additionalProperties[i]!=null && !this.additionalProperties[i].getAllowedValues().isEmpty())
		//				state = state.withProperty(this.additionalProperties[i], this.additionalProperties[i].getAllowedValues().toArray()[0]);
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
	public String getCustomStateMapping(int meta)
	{
		return null;
	}

	protected static Material setTempProperties(Material material, PropertyEnum<?> property, Object... additionalProperties)
	{
		ArrayList<IProperty> propList = new ArrayList<IProperty>();
		ArrayList<IUnlistedProperty> unlistedPropList = new ArrayList<IUnlistedProperty>();
		propList.add(property);
		for(Object o : additionalProperties)
		{
			if(o instanceof IProperty)
				propList.add((IProperty)o);
			if(o instanceof IProperty[])
				for(IProperty p : ((IProperty[])o))
					propList.add(p);
			if(o instanceof IUnlistedProperty)
				unlistedPropList.add((IUnlistedProperty)o);
			if(o instanceof IUnlistedProperty[])
				for(IUnlistedProperty p : ((IUnlistedProperty[])o))
					unlistedPropList.add(p);
		}
		tempProperties = propList.toArray(new IProperty[propList.size()]);
		tempUnlistedProperties = unlistedPropList.toArray(new IUnlistedProperty[unlistedPropList.size()]);
		return material;
	}
	protected static Object[] combineProperties(Object[] currentProperties, Object... addedProperties)
	{
		Object[] array = new Object[currentProperties.length + addedProperties.length];
		for(int i=0; i<currentProperties.length; i++)
			array[i] = currentProperties[i];
		for(int i=0; i<addedProperties.length; i++)
			array[currentProperties.length+i] = addedProperties[i];
		return array;
	}

	public BlockIEBase setMetaHidden(int... meta)
	{
		for(int i : meta)
			if(i>=0 && i<this.isMetaHidden.length)
				this.isMetaHidden[i] = true;
		return this;
	}
	public BlockIEBase setMetaUnhidden(int... meta)
	{
		for(int i : meta)
			if(i>=0 && i<this.isMetaHidden.length)
				this.isMetaHidden[i] = false;
		return this;
	}
	public boolean isMetaHidden(int meta)
	{
		return this.isMetaHidden[Math.max(0, Math.min(meta, this.isMetaHidden.length-1))];
	}

	public BlockIEBase setHasFlavour(int... meta)
	{
		if(meta==null||meta.length<1)
			for(int i=0; i<hasFlavour.length; i++)
				this.hasFlavour[i] = true;
		else
			for(int i : meta)
				if(i>=0 && i<this.hasFlavour.length)
					this.hasFlavour[i] = false;
		return this;
	}
	public boolean hasFlavour(ItemStack stack)
	{
		return this.hasFlavour[Math.max(0, Math.min(stack.getItemDamage(), this.hasFlavour.length-1))];
	}

	protected void setBlockLayer(EnumWorldBlockLayer... layer)
	{
		this.renderLayers = Sets.newHashSet(layer);
	}
	public BlockIEBase<E> setMetaBlockLayer(int meta, EnumWorldBlockLayer... layer)
	{
		this.metaRenderLayers[Math.max(0, Math.min(meta, this.metaRenderLayers.length-1))] = Sets.newHashSet(layer);
		return this;
	}
	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer)
	{
		if(cachedTileRequestState!=null)
		{
			int meta = this.getMetaFromState(cachedTileRequestState);
			if(meta>=0 && meta<metaRenderLayers.length && metaRenderLayers[meta]!=null)
				return metaRenderLayers[meta].contains(layer);
		}
		return renderLayers.contains(layer);
	}
	public BlockIEBase<E> setMetaLightOpacity(int meta, int opacity)
	{
		metaLightOpacities.put(meta, opacity);
		return this;
	}
	@Override
    public int getLightOpacity(IBlockAccess w, BlockPos pos)
    {
        if (!(w instanceof World))
        	return getLightOpacity();
        World world = (World) w;
        if (!world.isBlockLoaded(pos))
        	return getLightOpacity();
        int meta = getMetaFromState(world.getBlockState(pos));
        if (metaLightOpacities.containsKey(meta))
        	return metaLightOpacities.get(meta);
        return getLightOpacity();
    }
	@Override
	public int getRenderType()
	{
		return 3;
	}
	//This is a ridiculously hacky workaround, I would not recommend it to anyone.
	protected static IBlockState cachedTileRequestState;
	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		cachedTileRequestState = state;
		return super.hasTileEntity(state);
	}

	protected BlockState createNotTempBlockState()
	{
		IProperty[] array = new IProperty[1+this.additionalProperties.length];
		array[0] = this.property;
		for(int i=0; i<this.additionalProperties.length; i++)
			array[1+i] = this.additionalProperties[i];
		if(this.additionalUnlistedProperties.length>0)
			return new ExtendedBlockState(this, array, additionalUnlistedProperties);
		return new BlockState(this, array);
	}
	protected IBlockState getInitDefaultState()
	{
		IBlockState state = this.blockState.getBaseState().withProperty(this.property, enumValues[0]);
		for(int i=0; i<this.additionalProperties.length; i++)
			if(this.additionalProperties[i]!=null && !this.additionalProperties[i].getAllowedValues().isEmpty())
				state = state.withProperty(this.additionalProperties[i], this.additionalProperties[i].getAllowedValues().toArray()[0]);
		return state;
	}

	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
	}
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		return true;
	}

	@Override
	protected BlockState createBlockState()
	{
		if(this.property!=null)
			return createNotTempBlockState();
		if(tempUnlistedProperties.length>0)
			return new ExtendedBlockState(this, tempProperties, tempUnlistedProperties);
		return new BlockState(this, tempProperties);
	}
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	@Override
	public int getMetaFromState(IBlockState state)
	{
		if(state==null || !this.equals(state.getBlock()))
			return 0;
		return state.getValue(this.property).getMeta();
	}
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		for(int i=0; i<this.additionalProperties.length; i++)
			if(this.additionalProperties[i]!=null && !this.additionalProperties[i].getAllowedValues().isEmpty())
				state = state.withProperty(this.additionalProperties[i], this.additionalProperties[i].getAllowedValues().toArray()[0]);
		return state;
	}
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = this.getDefaultState().withProperty(this.property, fromMeta(meta));
		for(int i=0; i<this.additionalProperties.length; i++)
			if(this.additionalProperties[i]!=null && !this.additionalProperties[i].getAllowedValues().isEmpty())
				state = state.withProperty(this.additionalProperties[i], this.additionalProperties[i].getAllowedValues().toArray()[0]);
		return state;
		//		return this.getDefaultState().withProperty(this.property, fromMeta(meta));
	}
	protected E fromMeta(int meta)
	{
		if(meta<0||meta>=enumValues.length)
			meta = 0;
		return enumValues[meta];
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
	{
		for(E type : this.enumValues)
			if(type.listForCreative() && !this.isMetaHidden[type.getMeta()])
				list.add(new ItemStack(this, 1, type.getMeta()));
	}

	void adjustSound()
	{
		if(this.blockMaterial==Material.anvil)
			this.stepSound = Block.soundTypeAnvil;
		else if(this.blockMaterial==Material.carpet||this.blockMaterial==Material.cloth)
			this.stepSound = Block.soundTypeCloth;
		else if(this.blockMaterial==Material.glass||this.blockMaterial==Material.ice)
			this.stepSound = Block.soundTypeGlass;
		else if(this.blockMaterial==Material.grass||this.blockMaterial==Material.tnt||this.blockMaterial==Material.plants||this.blockMaterial==Material.vine)
			this.stepSound = Block.soundTypeGrass;
		else if(this.blockMaterial==Material.ground)
			this.stepSound = Block.soundTypeGravel;
		else if(this.blockMaterial==Material.iron)
			this.stepSound = Block.soundTypeMetal;
		else if(this.blockMaterial==Material.sand)
			this.stepSound = Block.soundTypeSand;
		else if(this.blockMaterial==Material.snow)
			this.stepSound = Block.soundTypeSnow;
		else if(this.blockMaterial==Material.rock)
			this.stepSound = Block.soundTypeStone;
		else if(this.blockMaterial==Material.wood||this.blockMaterial==Material.cactus)
			this.stepSound = Block.soundTypeWood;
	}

	public boolean allowHammerHarvest(IBlockState blockState)
	{
		return false;
	}
	public boolean isOpaqueCube()
	{
		return false;
	}
	public static interface IBlockEnum extends IStringSerializable
	{
		public String getName();
		public int getMeta();
		public boolean listForCreative();
	}
}