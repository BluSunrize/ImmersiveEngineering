/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class IEMinecartEntity<T extends IEBaseTileEntity> extends AbstractMinecartEntity implements INamedContainerProvider
{
	protected T containedTileEntity;

	protected IEMinecartEntity(EntityType<?> type, World world, double x, double y, double z)
	{
		super(type, world, x, y, z);
		this.containedTileEntity = getTileProvider().get();
	}

	protected IEMinecartEntity(EntityType<?> type, World world)
	{
		super(type, world);
		this.containedTileEntity = getTileProvider().get();
	}

	protected abstract Supplier<T> getTileProvider();

	public T getContainedTileEntity()
	{
		return containedTileEntity;
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
		if(this.isAlive()&&this.containedTileEntity!=null)
			return this.containedTileEntity.getCapability(capability, facing);
		return super.getCapability(capability, facing);
	}

	@Override
	public void killMinecart(DamageSource source)
	{
		this.remove();
		if(this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
		{
			ItemStack itemstack = getCartItem();
			this.writeTileToItem(itemstack);
			if(this.hasCustomName())
				itemstack.setDisplayName(this.getCustomName());
			this.entityDropItem(itemstack);
		}
	}

	protected abstract void invalidateCaps();

	@Override
	public int getComparatorLevel()
	{
		if(this.containedTileEntity instanceof IComparatorOverride)
			return ((IComparatorOverride)this.containedTileEntity).getComparatorInputOverride();
		return super.getComparatorLevel();
	}

	@Override
	public void remove(boolean keepData)
	{
		super.remove(keepData);
		if(!keepData)
			invalidateCaps();
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand)
	{
		if(super.processInitialInteract(player, hand))
			return true;
		if(!world.isRemote&&this.containedTileEntity instanceof IInteractionObjectIE)
		{
			NetworkHooks.openGui((ServerPlayerEntity)player, this, buffer -> buffer.writeInt(this.getEntityId()));
			return true;
		}
		return false;
	}

	@Nullable
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return GuiHandler.createContainer(playerInventory, this, id);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		if(this.containedTileEntity!=null)
			this.containedTileEntity.writeCustomNBT(compound, false);
	}

	@Override
	protected void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		this.containedTileEntity = getTileProvider().get();
		this.containedTileEntity.readCustomNBT(compound, false);
	}

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
