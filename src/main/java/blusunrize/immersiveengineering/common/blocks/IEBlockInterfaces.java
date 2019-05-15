/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityMultiblockPart;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class IEBlockInterfaces
{
	public interface IAttachedIntegerProperies
	{
		String[] getIntPropertyNames();

		IntegerProperty getIntProperty(String name);

		int getIntPropertyValue(String name);

		default void setValue(String name, int value)
		{
		}
	}

	public interface IUsesBooleanProperty
	{
		@Nullable
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

	public interface IColouredBlock
	{
		boolean hasCustomBlockColours();

		int getRenderColour(IBlockState state, @Nullable IBlockReader worldIn, @Nullable BlockPos pos, int tintIndex);
	}

	public interface IColouredTile
	{
		int getRenderColour(int tintIndex);
	}

	public interface IDirectionalTile
	{
		EnumFacing getFacing();

		void setFacing(EnumFacing facing);

		/**
		 * @return 0 = side clicked, 1=piston behaviour,  2 = horizontal, 3 = vertical, 4 = x/z axis, 5 = horizontal based on quadrant, 6 = horizontal preferring clicked side
		 */
		int getFacingLimitation();

		default EnumFacing getFacingForPlacement(EntityLivingBase placer, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
		{
			EnumFacing f = EnumFacing.DOWN;
			int limit = getFacingLimitation();
			if(limit==0)
				f = side;
			else if(limit==1)
				f = EnumFacing.getDirectionFromEntityLiving(pos, placer);
			else if(limit==2)
				f = EnumFacing.fromAngle(placer.rotationYaw);
			else if(limit==3)
				f = (side!=EnumFacing.DOWN&&(side==EnumFacing.UP||hitY <= .5))?EnumFacing.UP: EnumFacing.DOWN;
			else if(limit==4)
			{
				f = EnumFacing.fromAngle(placer.rotationYaw);
				if(f==EnumFacing.SOUTH||f==EnumFacing.WEST)
					f = f.getOpposite();
			}
			else if(limit==5)
			{
				if(side.getAxis()!=Axis.Y)
					f = side.getOpposite();
				else
				{
					float xFromMid = hitX-.5f;
					float zFromMid = hitZ-.5f;
					float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
					if(max==Math.abs(xFromMid))
						f = xFromMid < 0?EnumFacing.WEST: EnumFacing.EAST;
					else
						f = zFromMid < 0?EnumFacing.NORTH: EnumFacing.SOUTH;
				}
			}
			else if(limit==6)
				f = side.getAxis()!=Axis.Y?side.getOpposite(): placer.getHorizontalFacing();

			return mirrorFacingOnPlacement(placer)?f.getOpposite(): f;
		}

		boolean mirrorFacingOnPlacement(EntityLivingBase placer);

		boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity);

		boolean canRotate(EnumFacing axis);

		default void afterRotation(EnumFacing oldDir, EnumFacing newDir)
		{
		}
	}

	public interface IAdvancedDirectionalTile extends IDirectionalTile
	{
		void onDirectionalPlacement(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer);
	}

	public interface IConfigurableSides
	{
		IEEnums.SideConfig getSideConfig(EnumFacing side);

		boolean toggleSide(EnumFacing side, EntityPlayer p);
	}

	public interface ITileDrop
	{
		/**
		 * Don't call this on generic TE'S, use getTileDrops or getPickBlock
		 */
		default ItemStack getTileDrop(@Nullable EntityPlayer player, IBlockState state)
		{
			NonNullList<ItemStack> drops = getTileDrops(player, state);
			return drops.size() > 0?drops.get(0): ItemStack.EMPTY;
		}

		default NonNullList<ItemStack> getTileDrops(@Nullable EntityPlayer player, IBlockState state)
		{
			return NonNullList.from(ItemStack.EMPTY, getTileDrop(player, state));
		}

		default ItemStack getPickBlock(@Nullable EntityPlayer player, IBlockState state, RayTraceResult rayRes)
		{
			return getTileDrop(player, state);
		}

		void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack);

		default boolean preventInventoryDrop()
		{
			return false;
		}
	}

	public interface IAdditionalDrops
	{
		Collection<ItemStack> getExtraDrops(EntityPlayer player, IBlockState state);
	}

	public interface IEntityProof
	{
		boolean canEntityDestroy(Entity entity);
	}

	public interface IPlayerInteraction
	{
		boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ);
	}

	public interface IHammerInteraction
	{
		boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ);
	}

	public interface IPlacementInteraction
	{
		void onTilePlaced(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack);
	}

	public interface IActiveState extends IUsesBooleanProperty
	{
		boolean getIsActive();

		@Nullable
		@Override
		default PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
		{
			if(inf==IActiveState.class)
				return IEProperties.ACTIVE;
			else
				return null;
		}
	}

	public interface IDualState extends IUsesBooleanProperty
	{
		boolean getIsSecondState();

		@Nullable
		@Override
		default PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
		{
			if(inf==IActiveState.class)
				return IEProperties.IS_SECOND_STATE;
			else
				return null;
		}
	}

	public interface IMirrorAble extends IUsesBooleanProperty
	{
		boolean getIsMirrored();

		@Nullable
		@Override
		default PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
		{
			if(inf==IActiveState.class)
				return IEProperties.MIRRORED;
			else
				return null;
		}
	}

	public interface IBlockBounds
	{
		float[] getBlockBounds();
	}

	public interface IFaceShape
	{
		BlockFaceShape getFaceShape(EnumFacing side);
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

	//TODO move a lot of this to block states!
	public interface IHasDummyBlocks extends IGeneralMultiblock
	{
		void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ);

		void breakDummies(BlockPos pos, IBlockState state);

		boolean isDummy();

		@Override
		default boolean isLogicDummy()
		{
			return isDummy();
		}
	}

	/**
	 * super-interface for {@link TileEntityMultiblockPart} and {@link IHasDummyBlocks}
	 */
	public interface IGeneralMultiblock
	{
		boolean isLogicDummy();
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
		@OnlyIn(Dist.CLIENT)
		HashMap<String, String> getTextureReplacements();
	}

	public interface IGuiTile
	{
		default boolean canOpenGui(EntityPlayer player)
		{
			return canOpenGui();
		}

		boolean canOpenGui();

		int getGuiID();

		@Nullable
		TileEntity getGuiMaster();

		default void onGuiOpened(EntityPlayer player, boolean clientside)
		{
		}
	}

	public interface IProcessTile
	{
		int[] getCurrentProcessesStep();

		int[] getCurrentProcessesMax();
	}

	public interface INeighbourChangeTile
	{
		void onNeighborBlockChange(BlockPos otherPos);
	}

	public interface IPropertyPassthrough
	{
	}

	public interface ICacheData
	{
		Object[] getCacheData();
	}
}