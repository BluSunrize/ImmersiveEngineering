/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class IEMinecartEntity<T extends IEBaseBlockEntity> extends AbstractMinecart implements MenuProvider
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
	public Type getMinecartType()
	{
		return Type.CHEST;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
	{
		if(this.isAlive()&&this.containedBlockEntity!=null)
		{
			LazyOptional<T> beCap = this.containedBlockEntity.getCapability(capability, facing);
			if(beCap.isPresent())
				return beCap;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void destroy(DamageSource source)
	{
		this.discard();
		if(this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
		{
			ItemStack itemstack = getCartItem();
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
	public InteractionResult interact(Player player, InteractionHand hand)
	{
		InteractionResult superResult = super.interact(player, hand);
		if(superResult==InteractionResult.SUCCESS)
			return superResult;
		if(!level.isClientSide&&this.containedBlockEntity instanceof IInteractionObjectIE)
		{
			NetworkHooks.openGui((ServerPlayer)player, this, buffer -> buffer.writeInt(this.getId()));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Nullable
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity)
	{
		return null;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		if(this.containedBlockEntity!=null)
			this.containedBlockEntity.writeCustomNBT(compound, false);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		this.containedBlockEntity = getTileProvider().get();
		this.containedBlockEntity.readCustomNBT(compound, false);
	}

	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public interface MinecartConstructor
	{
		IEMinecartEntity<?> make(Level world, double x, double y, double z);
	}
}
