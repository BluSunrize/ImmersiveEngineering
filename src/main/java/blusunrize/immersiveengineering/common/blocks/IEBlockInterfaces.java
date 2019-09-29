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
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
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
		String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer);

		boolean useNixieFont(PlayerEntity player, RayTraceResult mop);
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
		default int getWeakRSOutput(BlockState state, Direction side)
		{
			return getStrongRSOutput(state, side);
		}

		int getStrongRSOutput(BlockState state, Direction side);

		boolean canConnectRedstone(BlockState state, Direction side);
	}

	public interface ILightValue
	{
		//TODO: Note: In case lighting does not work: It seems light values are now discrete and
		//      also caches for blockstates. Either an additional Forge patch is needed (or it
		//      is buggy?) the returned light value has to correspond to the values passed as
		//      block property during block construction.
		int getLightValue();
	}

	public interface IColouredBlock
	{
		boolean hasCustomBlockColours();

		int getRenderColour(BlockState state, @Nullable IBlockReader worldIn, @Nullable BlockPos pos, int tintIndex);
	}

	public interface IColouredTile
	{
		int getRenderColour(int tintIndex);
	}

	public interface IDirectionalTile
	{
		Direction getFacing();

		void setFacing(Direction facing);

		/**
		 * @return 0 = side clicked, 1=piston behaviour,  2 = horizontal, 3 = vertical, 4 = x/z axis, 5 = horizontal based on quadrant, 6 = horizontal preferring clicked side
		 */
		PlacementLimitation getFacingLimitation();

		default Direction getFacingForPlacement(LivingEntity placer, BlockPos pos, Direction side, float hitX, float hitY, float hitZ)
		{
			Direction f;
			PlacementLimitation limit = getFacingLimitation();
			switch(limit)
			{
				case SIDE_CLICKED:
					f = side;
					break;
				case PISTON_LIKE:
					f = Direction.getFacingDirections(placer)[0];
					break;
				case HORIZONTAL:
					f = Direction.fromAngle(placer.rotationYaw);
					break;
				case VERTICAL:
					f = (side!=Direction.DOWN&&(side==Direction.UP||hitY <= .5))?Direction.UP: Direction.DOWN;
					break;
				case HORIZONTAL_AXIS:
					f = Direction.fromAngle(placer.rotationYaw);
					if(f==Direction.SOUTH||f==Direction.WEST)
						f = f.getOpposite();
					break;
				case HORIZONTAL_QUADRANT:
					if(side.getAxis()!=Axis.Y)
						f = side.getOpposite();
					else
					{
						float xFromMid = hitX-.5f;
						float zFromMid = hitZ-.5f;
						float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
						if(max==Math.abs(xFromMid))
							f = xFromMid < 0?Direction.WEST: Direction.EAST;
						else
							f = zFromMid < 0?Direction.NORTH: Direction.SOUTH;
					}
					break;
				case HORIZONTAL_PREFER_SIDE:
					f = side.getAxis()!=Axis.Y?side.getOpposite(): placer.getHorizontalFacing();
					break;
				case FIXED_DOWN:
					f = Direction.DOWN;
					break;
				default:
					throw new IllegalArgumentException("Invalid facing limitation: "+limit);
			}

			IELogger.logger.debug("Setting facing to {}", f);
			return mirrorFacingOnPlacement(placer)?f.getOpposite(): f;
		}

		boolean mirrorFacingOnPlacement(LivingEntity placer);

		boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity);

		boolean canRotate(Direction axis);

		default void afterRotation(Direction oldDir, Direction newDir)
		{
		}

		enum PlacementLimitation
		{
			SIDE_CLICKED,
			PISTON_LIKE,
			HORIZONTAL,
			VERTICAL,
			HORIZONTAL_AXIS,
			HORIZONTAL_QUADRANT,
			HORIZONTAL_PREFER_SIDE,
			FIXED_DOWN
		}
	}

	public interface BlockstateProvider
	{
		BlockState getState();

		void setState(BlockState newState);
	}

	public interface IStateBasedDirectional extends IDirectionalTile, BlockstateProvider
	{

		EnumProperty<Direction> getFacingProperty();

		@Override
		default Direction getFacing()
		{
			return getState().get(getFacingProperty());
		}

		@Override
		default void setFacing(Direction facing)
		{
			BlockState oldState = getState();
			BlockState newState = oldState.with(getFacingProperty(), facing);
			setState(newState);
		}
	}

	public interface IAdvancedDirectionalTile extends IDirectionalTile
	{
		void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer);
	}

	public interface IConfigurableSides
	{
		IEEnums.SideConfig getSideConfig(Direction side);

		boolean toggleSide(Direction side, PlayerEntity p);
	}

	public interface ITileDrop
	{
		List<ItemStack> getTileDrops(Builder context);

		default ItemStack getPickBlock(@Nullable PlayerEntity player, BlockState state, RayTraceResult rayRes)
		{
			TileEntity tile = (TileEntity)this;
			ServerWorld world = (ServerWorld)tile.getWorld();
			return getTileDrops(
					new Builder(world)
							.withNullableParameter(LootParameters.TOOL, ItemStack.EMPTY)
							.withNullableParameter(LootParameters.BLOCK_STATE, world.getBlockState(tile.getPos()))
							.withNullableParameter(LootParameters.POSITION, tile.getPos())
			).get(0);
		}

		void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack);

		default boolean preventInventoryDrop()
		{
			return false;
		}
	}

	public interface IAdditionalDrops
	{
		Collection<ItemStack> getExtraDrops(PlayerEntity player, BlockState state);
	}

	public interface IEntityProof
	{
		boolean canEntityDestroy(Entity entity);
	}

	public interface IPlayerInteraction
	{
		boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ);
	}

	public interface IHammerInteraction
	{
		boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ);
	}

	public interface IPlacementInteraction
	{
		void onTilePlaced(World world, BlockPos pos, BlockState state, Direction side, float hitX, float hitY, float hitZ, LivingEntity placer, ItemStack stack);
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

	public interface IAdvancedSelectionBounds extends IBlockBounds
	{
		List<AxisAlignedBB> getAdvancedSelectionBounds();

		boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list);
	}

	public interface IAdvancedCollisionBounds extends IBlockBounds
	{
		@Nullable
		List<AxisAlignedBB> getAdvancedColisionBounds();
	}

	//TODO move a lot of this to block states!
	public interface IHasDummyBlocks extends IGeneralMultiblock
	{
		void placeDummies(BlockItemUseContext ctx, BlockState state);

		void breakDummies(BlockPos pos, BlockState state);

		boolean isDummy();

		@Override
		default boolean isLogicDummy()
		{
			return isDummy();
		}
	}

	/**
	 * super-interface for {@link MultiblockPartTileEntity} and {@link IHasDummyBlocks}
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

	public interface IInteractionObjectIE extends INamedContainerProvider
	{
		@Nullable
		IInteractionObjectIE getGuiMaster();

		boolean canUseGui(PlayerEntity player);

		@Nullable
		@Override
		default Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
		{
			return GuiHandler.createContainer(playerInventory, (TileEntity)this, id);
		}

		@Override
		default ITextComponent getDisplayName()
		{
			return new StringTextComponent("");
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