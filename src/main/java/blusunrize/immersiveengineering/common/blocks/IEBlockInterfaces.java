/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.IModelData;

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
		@Nullable
		Component[] getOverlayText(Player player, HitResult mop, boolean hammer);

		@Deprecated
		boolean useNixieFont(Player player, HitResult mop);
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

		int getRenderColour(BlockState state, @Nullable BlockGetter worldIn, @Nullable BlockPos pos, int tintIndex);
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
					f = Direction.orderedByNearest(placer)[0];
					break;
				case HORIZONTAL:
					f = Direction.fromYRot(placer.yRot);
					break;
				case VERTICAL:
					f = (side!=Direction.DOWN&&(side==Direction.UP||hitY <= .5))?Direction.UP: Direction.DOWN;
					break;
				case HORIZONTAL_AXIS:
					f = Direction.fromYRot(placer.yRot);
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
					f = side.getAxis()!=Axis.Y?side.getOpposite(): placer.getDirection();
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

		default boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
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

		Property<Direction> getFacingProperty();

		@Override
		default Direction getFacing()
		{
			BlockState state = getState();
			if(state.hasProperty(getFacingProperty()))
				return state.getValue(getFacingProperty());
			else
				return Direction.NORTH;
		}

		@Override
		default void setFacing(Direction facing)
		{
			BlockState oldState = getState();
			BlockState newState = oldState.setValue(getFacingProperty(), facing);
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

		boolean toggleSide(Direction side, Player p);
	}

	public interface ITileDrop extends IReadOnPlacement
	{
		List<ItemStack> getTileDrops(LootContext context);

		default ItemStack getPickBlock(@Nullable Player player, BlockState state, HitResult rayRes)
		{
			//TODO make this work properly on the client side
			BlockEntity tile = (BlockEntity)this;
			if(tile.getLevel().isClientSide)
				return new ItemStack(state.getBlock());
			ServerLevel world = (ServerLevel)tile.getLevel();
			return getTileDrops(
					new Builder(world)
							.withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY)
							.withOptionalParameter(LootContextParams.BLOCK_STATE, world.getBlockState(tile.getBlockPos()))
							.withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos()))
							.create(LootContextParamSets.BLOCK)
			).get(0);
		}
	}

	public interface IReadOnPlacement
	{
		void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack);
	}

	public interface IAdditionalDrops
	{
		Collection<ItemStack> getExtraDrops(Player player, BlockState state);
	}

	public interface IEntityProof
	{
		boolean canEntityDestroy(Entity entity);
	}

	public interface IPlayerInteraction
	{
		//TODO should really return ActionResultType
		boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ);
	}

	public interface IHammerInteraction
	{
		boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec);
	}

	public interface IScrewdriverInteraction
	{
		InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec);
	}

	public interface IPlacementInteraction
	{
		void onTilePlaced(Level world, BlockPos pos, BlockState state, Direction side, float hitX, float hitY, float hitZ, LivingEntity placer, ItemStack stack);
	}

	public interface IActiveState extends BlockstateProvider
	{
		default boolean getIsActive()
		{
			BlockState state = getState();
			if(state.hasProperty(IEProperties.ACTIVE))
				return state.getValue(IEProperties.ACTIVE);
			else
				return false;
		}

		default void setActive(boolean active)
		{
			BlockState state = getState();
			BlockState newState = state.setValue(IEProperties.ACTIVE, active);
			setState(newState);
		}
	}

	public interface IMirrorAble extends BlockstateProvider
	{
		default boolean getIsMirrored()
		{
			BlockState state = getState();
			if(state.hasProperty(IEProperties.MIRRORED))
				return state.getValue(IEProperties.MIRRORED);
			else
				return false;
		}

		default void setMirrored(boolean mirrored)
		{
			BlockState state = getState();
			BlockState newState = state.setValue(IEProperties.MIRRORED, mirrored);
			setState(newState);
		}
	}

	public interface IBlockBounds extends ISelectionBounds, ICollisionBounds
	{
		@Nonnull
		VoxelShape getBlockBounds(@Nullable CollisionContext ctx);

		@Nonnull
		@Override
		default VoxelShape getCollisionShape(CollisionContext ctx)
		{
			return getBlockBounds(ctx);
		}

		@Nonnull
		@Override
		default VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
		{
			return getBlockBounds(ctx);
		}
	}

	public interface ISelectionBounds
	{
		@Nonnull
		VoxelShape getSelectionShape(@Nullable CollisionContext ctx);
	}

	public interface ICollisionBounds
	{
		@Nonnull
		VoxelShape getCollisionShape(CollisionContext ctx);
	}

	//TODO move a lot of this to block states!
	public interface IHasDummyBlocks extends IGeneralMultiblock
	{
		void placeDummies(BlockPlaceContext ctx, BlockState state);

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
			if(state.hasProperty(IEProperties.MULTIBLOCKSLAVE))
				return state.getValue(IEProperties.MULTIBLOCKSLAVE);
			else
				return true;
		}

		default void checkForNeedlessTicking()
		{
			ApiUtils.checkForNeedlessTicking((BlockEntity & IGeneralMultiblock)this,
					// The warning on the next line should be ignored, using a method reference causes
					// a "BootstrapMethodError"
					te -> te.isDummy());
		}
	}

	public interface IHasObjProperty extends IAdvancedHasObjProperty
	{
		VisibilityList compileDisplayList(BlockState state);

		@Override
		default IEObjState getIEObjState(BlockState state)
		{
			return new IEObjState(compileDisplayList(state), Transformation.identity());
		}
	}

	public interface IAdvancedHasObjProperty
	{
		IEObjState getIEObjState(BlockState state);
	}

	public interface IInteractionObjectIE<T extends BlockEntity & IInteractionObjectIE<T>> extends MenuProvider
	{
		@Nullable
		T getGuiMaster();

		TileContainer<? super T, ?> getContainerType();

		boolean canUseGui(Player player);

		default boolean isValid()
		{
			return getGuiMaster()!=null;
		}

		@Nonnull//Super is annotated nullable, but Forge assumes Nonnull in at least one place
		@Override
		default AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity)
		{
			T master = getGuiMaster();
			Preconditions.checkNotNull(master);
			TileContainer<? super T, ?> type = getContainerType();
			return type.create(id, playerInventory, master);
		}

		@Override
		default Component getDisplayName()
		{
			return new TextComponent("");
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

	public interface IModelDataBlock
	{
		IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state,
								@Nonnull IModelData tileData);
	}
}