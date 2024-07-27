/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

public class SirenBlockEntity extends ImmersiveConnectableBlockEntity
		implements IEServerTickableBE, IStateBasedDirectional, IScrewdriverInteraction,
		IBlockBounds, IRedstoneConnector, IBlockOverlayText
{
	private static final Vec3 CONNECTION_OFFSET = new Vec3(0.5, 0.875, 0.5);
	private final double RADIUS = 48;
	boolean active = false;
	int activeTicks = 0;

	public DyeColor redstoneChannel = DyeColor.WHITE;
	public SirenSound sound = SirenSound.SIREN;

	public SirenBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.SIREN.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(active)
		{
			if(activeTicks%160==0) // sound refreshes after 8 seconds
				getLevelNonnull().playSound(null, getPosition(), this.sound.sound.value(), SoundSource.BLOCKS, 4, 1);
			if(activeTicks%80==0) // entities update after 4 seconds
			{
				AABB aabb = new AABB(getPosition()).inflate(RADIUS);
				List<LivingEntity> nearbyEntities = getLevelNonnull().getEntitiesOfClass(LivingEntity.class, aabb, entity -> entity.isAlive()&&!entity.isRemoved());
				nearbyEntities.forEach(entity -> {
					// hear the bell
					entity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, getLevelNonnull().getGameTime());
					// make raiders glow
					if(entity.getType().is(EntityTypeTags.RAIDERS))
						entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80));
				});
			}
			activeTicks++;
		}
		else if(activeTicks > 0)
			activeTicks = 0;
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!level.isClientSide&&SafeChunkUtils.isChunkSafe(level, worldPosition))
		{
			final boolean oldActive = this.active;
			this.active = handler.getValue(this.redstoneChannel.getId()) > 0;
			if(this.active!=oldActive)
				this.markContainingBlockForUpdate(null);
		}
	}


	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
	}

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide)
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_Siren, this);
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getInt("redstoneChannel"));
		if(message.contains("sound"))
			sound = SirenSound.BY_ORDINAL.apply(message.getInt("sound"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putInt("redstoneChannel", redstoneChannel.getId());
		nbt.putInt("sound", sound.ordinal());
		nbt.putBoolean("active", active);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel"));
		sound = SirenSound.BY_ORDINAL.apply(nbt.getInt("sound"));
		active = nbt.getBoolean("active");
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		return CONNECTION_OFFSET;
	}

	private static final List<AABB> BASE_SHAPE = List.of(
			new AABB(0, .25, .25, 1, .75, .75),
			new AABB(.25, .25, .25, .75, .75, 1),
			new AABB(.3125, .75, .3125, .6875, 1, .6875)
	);

	private static final CachedShapesWithTransform<Direction, Direction> SHAPES = new CachedShapesWithTransform<>(direction -> {
		ArrayList<AABB> list = new ArrayList<>(BASE_SHAPE);
		if(direction.getAxis()==Axis.Y)
		{
			list.add(new AABB(.3125, 0, .3125, .6875, .25, .6875));
			list.add(new AABB(.25, .25, 0, .75, .75, .25));
		}
		else
			list.add(new AABB(.3125, .1875, 0, .6875, .6875, .25));
		return list;
	}, (direction, aabb) -> direction.getAxis()==Axis.Y?aabb: ShapeUtils.transformAABB(aabb, direction));

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		Direction f = getFacing();
		return SHAPES.get(f, f);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE_NO_DOWN;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return null;
		return new Component[]{
				Component.translatable(Lib.DESC_INFO+"redstoneChannel", I18n.get("color.minecraft."+redstoneChannel.getName()))
		};
	}


	public enum SirenSound
	{
		SIREN(IESounds.siren),
		KLAXON(IESounds.klaxon),
		BUZZER(IESounds.buzzer);

		private static final IntFunction<SirenSound> BY_ORDINAL = ByIdMap.continuous(SirenSound::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

		private final Holder<SoundEvent> sound;

		SirenSound(Holder<SoundEvent> sound)
		{
			this.sound = sound;
		}

		public Component getComponent()
		{
			return Component.translatable(Lib.GUI_CONFIG+"siren.sound."+name().toLowerCase(Locale.ENGLISH));
		}
	}
}