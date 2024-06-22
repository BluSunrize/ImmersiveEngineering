/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.gui.IESlot.Upgrades;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.util.IESounds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static blusunrize.immersiveengineering.common.register.IEDataComponents.CHEMTHROWER_DATA;

public class ChemthrowerItem extends UpgradeableToolItem implements IAdvancedFluidItem, IScrollwheel
{
	private static final int CAPACITY = 2*FluidType.BUCKET_VOLUME;

	public ChemthrowerItem()
	{
		super(new Properties().stacksTo(1).component(CHEMTHROWER_DATA, ChemthrowerData.DEFAULT), "CHEMTHROWER");
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		int cap = getCapacity(stack, CAPACITY);

		int numberOfTanks = getUpgrades(stack).getBoolean("multitank")?3: 1;

		for(int i = 0; i < numberOfTanks; i++)
		{
			Component add = IEItemFluidHandler.fluidItemInfoFlavor(getData(stack).getFluid(i), cap);
			if(i > 0)
				TextUtils.applyFormat(
						add,
						ChatFormatting.GRAY
				);
			list.add(add);
		}
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.NONE;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
//		ToDo: Make an Upgrade Advancement?
//		if(contents[0]!=null&&contents[1]!=null&&contents[2]!=null&&contents[3]!=null)
//			Utils.unlockIEAdvancement(player, "upgrade_chemthrower");
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(player.isShiftKeyDown())
		{
			if(!world.isClientSide)
				setIgniteEnable(stack, !isIgniteEnable(stack));
		}
		else
			player.startUsingItem(hand);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int remainingUseDuration)
	{
		FluidStack fs = this.getFluid(stack);
		if(!fs.isEmpty())
		{
			int duration = getUseDuration(stack, player)-remainingUseDuration;
			int consumed = IEServerConfig.TOOLS.chemthrower_consumption.get();
			if(consumed*duration <= fs.getAmount())
			{
				Vec3 v = player.getLookAngle();
				int split = 8;
				boolean isGas = fs.getFluid().is(Fluids.GASEOUS);

				float scatter = isGas?.25f: .15f;
				float range = isGas?.5f: 1f;
				if(getUpgrades(stack).getBoolean("focus"))
				{
					range += .25f;
					scatter = .025f;
				}

				boolean ignite = ChemthrowerHandler.isFlammable(fs.getFluid())&&isIgniteEnable(stack);
				for(int i = 0; i < split; i++)
				{
					Vec3 vecDir = v.add(player.getRandom().nextGaussian()*scatter, player.getRandom().nextGaussian()*scatter, player.getRandom().nextGaussian()*scatter);
					ChemthrowerShotEntity chem = new ChemthrowerShotEntity(player.level(), player, vecDir.x*0.25, vecDir.y*0.25, vecDir.z*0.25, fs);

					// Apply momentum from the player.
					chem.setDeltaMovement(player.getDeltaMovement().add(vecDir.scale(range)));

					// Apply a small amount of backforce.
					if(!player.onGround())
						player.setDeltaMovement(player.getDeltaMovement().subtract(vecDir.scale(0.0025*range)));
					if(ignite)
						chem.igniteForSeconds(10);
					if(!player.level().isClientSide)
						player.level().addFreshEntity(chem);
				}
				if(remainingUseDuration%4==0)
				{
					if(ignite)
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.sprayFire.value(), SoundSource.PLAYERS, .5f, 1.5f);
					else
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.spray.value(), SoundSource.PLAYERS, .5f, .75f);
				}
			}
			else
				player.releaseUsingItem();
		}
		else
			player.releaseUsingItem();
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity player, int timeLeft)
	{
		final var data = getData(stack);
		FluidStack fs = data.mainFluid.copy();
		if(fs.isEmpty())
			return;
		int duration = getUseDuration(stack, player)-timeLeft;
		fs.shrink(IEServerConfig.TOOLS.chemthrower_consumption.get()*duration);
		stack.set(CHEMTHROWER_DATA, new ChemthrowerData(data.ignite, fs, data.fluid1, data.fluid2));
	}

	@Override
	public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_)
	{
		return 72000;
	}

	@Override
	public void onScrollwheel(ItemStack stack, Player playerEntity, boolean forward)
	{
		if(getUpgrades(stack).getBoolean("multitank"))
		{
			var oldData = getData(stack);

			if(forward)
				stack.set(CHEMTHROWER_DATA, new ChemthrowerData(oldData.ignite, oldData.fluid2, oldData.mainFluid, oldData.fluid1));
			else
				stack.set(CHEMTHROWER_DATA, new ChemthrowerData(oldData.ignite, oldData.fluid1, oldData.fluid2, oldData.mainFluid));
		}
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack, RegistryAccess registries)
	{
		final var data = getData(stack);
		FluidStack fs = data.mainFluid.copy();
		final var capacity = getCapacity(stack, CAPACITY);
		if(fs.getAmount() > capacity)
			stack.set(CHEMTHROWER_DATA, new ChemthrowerData(data.ignite, fs.copyWithAmount(capacity), data.fluid1, data.fluid2));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged||CapabilityShader.shouldReequipDueToShader(oldStack, newStack))
			return true;
		else
			return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public static void registerCapabilities(ItemCapabilityRegistrar registrar)
	{
		registerCapabilitiesISI(registrar);
		registrar.register(FluidHandler.ITEM, stack -> new IEItemFluidHandler(stack, CAPACITY));
		registrar.register(
				CapabilityShader.ITEM,
				stack -> new ShaderWrapper_Item(ieLoc("chemthrower"), stack)
		);
	}

	@Override
	public int getSlotCount()
	{
		return 4;
	}

	@Override
	public int getCapacity(ItemStack stack, int baseCapacity)
	{
		return baseCapacity+getUpgrades(stack).getInt("capacity");
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new Upgrades(container, toolInventory, 0, 80, 32, "CHEMTHROWER", stack, true, level, getPlayer),
				new Upgrades(container, toolInventory, 1, 100, 32, "CHEMTHROWER", stack, true, level, getPlayer)
		};
	}

	public static void setIgniteEnable(ItemStack chemthrower, boolean enabled)
	{
		var data = getData(chemthrower);
		chemthrower.set(CHEMTHROWER_DATA, new ChemthrowerData(enabled, data.mainFluid, data.fluid1, data.fluid2));
	}

	public static boolean isIgniteEnable(ItemStack chemthrower)
	{
		return getData(chemthrower).ignite;
	}

	@Override
	public FluidStack getFluid(ItemStack container)
	{
		return getData(container).mainFluid;
	}

	private static ChemthrowerData getData(ItemStack chemthrower)
	{
		return chemthrower.getOrDefault(CHEMTHROWER_DATA, ChemthrowerData.DEFAULT);
	}

	public record ChemthrowerData(
			boolean ignite, FluidStack mainFluid, FluidStack fluid1, FluidStack fluid2
	)
	{
		public static final Codec<ChemthrowerData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				Codec.BOOL.fieldOf("ignite").forGetter(ChemthrowerData::ignite),
				FluidStack.CODEC.fieldOf("mainFluid").forGetter(ChemthrowerData::mainFluid),
				FluidStack.CODEC.fieldOf("fluid1").forGetter(ChemthrowerData::fluid1),
				FluidStack.CODEC.fieldOf("fluid2").forGetter(ChemthrowerData::fluid2)
		).apply(inst, ChemthrowerData::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, ChemthrowerData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, ChemthrowerData::ignite,
				FluidStack.STREAM_CODEC, ChemthrowerData::mainFluid,
				FluidStack.STREAM_CODEC, ChemthrowerData::fluid1,
				FluidStack.STREAM_CODEC, ChemthrowerData::fluid2,
				ChemthrowerData::new
		);
		public static final ChemthrowerData DEFAULT = new ChemthrowerData(
				false, FluidStack.EMPTY, FluidStack.EMPTY, FluidStack.EMPTY
		);

		public ChemthrowerData
		{
			mainFluid = mainFluid.copy();
			fluid1 = fluid1.copy();
			fluid2 = fluid2.copy();
		}

		public FluidStack getFluid(int i)
		{
			return switch(i)
			{
				case 0 -> mainFluid;
				case 1 -> fluid1;
				case 2 -> fluid2;
				default -> throw new IllegalStateException("Unexpected value: "+i);
			};
		}
	}
}