/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.blocks.metal.TurretBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.items.BulletItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public abstract class TurretMenu extends IEContainerMenu
{
	public final TurretContext data;

	protected TurretMenu(TurretContext ctx)
	{
		super(ctx.ctx);
		this.data = ctx;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(ctx.invPlayer, j+i*9+9, 8+j*18, 109+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(ctx.invPlayer, i, 8+i*18, 167));
		addGenericData(GenericContainerData.energy(ctx.energy));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.STRINGS, ctx.targetList));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, ctx.whitelist));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, ctx.attackAnimals));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, ctx.attackPlayers));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, ctx.attackNeutrals));
	}

	public record TurretContext(
			MenuContext ctx,
			Inventory invPlayer,
			IMutableEnergyStorage energy,
			GetterAndSetter<List<String>> targetList,
			Runnable afterChange,
			GetterAndSetter<Boolean> whitelist,
			GetterAndSetter<Boolean> attackAnimals,
			GetterAndSetter<Boolean> attackPlayers,
			GetterAndSetter<Boolean> attackNeutrals
	)
	{
		public static TurretContext serverCtx(MenuType<?> type, int id, Inventory invPlayer, TurretBlockEntity<?> be)
		{
			return new TurretContext(
					IEContainerMenu.blockCtx(type, id, be),
					invPlayer,
					be.energyStorage,
					new GetterAndSetter<>(() -> be.targetList, l -> be.targetList = l),
					be::resetTarget,
					new GetterAndSetter<>(() -> be.whitelist, b -> be.whitelist = b),
					new GetterAndSetter<>(() -> be.attackAnimals, b -> be.attackAnimals = b),
					new GetterAndSetter<>(() -> be.attackPlayers, b -> be.attackPlayers = b),
					new GetterAndSetter<>(() -> be.attackNeutrals, b -> be.attackNeutrals = b)
			);
		}

		public static TurretContext clientCtx(MenuType<?> type, int id, Inventory invPlayer)
		{
			return new TurretContext(
					IEContainerMenu.clientCtx(type, id),
					invPlayer,
					new MutableEnergyStorage(TurretBlockEntity.ENERGY_CAPACITY),
					GetterAndSetter.standalone(List.of()),
					() -> {
					},
					GetterAndSetter.standalone(false),
					GetterAndSetter.standalone(false),
					GetterAndSetter.standalone(false),
					GetterAndSetter.standalone(false)
			);
		}
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(nbt.contains("add", Tag.TAG_STRING))
			data.targetList.get().add(nbt.getString("add"));
		if(nbt.contains("remove", Tag.TAG_INT))
			data.targetList.get().remove(nbt.getInt("remove"));
		if(nbt.contains("whitelist", Tag.TAG_BYTE))
			data.whitelist.set(nbt.getBoolean("whitelist"));
		if(nbt.contains("attackAnimals", Tag.TAG_BYTE))
			data.attackAnimals.set(nbt.getBoolean("attackAnimals"));
		if(nbt.contains("attackPlayers", Tag.TAG_BYTE))
			data.attackPlayers.set(nbt.getBoolean("attackPlayers"));
		if(nbt.contains("attackNeutrals", Tag.TAG_BYTE))
			data.attackNeutrals.set(nbt.getBoolean("attackNeutrals"));
		data.afterChange.run();
	}

	public static class ChemTurretMenu extends TurretMenu
	{
		public static ChemTurretMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, TurretChemBlockEntity be)
		{
			return new ChemTurretMenu(
					TurretContext.serverCtx(type, id, invPlayer, be),
					be.tank,
					new GetterAndSetter<>(() -> be.ignite, b -> be.ignite = b)
			);
		}

		public static ChemTurretMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
		{
			return new ChemTurretMenu(
					TurretContext.clientCtx(type, id, invPlayer),
					new FluidTank(TurretChemBlockEntity.TANK_VOLUME),
					GetterAndSetter.standalone(false)
			);
		}

		public final FluidTank tank;
		public final GetterAndSetter<Boolean> ignite;

		private ChemTurretMenu(TurretContext ctx, FluidTank tank, GetterAndSetter<Boolean> ignite)
		{
			super(ctx);
			this.tank = tank;
			this.ignite = ignite;
			addGenericData(GenericContainerData.fluid(tank));
			addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, ignite));
		}

		@Override
		public void receiveMessageFromScreen(CompoundTag nbt)
		{
			super.receiveMessageFromScreen(nbt);
			if(nbt.contains("ignite", Tag.TAG_BYTE))
				ignite.set(nbt.getBoolean("ignite"));
		}
	}

	public static class GunTurretMenu extends TurretMenu
	{
		public static GunTurretMenu makeServer(MenuType<?> type, int id, Inventory invPlayer, TurretGunBlockEntity be)
		{
			return new GunTurretMenu(
					TurretContext.serverCtx(type, id, invPlayer, be),
					new ItemStackHandler(be.getInventory()),
					new GetterAndSetter<>(() -> be.expelCasings, b -> be.expelCasings = b)
			);
		}

		public static GunTurretMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
		{
			return new GunTurretMenu(
					TurretContext.clientCtx(type, id, invPlayer),
					new ItemStackHandler(TurretGunBlockEntity.NUM_SLOTS),
					GetterAndSetter.standalone(false)
			);
		}

		public final GetterAndSetter<Boolean> expelCasings;

		private GunTurretMenu(TurretContext ctx, IItemHandler inv, GetterAndSetter<Boolean> expelCasings)
		{
			super(ctx);
			this.expelCasings = expelCasings;
			this.addSlot(new IESlot.Bullet(inv, 0, 134, 13, 64)
			{
				@Override
				public boolean mayPlace(ItemStack itemStack)
				{
					if(!super.mayPlace(itemStack))
						return false;
					IBullet bullet = ((BulletItem)itemStack.getItem()).getType();
					return bullet!=null&&bullet.isValidForTurret();
				}
			});
			this.addSlot(new IESlot.NewOutput(inv, 1, 134, 49));
			ownSlotCount = 2;
			addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, expelCasings));
		}

		@Override
		public void receiveMessageFromScreen(CompoundTag nbt)
		{
			super.receiveMessageFromScreen(nbt);
			if(nbt.contains("expelCasings", Tag.TAG_BYTE))
				expelCasings.set(nbt.getBoolean("expelCasings"));
		}
	}
}