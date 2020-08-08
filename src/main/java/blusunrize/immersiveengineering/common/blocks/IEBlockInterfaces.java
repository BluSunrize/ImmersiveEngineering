/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import com.google.common.base.Preconditions;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
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

	public interface IBlockOverlayText
	{
		String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer);

		boolean useNixieFont(PlayerEntity player, RayTraceResult mop);
	}

	public interface ISoundTile
	{
		boolean shouldPlaySound(String sound);

		default float getSoundRadiusSq()
		{
			return 256.0f;
		}
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
		default int getWeakRSOutput(Direction side)
		{
			return getStrongRSOutput(side);
		}

		int getStrongRSOutput(Direction side);

		boolean canConnectRedstone(Direction side);
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

			return mirrorFacingOnPlacement(placer)?f.getOpposite(): f;
		}

		default boolean mirrorFacingOnPlacement(LivingEntity placer)
		{
			return false;
		}

		default boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
		{
			return true;
		}

		default boolean canRotate(Direction axis)
		{
			return true;
		}

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
			BlockState state = getState();
			if(state.has(getFacingProperty()))
				return state.get(getFacingProperty());
			else
				return Direction.NORTH;
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
		IOSideConfig getSideConfig(Direction side);

		boolean toggleSide(Direction side, PlayerEntity p);
	}

	public interface ITileDrop extends IReadOnPlacement
	{
		List<ItemStack> getTileDrops(LootContext context);

		default ItemStack getPickBlock(@Nullable PlayerEntity player, BlockState state, RayTraceResult rayRes)
		{
			//TODO make this work properly on the client side
			TileEntity tile = (TileEntity)this;
			if(tile.getWorld().isRemote)
				return new ItemStack(state.getBlock());
			ServerWorld world = (ServerWorld)tile.getWorld();
			return getTileDrops(
					new Builder(world)
							.withNullableParameter(LootParameters.TOOL, ItemStack.EMPTY)
							.withNullableParameter(LootParameters.BLOCK_STATE, world.getBlockState(tile.getPos()))
							.withNullableParameter(LootParameters.POSITION, tile.getPos())
							.build(LootParameterSets.BLOCK)
			).get(0);
		}
	}

	public interface IReadOnPlacement
	{
		void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack);
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
		boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec);
	}

	public interface IScrewdriverInteraction
	{
		boolean screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec);
	}

	public interface IPlacementInteraction
	{
		void onTilePlaced(World world, BlockPos pos, BlockState state, Direction side, float hitX, float hitY, float hitZ, LivingEntity placer, ItemStack stack);
	}

	public interface IActiveState extends BlockstateProvider
	{
		default boolean getIsActive()
		{
			BlockState state = getState();
			if(state.has(IEProperties.ACTIVE))
				return state.get(IEProperties.ACTIVE);
			else
				return false;
		}

		default void setActive(boolean active)
		{
			BlockState state = getState();
			BlockState newState = state.with(IEProperties.ACTIVE, active);
			setState(newState);
		}
	}

	public interface IMirrorAble extends BlockstateProvider
	{
		default boolean getIsMirrored()
		{
			BlockState state = getState();
			if(state.has(IEProperties.MIRRORED))
				return state.get(IEProperties.MIRRORED);
			else
				return false;
		}

		default void setMirrored(boolean mirrored)
		{
			BlockState state = getState();
			BlockState newState = state.with(IEProperties.MIRRORED, mirrored);
			setState(newState);
		}
	}

	public interface IBlockBounds extends ISelectionBounds, ICollisionBounds
	{
		@Nonnull
		VoxelShape getBlockBounds(@Nullable ISelectionContext ctx);

		@Nonnull
		@Override
		default VoxelShape getCollisionShape(ISelectionContext ctx)
		{
			return getBlockBounds(ctx);
		}

		@Nonnull
		@Override
		default VoxelShape getSelectionShape(@Nullable ISelectionContext ctx)
		{
			return getBlockBounds(ctx);
		}
	}

	public interface ISelectionBounds
	{
		@Nonnull
		VoxelShape getSelectionShape(@Nullable ISelectionContext ctx);
	}

	public interface ICollisionBounds
	{
		@Nonnull
		VoxelShape getCollisionShape(ISelectionContext ctx);
	}

	//TODO move a lot of this to block states!
	public interface IHasDummyBlocks extends IGeneralMultiblock
	{
		void placeDummies(BlockItemUseContext ctx, BlockState state);

		void breakDummies(BlockPos pos, BlockState state);
	}

	/**
	 * super-interface for {@link MultiblockPartTileEntity} and {@link IHasDummyBlocks}
	 */
	public interface IGeneralMultiblock extends BlockstateProvider
	{
		@Nullable
		IGeneralMultiblock master();

		default boolean isDummy()
		{
			BlockState state = getState();
			if(state.has(IEProperties.MULTIBLOCKSLAVE))
				return state.get(IEProperties.MULTIBLOCKSLAVE);
			else
				return true;
		}
	}

	public interface IHasObjProperty extends IAdvancedHasObjProperty
	{
		VisibilityList compileDisplayList(BlockState state);

		@Override
		default IEObjState getIEObjState(BlockState state)
		{
			return new IEObjState(compileDisplayList(state), TRSRTransformation.identity());
		}
	}

	public interface IAdvancedHasObjProperty
	{
		IEObjState getIEObjState(BlockState state);
	}

	public interface IInteractionObjectIE extends INamedContainerProvider
	{
		@Nullable
		IInteractionObjectIE getGuiMaster();

		boolean canUseGui(PlayerEntity player);

		default boolean isValid()
		{
			return getGuiMaster()!=null;
		}

		@Nonnull//Super is annotated nullable, but Forge assumes Nonnull in at least one place
		@Override
		default Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
		{
			IInteractionObjectIE master = getGuiMaster();
			Preconditions.checkState(master instanceof TileEntity);
			return GuiHandler.createContainer(playerInventory, (TileEntity)master, id);
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

	public interface IPropertyPassthrough
	{
	}

	public interface ICacheData
	{
		Object[] getCacheData();
	}

	public interface IModelDataBlock
	{
		IModelData getModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state,
								@Nonnull IModelData tileData);
	}
}