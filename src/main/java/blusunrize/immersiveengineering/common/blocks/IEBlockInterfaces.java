package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IEBlockInterfaces
{
	public interface IIEMetaBlock
	{
		String getIEBlockName();
		IProperty getMetaProperty();
		Enum[] getMetaEnums();
		IBlockState getInventoryState(int meta);
		boolean useCustomStateMapper();
		String getCustomStateMapping(int meta, boolean itemBlock);

		boolean appendPropertiesToState();
	}

	public interface IAttachedIntegerProperies
	{
		String[] getIntPropertyNames();
		PropertyInteger getIntProperty(String name);
		int getIntPropertyValue(String name);
	}
	
	public interface IUsesBooleanProperty
	{
		PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf);
	}

	public interface IBlockOverlayText
	{
		String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer);
		boolean useNixieFont(EntityPlayer player, RayTraceResult mop);
	}

	public interface ISoundTile
	{
		boolean shoudlPlaySound(String sound);
	}

	public interface ISpawnInterdiction
	{
		double getInterdictionRangeSquared();
	}

	public interface IComparatorOverride
	{
		int getComparatorInputOverride();
	}

	public interface IRedstoneOutput
	{
		default int getWeakRSOutput(IBlockState state, EnumFacing side)
		{
			return getStrongRSOutput(state, side);
		}

		int getStrongRSOutput(IBlockState state, EnumFacing side);

		boolean canConnectRedstone(IBlockState state, EnumFacing side);
	}

	public interface ILightValue
	{
		int getLightValue();
	}
	
	public interface IColouredTile
	{
		int getRenderColour();
	}

	public interface IDirectionalTile
	{
		EnumFacing getFacing();
		void setFacing(EnumFacing facing);
		/**
		 * @return 0 = side clicked, 1=piston behaviour,  2 = horizontal, 3 = vertical, 4 = x/z axis, 5 = horizontal based on quadrant
		 */
		int getFacingLimitation();
		boolean mirrorFacingOnPlacement(EntityLivingBase placer);
		boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity);
	}
	public interface IAdvancedDirectionalTile extends IDirectionalTile
	{
		void onDirectionalPlacement(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer);
	}
	
	public interface IConfigurableSides
	{
		IEEnums.SideConfig getSideConfig(int side);
		default boolean toggleSide(int side, EntityPlayer p)
		{
			toggleSide(side);
			return true;
		}
		@Deprecated
		default void toggleSide(int side)
		{}
	}

	public interface ITileDrop
	{
		ItemStack getTileDrop(EntityPlayer player, IBlockState state);

		void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack);
	}
	
	public interface IPlayerInteraction
	{
		boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ);
	}
	
	public interface IHammerInteraction
	{
		boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ);
	}

	public interface IActiveState extends IUsesBooleanProperty
	{
		boolean getIsActive();
	}
	
	public interface IDualState extends IUsesBooleanProperty
	{
		boolean getIsSecondState();
	}
	
	public interface IMirrorAble extends IUsesBooleanProperty
	{
		boolean getIsMirrored();
	}

	public interface IBlockBounds
	{
		float[] getBlockBounds();
	}
	public interface IAdvancedSelectionBounds extends IBlockBounds
	{
		List<AxisAlignedBB> getAdvancedSelectionBounds();
		boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list);
	}
	public interface IAdvancedCollisionBounds extends IBlockBounds
	{
		List<AxisAlignedBB> getAdvancedColisionBounds();
	}

	public interface IHasDummyBlocks
	{
		boolean isDummy();
		void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ);
		void breakDummies(BlockPos pos, IBlockState state);
	}
	
	public interface IHasObjProperty
	{
		ArrayList<String> compileDisplayList();
	}
	public interface IAdvancedHasObjProperty
	{
		OBJState getOBJState();
	}
	public interface IDynamicTexture
	{
		@SideOnly(Side.CLIENT)
		HashMap<String,String> getTextureReplacements();
	}
	
	public interface IGuiTile
	{
		boolean canOpenGui();
		int getGuiID();
		TileEntity getGuiMaster();

		default void onGuiOpened(EntityPlayer player, boolean clientside)
		{
		}
	}
	
	public interface INeighbourChangeTile
	{
		void onNeighborBlockChange(BlockPos pos, BlockPos neightbour);
	}

	public interface IPropertyPassthrough
	{
	}
}