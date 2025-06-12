/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class IEBlockInterfaces
{
	public interface IBlockOverlayText
	{
		@Nullable
		Component[] getOverlayText(Player player, HitResult mop, boolean hammer);

		@Deprecated
		default boolean useNixieFont(Player player, HitResult mop)
		{
			return false;
		}

		;
	}

	public interface ISoundBE
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

		@Nullable
		default Pair<DyeColor, Byte>[] overrideVoltmeterRead() {
			return null;
		}
	}

	public interface IColouredBlock
	{
		boolean hasCustomBlockColours();

		int getRenderColour(BlockState state, @Nullable BlockGetter worldIn, @Nullable BlockPos pos, int tintIndex);
	}

	public interface IColouredBE
	{
		int getRenderColour(int tintIndex);
	}

	public interface IDirectionalBE
	{
		Direction getFacing();

		void setFacing(Direction facing);

		/**
		 * @return 0 = side clicked, 1=piston behaviour,  2 = horizontal, 3 = vertical, 4 = x/z axis, 5 = horizontal based on quadrant, 6 = horizontal preferring clicked side
		 */
		PlacementLimitation getFacingLimitation();

		default Direction getFacingForPlacement(BlockPlaceContext ctx)
		{
			Direction f = getFacingLimitation().getDirectionForPlacement(ctx);

			return mirrorFacingOnPlacement(ctx.getPlayer())?f.getOpposite(): f;
		}

		default boolean mirrorFacingOnPlacement(LivingEntity placer)
		{
			return false;
		}

		default boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
		{
			return true;
		}

		default void afterRotation(Direction oldDir, Direction newDir)
		{
		}

	}

	public interface BlockstateProvider
	{
		BlockState getState();

		void setState(BlockState newState);
	}

	public interface IStateBasedDirectional extends IDirectionalBE, BlockstateProvider
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

	public interface IAdvancedDirectionalBE extends IDirectionalBE
	{
		void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer);
	}

	public interface IConfigurableSides
	{
		IOSideConfig getSideConfig(Direction side);

		boolean toggleSide(Direction side, Player p);
	}

	public interface IBlockEntityDrop extends IPlacementInteraction
	{
		void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop);

		default ItemStack getPickBlock(@Nullable Player player, BlockState state, HitResult rayRes)
		{
			//TODO make this work properly on the client side
			BlockEntity tile = (BlockEntity)this;
			Mutable<ItemStack> drop = new MutableObject<>(new ItemStack(state.getBlock()));
			if(tile.getLevel() instanceof ServerLevel world)
			{
				final LootParams parms = new LootParams.Builder(world)
						.withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY)
						.withOptionalParameter(LootContextParams.BLOCK_STATE, world.getBlockState(tile.getBlockPos()))
						.withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos()))
						.create(LootContextParamSets.BLOCK);
				getBlockEntityDrop(
						new LootContext.Builder(parms).create(Optional.of(IEApi.ieLoc("pick_block"))),
						drop::setValue
				);
			}
			return drop.getValue();
		}
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
		ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ);
	}

	public interface IHammerInteraction
	{
		boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec);
	}

	public interface IHammerBlockInteraction
	{
		InteractionResult useHammer(BlockState state, Level world, BlockPos pos, Player player);
	}

	public interface IScrewdriverInteraction
	{
		ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec);
	}

	public interface IPlacementInteraction
	{
		void onBEPlaced(BlockPlaceContext ctx);
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
	 * super-interface for {@link IHasDummyBlocks}
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
	}

	public interface IInteractionObjectIE<T extends BlockEntity & IInteractionObjectIE<T>> extends MenuProvider
	{
		@Nullable
		T getGuiMaster();

		ArgContainer<? super T, ?> getContainerType();

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
			ArgContainer<? super T, ?> type = getContainerType();
			return type.create(id, playerInventory, master);
		}

		@Override
		default Component getDisplayName()
		{
			return Component.literal("");
		}
	}

	public interface IProcessBE
	{
		int[] getCurrentProcessesStep();

		int[] getCurrentProcessesMax();
	}
}