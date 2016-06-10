package blusunrize.immersiveengineering.common.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IEBlockInterfaces
{
	public interface IIEMetaBlock
	{
		public String getIEBlockName();
		public IProperty getMetaProperty();
		public Enum[] getMetaEnums();
		public IBlockState getInventoryState(int meta);
		public boolean useCustomStateMapper();
		public String getCustomStateMapping(int meta);
	}

	public interface IAttachedIntegerProperies
	{
		public String[] getIntPropertyNames();
		public PropertyInteger getIntProperty(String name);
		public int getIntPropertyValue(String name);
	}
	
	public interface IUsesBooleanProperty
	{
		public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf);
	}

	public interface IBlockOverlayText
	{
		public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer);
		public boolean useNixieFont(EntityPlayer player, MovingObjectPosition mop);
	}

	public interface ISoundTile
	{
		public boolean shoudlPlaySound(String sound);
	}

	public interface ISpawnInterdiction
	{
		public double getInterdictionRangeSquared();
	}

	public interface IComparatorOverride
	{
		public int getComparatorInputOverride();
	}

	public interface ILightValue
	{
		public int getLightValue();
	}
	
	public interface IColouredTile
	{
		public int getRenderColour();
	}

	public interface IDirectionalTile
	{
		public EnumFacing getFacing();
		public void setFacing(EnumFacing facing);
		/**
		 * @return 0 = side clicked, 1=piston behaviour,  2 = horizontal, 3 = vertical, 4 = x/z axis
		 */
		public int getFacingLimitation();
		public boolean mirrorFacingOnPlacement(EntityLivingBase placer);
		public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity);
	}
	public interface IAdvancedDirectionalTile extends IDirectionalTile
	{
		public void onDirectionalPlacement(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer);
	}
	
	public interface IConfigurableSides
	{
		public IEEnums.SideConfig getSideConfig(int side);
		public void toggleSide(int side);
	}

	public interface ITileDrop
	{
		public ItemStack getTileDrop(EntityPlayer player, IBlockState state);
		public void readOnPlacement(EntityLivingBase placer, ItemStack stack);
	}
	
	public interface IPlayerInteraction
	{
		public boolean interact(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ);
	}
	
	public interface IHammerInteraction
	{
		public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ);
	}

	public interface IActiveState extends IUsesBooleanProperty
	{
		public boolean getIsActive();
	}
	
	public interface IDualState extends IUsesBooleanProperty
	{
		public boolean getIsSecondState();
	}
	
	public interface IMirrorAble extends IUsesBooleanProperty
	{
		public boolean getIsMirrored();
	}

	public interface IBlockBounds
	{
		public float[] getBlockBounds();
		public float[] getSpecialCollisionBounds();
		public float[] getSpecialSelectionBounds();
	}
	public interface IAdvancedSelectionBounds extends IBlockBounds
	{
		public List<AxisAlignedBB> getAdvancedSelectionBounds();
		public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, MovingObjectPosition mop, ArrayList<AxisAlignedBB> list);
	}
	public interface IAdvancedCollisionBounds extends IBlockBounds
	{
		public List<AxisAlignedBB> getAdvancedColisionBounds();
	}

	public interface IHasDummyBlocks
	{
		public boolean isDummy();
		public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ);
		public void breakDummies(BlockPos pos, IBlockState state);
	}
	
	public interface IHasObjProperty
	{
		public ArrayList<String> compileDisplayList();
	}
	public interface IAdvancedHasObjProperty
	{
		public OBJState getOBJState();
	}
	public interface IDynamicTexture
	{
		@SideOnly(Side.CLIENT)
		public HashMap<String,String> getTextureReplacements();
	}
	
	public interface IGuiTile
	{
		public boolean canOpenGui();
		public int getGuiID();
		public TileEntity getGuiMaster();
	}
	
	public interface INeighbourChangeTile
	{
		public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock);
	}
}