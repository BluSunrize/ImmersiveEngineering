package blusunrize.immersiveengineering.common.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.Lib;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockIEBase extends BlockContainer
{
	public String name;
	public String[] subNames;
	public final IIcon[][] icons;
	protected final int iconDimensions;
	public boolean hasFlavour = false;

	protected BlockIEBase(String name, Material mat, int iconDimensions, Class<? extends ItemBlockIEBase> itemBlock, String... subNames)
	{
		super(mat);
		this.adjustSound();
		this.subNames = subNames;
		this.name = name;
		this.iconDimensions = iconDimensions;
		this.icons = new IIcon[subNames.length][iconDimensions];
		this.setBlockName(ImmersiveEngineering.MODID+"."+name);
		GameRegistry.registerBlock(this, itemBlock, name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
	}

	public BlockIEBase setHasFlavour(boolean hasFlavour)
	{
		this.hasFlavour = hasFlavour;
		return this;
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

	@Override
	public int damageDropped(int meta)
	{
		return meta;
	}
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for(int i=0; i<subNames.length; i++)
			list.add(new ItemStack(item, 1, i));
	}
	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
	{
		return false;
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
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if(meta<icons.length)
			return icons[meta][getSideForTexture(side)];
		return null;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta<icons.length)
			return icons[meta][getSideForTexture(side)];
		return null;
	}
	protected int getSideForTexture(int side)
	{
		if(iconDimensions==2)
			return side==0||side==1?0: 1;
		if(iconDimensions==4)
			return side<2?side: side==2||side==3?2: 3;
		return Math.min(side, iconDimensions-1);
	}


	public abstract boolean allowHammerHarvest(int metadata);
	@Override
	public boolean isToolEffective(String type, int metadata)
	{
		if(Lib.TOOL_HAMMER.equals(type) && allowHammerHarvest(metadata))
			return true;
		return super.isToolEffective(type, metadata);
	}
	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		if (this.getMaterial().isToolNotRequired())
			return true;
		ItemStack stack = player.inventory.getCurrentItem();
		if(stack!=null && stack.getItem().getToolClasses(stack).contains(Lib.TOOL_HAMMER) && this.allowHammerHarvest(meta))
			return this.getHarvestLevel(meta)<stack.getItem().getHarvestLevel(stack, Lib.TOOL_HAMMER);
		return super.canHarvestBlock(player, meta);
	}


	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 vec0, Vec3 vec1)
	{
		//this.setBlockBoundsBasedOnState(world, x, y, z);
		//vec0 = vec0.addVector((double)(-x), (double)(-y), (double)(-z));
		//vec1 = vec1.addVector((double)(-x), (double)(-y), (double)(-z));
		return super.collisionRayTrace(world, x, y, z, vec0, vec1);
	}

	protected MovingObjectPosition rayTraceAgainstBox(int x, int y, int z,Vec3 vec0, Vec3 vec1, double minX, double maxX, double minY, double maxY, double minZ, double maxZ)
	{
		Vec3 vecMinX = vec0.getIntermediateWithXValue(vec1, this.minX);
		Vec3 vecMaxX = vec0.getIntermediateWithXValue(vec1, this.maxX);
		Vec3 vecMinY = vec0.getIntermediateWithYValue(vec1, this.minY);
		Vec3 vecMaxY = vec0.getIntermediateWithYValue(vec1, this.maxY);
		Vec3 vecMinZ = vec0.getIntermediateWithZValue(vec1, this.minZ);
		Vec3 vecMaxZ = vec0.getIntermediateWithZValue(vec1, this.maxZ);

		if (!this.isVecInsideYZBounds(vecMinX))
			vecMinX = null;
		if (!this.isVecInsideYZBounds(vecMaxX))
			vecMaxX = null;
		if (!this.isVecInsideXZBounds(vecMinY))
			vecMinY = null;
		if (!this.isVecInsideXZBounds(vecMaxY))
			vecMaxY = null;
		if (!this.isVecInsideXYBounds(vecMinZ))
			vecMinZ = null;
		if (!this.isVecInsideXYBounds(vecMaxZ))
			vecMaxZ = null;

		Vec3 vec38 = null;

		if (vecMinX != null && (vec38 == null || vec0.squareDistanceTo(vecMinX) < vec0.squareDistanceTo(vec38)))
			vec38 = vecMinX;
		if (vecMaxX != null && (vec38 == null || vec0.squareDistanceTo(vecMaxX) < vec0.squareDistanceTo(vec38)))
			vec38 = vecMaxX;
		if (vecMinY != null && (vec38 == null || vec0.squareDistanceTo(vecMinY) < vec0.squareDistanceTo(vec38)))
			vec38 = vecMinY;
		if (vecMaxY != null && (vec38 == null || vec0.squareDistanceTo(vecMaxY) < vec0.squareDistanceTo(vec38)))
			vec38 = vecMaxY;
		if (vecMinZ != null && (vec38 == null || vec0.squareDistanceTo(vecMinZ) < vec0.squareDistanceTo(vec38)))
			vec38 = vecMinZ;
		if (vecMaxZ != null && (vec38 == null || vec0.squareDistanceTo(vecMaxZ) < vec0.squareDistanceTo(vec38)))
			vec38 = vecMaxZ;

		if (vec38 == null)
			return null;
		else
		{
			byte b0 = -1;
			if (vec38 == vecMinX)
				b0 = 4;
			if (vec38 == vecMaxX)
				b0 = 5;
			if (vec38 == vecMinY)
				b0 = 0;
			if (vec38 == vecMaxY)
				b0 = 1;
			if (vec38 == vecMinZ)
				b0 = 2;
			if (vec38 == vecMaxZ)
				b0 = 3;
			return new MovingObjectPosition(x, y, z, b0, vec38.addVector((double)x, (double)y, (double)z));
		}
	}
	protected boolean isVecInsideYZBounds(Vec3 vec)
	{
		return vec == null ? false : vec.yCoord >= this.minY && vec.yCoord <= this.maxY && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
	}
	protected boolean isVecInsideXZBounds(Vec3 vec)
	{
		return vec == null ? false : vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
	}
	protected boolean isVecInsideXYBounds(Vec3 vec)
	{
		return vec == null ? false : vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY;
	}

	public static class BlockIESimple extends BlockIEBase
	{
		public BlockIESimple(String name, Material mat, Class<? extends ItemBlockIEBase> itemBlock, String... subNames)
		{
			super(name, mat, 1, itemBlock, subNames);
		}
		@Override
		public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
		{
			return true;
		}
		@Override
		public boolean isOpaqueCube()
		{
			return true;
		}
		@Override
		public boolean renderAsNormalBlock()
		{
			return true;
		}

		@Override
		public void registerBlockIcons(IIconRegister iconRegister)
		{
			for(int i=0;i<subNames.length;i++)
				icons[i][0] = iconRegister.registerIcon("immersiveengineering:"+name+"_"+subNames[i]);
		}

		@Override
	    public boolean hasTileEntity(int metadata)
	    {
	        return false;
	    }
		@Override
		public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
		{
			return null;
		}
		@Override
		public boolean allowHammerHarvest(int metadata)
		{
			return false;
		}
	}
}