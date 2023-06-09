/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class IEMinecartEntity<T extends BlockEntity> extends AbstractMinecart implements MenuProvider
{
	protected T containedBlockEntity;

	protected IEMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
		this.containedBlockEntity = getTileProvider().get();
	}

	protected IEMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
		this.containedBlockEntity = getTileProvider().get();
	}

	protected abstract Supplier<T> getTileProvider();

	public T getContainedBlockEntity()
	{
		return containedBlockEntity;
	}

	public abstract void writeTileToItem(ItemStack itemStack);

	public abstract void readTileFromItem(LivingEntity placer, ItemStack itemStack);

	@Override
	@Nonnull
	public Type getMinecartType()
	{
		return Type.CHEST;
	}

	@Override
	@Nonnull
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction facing)
	{
		if(this.isAlive()&&this.containedBlockEntity!=null)
		{
			LazyOptional<C> beCap = this.containedBlockEntity.getCapability(capability, facing);
			if(beCap.isPresent())
				return beCap;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void destroy(@Nonnull DamageSource source)
	{
		this.kill();
		if(this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
		{
			ItemStack itemstack = getPickResult();
			this.writeTileToItem(itemstack);
			if(this.hasCustomName())
				itemstack.setHoverName(this.getCustomName());
			this.spawnAtLocation(itemstack);
		}
	}

	@Override
	public int getComparatorLevel()
	{
		if(this.containedBlockEntity instanceof IComparatorOverride)
			return ((IComparatorOverride)this.containedBlockEntity).getComparatorInputOverride();
		return super.getComparatorLevel();
	}

	@Override
	@Nonnull
	public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
	{
		InteractionResult superResult = super.interact(player, hand);
		if(superResult==InteractionResult.SUCCESS)
			return superResult;
		if(player instanceof ServerPlayer serverPlayer&&this.containedBlockEntity instanceof MenuProvider menuProvider)
		{
			if(menuProvider instanceof IInteractionObjectIE<?>)
				NetworkHooks.openScreen(serverPlayer, this, buffer -> buffer.writeInt(this.getId()));
			else
				NetworkHooks.openScreen(serverPlayer, this);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Nullable
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity)
	{
		return null;
	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		if(this.containedBlockEntity!=null)
		{
			CompoundTag bEntityData = this.containedBlockEntity.saveWithoutMetadata();
			compound.merge(bEntityData);
		}
	}

	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		this.containedBlockEntity = getTileProvider().get();
		this.containedBlockEntity.load(compound);
	}

	@Override
	public @Nonnull Packet<ClientGamePacketListener> getAddEntityPacket()
	{
		// TODO see fluorescent tube
		return (Packet<ClientGamePacketListener>)NetworkHooks.getEntitySpawningPacket(this);
	}

	// This is only used by the super impl of destroy, which does not allow attaching NBT to the drop. So it's actually
	// unused for our minecarts
	@Override
	protected Item getDropItem()
	{
		return Items.MINECART;
	}

	public interface MinecartConstructor
	{
		IEMinecartEntity<?> make(Level world, double x, double y, double z);
	}
}
