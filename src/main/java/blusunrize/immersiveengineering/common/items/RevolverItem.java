/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.client.render.tooltip.RevolverServerTooltip;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.network.MessageSpeedloaderSync;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.*;

public class RevolverItem extends UpgradeableToolItem implements IBulletContainer, IZoomTool
{
	public RevolverItem()
	{
		super(new Properties().stacksTo(1), "REVOLVER");
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	public static UUID speedModUUID = Utils.generateNewUUID();
	public static UUID luckModUUID = Utils.generateNewUUID();

	/* ------------- CORE ITEM METHODS ------------- */

	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack)
	{
		return copyBulletsToShareTag(stack, super.getShareTag(stack));
	}

	public static CompoundTag copyBulletsToShareTag(ItemStack stack, CompoundTag ret)
	{
		if(ret==null)
			ret = new CompoundTag();
		else
			ret = ret.copy();
		final CompoundTag retFinal = ret;
		stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler ->
		{
			IBulletContainer container = (IBulletContainer)stack.getItem();
			NonNullList<ItemStack> bullets = NonNullList.withSize(container.getBulletCount(stack), ItemStack.EMPTY);
			for(int i = 0; i < bullets.size(); i++)
				bullets.set(i, handler.getStackInSlot(i));
			retFinal.put("bullets", ContainerHelper.saveAllItems(new CompoundTag(), bullets));
		});
		return retFinal;
	}

	@Override
	public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt)
	{
		super.readShareTag(stack, nbt);
		readBulletsFromShareTag(stack, nbt);
	}

	public static void readBulletsFromShareTag(ItemStack stack, @Nullable CompoundTag nbt)
	{
		if(nbt!=null)
			stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler ->
			{
				if(!(handler instanceof IItemHandlerModifiable modifiable))
					return;
				IBulletContainer container = (IBulletContainer)stack.getItem();
				NonNullList<ItemStack> bullets = NonNullList.withSize(container.getBulletCount(stack), ItemStack.EMPTY);
				ContainerHelper.loadAllItems(nbt.getCompound("bullets"), bullets);
				for(int i = 0; i < bullets.size(); ++i)
					modifiable.setStackInSlot(i, bullets.get(i));
			});
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "revolver"), stack));

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return shaders.cast();
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	/* ------------- INTERNAL INVENTORY ------------- */

	@Override
	public int getSlotCount()
	{
		return 18+2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, toolInventory, 18+0, 80, 32, "REVOLVER", stack, true, level, getPlayer),
						new IESlot.Upgrades(container, toolInventory, 18+1, 100, 32, "REVOLVER", stack, true, level, getPlayer)
				};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
				.ifPresent(inv -> {
					if(!inv.getStackInSlot(18).isEmpty()&&!inv.getStackInSlot(19).isEmpty())
						Utils.unlockIEAdvancement(player, "tools/upgrade_revolver");
				});
	}

	/* ------------- NAME, TOOLTIP, SUB-ITEMS ------------- */

	@Nonnull
	@Override
	public String getDescriptionId(@Nonnull ItemStack stack)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			return this.getDescriptionId()+"."+tag;
		return super.getDescriptionId(stack);
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> list, @Nonnull TooltipFlag flag)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver."+tag));
		else if(ItemNBTHelper.hasKey(stack, "flavour"))
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver."+ItemNBTHelper.getString(stack, "flavour")));
		else
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver"));

		CompoundTag perks = getPerks(stack);
		for(String key : perks.getAllKeys())
		{
			RevolverPerk perk = RevolverPerk.get(key);
			if(perk!=null)
				list.add(Component.literal("  ").append(perk.getDisplayString(perks.getDouble(key))));
		}
	}

	@Nonnull
	@Override
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack pStack)
	{
		return Optional.of(new RevolverServerTooltip(getBullets(pStack), getBulletCount(pStack)));
	}

	/* ------------- ATTRIBUTES, UPDATE, RIGHTCLICK ------------- */

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, ItemStack stack)
	{
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		if(slot==EquipmentSlot.MAINHAND)
		{
			if(getUpgrades(stack).getBoolean("fancyAnimation"))
				builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2, Operation.ADDITION));
			double melee = getUpgradeValue_d(stack, "melee");
			if(melee!=0)
			{
				builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", melee, Operation.ADDITION));
				builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, Operation.ADDITION));
			}
		}
		if(slot.getType()==Type.HAND)
		{
			double speed = getUpgradeValue_d(stack, "speed");
			if(speed!=0)
				builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(speedModUUID, "Weapon modifier", speed, Operation.MULTIPLY_BASE));

			double luck = getUpgradeValue_d(stack, RevolverPerk.LUCK.getNBTKey());
			if(luck!=0)
				builder.put(Attributes.LUCK, new AttributeModifier(luckModUUID, "Weapon modifier", luck, Operation.ADDITION));
		}
		return builder.build();
	}

	@Override
	public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		{
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				int reload = ItemNBTHelper.getInt(stack, "reload")-1;
				if(reload <= 0)
					ItemNBTHelper.remove(stack, "reload");
				else
					ItemNBTHelper.putInt(stack, "reload", reload);
			}
			if(ItemNBTHelper.hasKey(stack, "cooldown"))
			{
				int cooldown = ItemNBTHelper.getInt(stack, "cooldown")-1;
				if(cooldown <= 0)
					ItemNBTHelper.remove(stack, "cooldown");
				else
					ItemNBTHelper.putInt(stack, "cooldown", cooldown);
			}
		}
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(@Nonnull ItemStack stack)
	{
		return UseAnim.BOW;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack revolver = player.getItemInHand(hand);
		if(!world.isClientSide)
		{
			if(player.isShiftKeyDown())
			{
				openGui(player, hand);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
			}
			else if(player.getAttackStrengthScale(1) >= 1)
			{
				if(this.getUpgrades(revolver).getBoolean("nerf"))
					world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.6f);
				else
				{
					if(getShootCooldown(revolver) > 0||ItemNBTHelper.hasKey(revolver, "reload"))
						return new InteractionResultHolder<>(InteractionResult.PASS, revolver);

					NonNullList<ItemStack> bullets = getBullets(revolver);

					if(isEmpty(revolver, false))
						for(int i = 0; i < player.getInventory().getContainerSize(); i++)
						{
							ItemStack stack = player.getInventory().getItem(i);
							if(stack.getItem() instanceof SpeedloaderItem&&!((SpeedloaderItem)stack.getItem()).isEmpty(stack))
							{
								for(ItemStack b : bullets)
									if(!b.isEmpty())
										world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), b));
								setBullets(revolver, ((SpeedloaderItem)stack.getItem()).getContainedItems(stack), true);
								((SpeedloaderItem)stack.getItem()).setContainedItems(stack, NonNullList.withSize(8, ItemStack.EMPTY));
								player.getInventory().setChanged();
								if(player instanceof ServerPlayer)
									ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
											new MessageSpeedloaderSync(i, hand));

								ItemNBTHelper.putInt(revolver, "reload", 60);
								return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
							}
						}

					if(!ItemNBTHelper.hasKey(revolver, "reload"))
					{
						ItemStack bulletStack = bullets.get(0);
						Item bullet0 = bulletStack.getItem();
						if(bullet0 instanceof BulletItem)
						{
							IBullet bullet = ((BulletItem)bullet0).getType();
							if(bullet!=null)
							{
								float noise = fireProjectile(world, player, revolver, bullet, bulletStack);
								bullets.set(0, bullet.getCasing(bullets.get(0)).copy());
								// alert nearby enemies
								Utils.attractEnemies(player, 64*noise);
								// Revolvers with more than 60% noise reduction do not trigger sculk sensors
								if(noise > .2f)
								{
									// anything louder than default is considered an explosion
									GameEvent eventTriggered = noise > 0.5?GameEvent.EXPLODE: GameEvent.PROJECTILE_SHOOT;
									world.gameEvent(eventTriggered, player.position(), GameEvent.Context.of(player));
								}
							}
							else
								world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.get(), SoundSource.PLAYERS, 1f, 1f);
						}
						else
							world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.get(), SoundSource.PLAYERS, 1f, 1f);

						rotateCylinder(revolver, player, true, bullets);
						ItemNBTHelper.putInt(revolver, "cooldown", getMaxShootCooldown(revolver));
						return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
					}
				}
			}
		}
		else if(!player.isShiftKeyDown())
		{
			if(getShootCooldown(revolver) > 0||ItemNBTHelper.hasKey(revolver, "reload"))
				return new InteractionResultHolder<>(InteractionResult.PASS, revolver);
			NonNullList<ItemStack> bullets = getBullets(revolver);
			if(!bullets.get(0).isEmpty()&&bullets.get(0).getItem() instanceof BulletItem)
			{
				ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(revolver);
				if(shader!=null)
				{

					Vec3 pos = Utils.getLivingFrontPos(player, .75, player.getBbHeight()*.75, ItemUtils.getLivingHand(player, hand), false, 1);
					shader.registryEntry().getEffectFunction().execute(world, shader.shader(), revolver,
							shader.sCase().getShaderType().toString(), pos,
							Vec3.directionFromRotation(player.getRotationVector()), .125f);
				}
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
	}

	public static float fireProjectile(Level world, LivingEntity shooter, ItemStack revolver, IBullet bullet, ItemStack bulletStack)
	{
		Player player = shooter instanceof Player p?p: null;
		Vec3 vec = shooter.getLookAngle();
		boolean electro = getUpgradesStatic(revolver).getBoolean("electro");
		int count = bullet.getProjectileCount(player);
		if(count==1)
		{
			Entity entBullet = getBullet(shooter, vec, bullet, electro);
			shooter.level().addFreshEntity(bullet.getProjectile(player, bulletStack, entBullet, electro));
		}
		else
			for(int i = 0; i < count; i++)
			{
				Vec3 vecDir = vec.add(shooter.getRandom().nextGaussian()*.1, shooter.getRandom().nextGaussian()*.1, shooter.getRandom().nextGaussian()*.1);
				Entity entBullet = getBullet(shooter, vecDir, bullet, electro);
				shooter.level().addFreshEntity(bullet.getProjectile(player, bulletStack, entBullet, electro));
			}

		float noise = 0.5f;
		if(hasUpgradeValue(revolver, RevolverPerk.NOISE.getNBTKey()))
			noise *= (float)getUpgradeValue_d(revolver, RevolverPerk.NOISE.getNBTKey());
		SoundEvent sound = bullet.getSound();
		if(sound==null)
			sound = IESounds.revolverFire.get();
		world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), sound, SoundSource.PLAYERS, noise, 1f);
		return noise;
	}

	public int getShootCooldown(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "cooldown");
	}

	public int getMaxShootCooldown(ItemStack stack)
	{
		if(hasUpgradeValue(stack, RevolverPerk.COOLDOWN.getNBTKey()))
			return (int)Math.ceil(15*getUpgradeValue_d(stack, RevolverPerk.COOLDOWN.getNBTKey()));
		return 15;
	}

	/* ------------- IBulletContainer ------------- */

	@Override
	public int getBulletCount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).getInt("bullets");
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver)
	{
		return ListUtils.fromItems(this.getContainedItems(revolver).subList(0, getBulletCount(revolver)));
	}

	/* ------------- BULLET UTILITY ------------- */

	private static RevolvershotEntity getBullet(LivingEntity living, Vec3 vecDir, IBullet type, boolean electro)
	{
		RevolvershotEntity bullet = new RevolvershotEntity(living.level(), living, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, type);
		bullet.setDeltaMovement(vecDir.scale(2));
		bullet.bulletElectro = electro;
		return bullet;
	}

	public void setBullets(ItemStack revolver, NonNullList<ItemStack> bullets, boolean ignoreExtendedMag)
	{
		IItemHandlerModifiable inv = (IItemHandlerModifiable)revolver.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
				.orElseThrow(RuntimeException::new);
		for(int i = 0; i < 18; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		if(ignoreExtendedMag&&getUpgrades(revolver).getInt("bullets") > 0)
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i < 2?i: i+getUpgrades(revolver).getInt("bullets"), bullets.get(i));
		else
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i, bullets.get(i));
	}

	public void rotateCylinder(ItemStack revolver, Player player, boolean forward, NonNullList<ItemStack> bullets)
	{
		NonNullList<ItemStack> cycled = NonNullList.withSize(getBulletCount(revolver), ItemStack.EMPTY);
		int offset = forward?-1: 1;
		for(int i = 0; i < cycled.size(); i++)
			cycled.set((i+offset+cycled.size())%cycled.size(), bullets.get(i));
		setBullets(revolver, cycled, false);
		player.getInventory().setChanged();
	}

	public void rotateCylinder(ItemStack revolver, Player player, boolean forward)
	{
		NonNullList<ItemStack> bullets = getBullets(revolver);
		rotateCylinder(revolver, player, forward, bullets);
	}

	public boolean isEmpty(ItemStack stack, boolean allowCasing)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
		return invCap.map(inv -> {
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack b = inv.getStackInSlot(i);
				boolean isValid = true;
				if(!allowCasing)
					isValid = b.getItem() instanceof BulletItem;
				if(!b.isEmpty()&&isValid)
					return false;
			}
			return true;
		}).orElse(true);
	}

	/* ------------- UPGRADES & PERKS ------------- */

	@Override
	public CompoundTag getUpgradeBase(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "baseUpgrades");
	}

	public String getRevolverDisplayTag(ItemStack revolver)
	{
		String tag = ItemNBTHelper.getString(revolver, "elite");
		if(!tag.isEmpty())
		{
			int split = tag.lastIndexOf("_");
			if(split < 0)
				split = tag.length();
			return tag.substring(0, split);
		}
		return "";
	}

	public static CompoundTag getPerks(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "perks");
	}

	public static boolean hasUpgradeValue(ItemStack stack, String key)
	{
		return getUpgradesStatic(stack).contains(key)||getPerks(stack).contains(key);
	}

	public static double getUpgradeValue_d(ItemStack stack, String key)
	{
		return getUpgradesStatic(stack).getDouble(key)+getPerks(stack).getDouble(key);
	}

	@Override
	public boolean canZoom(ItemStack stack, Player player)
	{
		return hasUpgradeValue(stack, "scope");
	}

	float[] zoomSteps = new float[]{.3125f, .4f, .5f, .625f};

	@Override
	public float[] getZoomSteps(ItemStack stack, Player player)
	{
		return zoomSteps;
	}

	/* ------------- CRAFTING ------------- */

	@Override
	public void onCraftedBy(ItemStack stack, @Nonnull Level world, @Nonnull Player player)
	{
		if(stack.isEmpty()||player==null)
			return;

		String uuid = player.getUUID().toString();
		if(specialRevolvers.containsKey(uuid))
		{
			ArrayList<SpecialRevolver> list = new ArrayList<>(specialRevolvers.get(uuid));
			if(!list.isEmpty())
			{
				list.add(null);
				String existingTag = ItemNBTHelper.getString(stack, "elite");
				if(existingTag.isEmpty())
					applySpecialCrafting(stack, list.get(0));
				else
				{
					int i = 0;
					for(; i < list.size(); i++)
						if(list.get(i)!=null&&existingTag.equals(list.get(i).tag))
							break;
					int next = (i+1)%list.size();
					applySpecialCrafting(stack, list.get(next));
				}
			}
		}
		this.recalculateUpgrades(stack, world, player);
	}

	public void applySpecialCrafting(ItemStack stack, SpecialRevolver r)
	{
		if(r==null)
		{
			ItemNBTHelper.remove(stack, "elite");
			ItemNBTHelper.remove(stack, "flavour");
			ItemNBTHelper.remove(stack, "baseUpgrades");
			return;
		}
		if(r.tag!=null&&!r.tag.isEmpty())
			ItemNBTHelper.putString(stack, "elite", r.tag);
		if(r.flavour!=null&&!r.flavour.isEmpty())
			ItemNBTHelper.putString(stack, "flavour", r.flavour);
		CompoundTag baseUpgrades = new CompoundTag();
		for(Map.Entry<String, Object> e : r.baseUpgrades.entrySet())
		{
			if(e.getValue() instanceof Boolean)
				baseUpgrades.putBoolean(e.getKey(), (Boolean)e.getValue());
			else if(e.getValue() instanceof Integer)
				baseUpgrades.putInt(e.getKey(), (Integer)e.getValue());
			else if(e.getValue() instanceof Float)
				baseUpgrades.putDouble(e.getKey(), (Float)e.getValue());
			else if(e.getValue() instanceof Double)
				baseUpgrades.putDouble(e.getKey(), (Double)e.getValue());
			else if(e.getValue() instanceof String)
				baseUpgrades.putString(e.getKey(), (String)e.getValue());
		}
		ItemNBTHelper.setTagCompound(stack, "baseUpgrades", baseUpgrades);
	}

	/* ------------- RENDERING ------------- */

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;

		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		Optional<Boolean> sameShader = wrapperOld.map(wOld -> {
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w -> ItemStack.matches(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if(!sameShader.orElse(true))
			return true;
		if(ItemNBTHelper.hasKey(oldStack, "elite")||ItemNBTHelper.hasKey(newStack, "elite"))
			return !ItemNBTHelper.getString(oldStack, "elite").equals(ItemNBTHelper.getString(newStack, "elite"));

		return false;
	}

	@Nullable
	@Override
	protected ItemContainerType<?> getContainerType()
	{
		return IEMenuTypes.REVOLVER;
	}

	/* ------------- INNER CLASSES ------------- */

	public static final Multimap<String, SpecialRevolver> specialRevolvers = ArrayListMultimap.create();
	public static final Map<String, SpecialRevolver> specialRevolversByTag = new HashMap<>();

	public record SpecialRevolver(
			String[] uuid,
			String tag,
			String flavour,
			HashMap<String, Object> baseUpgrades,
			String[] renderAdditions
	)
	{
	}

	@ParametersAreNonnullByDefault
	public enum RevolverPerk
	{
		COOLDOWN(f -> f > 1,
				f -> Utils.NUMBERFORMAT_PREFIXED.format((1-f)*100),
				(l, r) -> l*r,
				1, -0.75, -0.05),
		NOISE(f -> f > 1,
				f -> Utils.NUMBERFORMAT_PREFIXED.format((f-1)*100),
				(l, r) -> l*r,
				1, -.9, -0.1),
		LUCK(f -> f < 0,
				f -> Utils.NUMBERFORMAT_PREFIXED.format(f*100),
				(l, r) -> l+r,
				0, 3, 0.5);

		private final DoublePredicate isBadValue;
		private final Function<Double, String> valueFormatter;
		private final DoubleBinaryOperator valueConcat;
		private final double generate_median;
		private final double generate_deviation;
		private final double generate_luckScale;

		RevolverPerk(DoublePredicate isBadValue, Function<Double, String> valueFormatter, DoubleBinaryOperator valueConcat, double generate_median, double generate_deviation, double generate_luckScale)
		{
			this.isBadValue = isBadValue;
			this.valueFormatter = valueFormatter;
			this.valueConcat = valueConcat;
			this.generate_median = generate_median;
			this.generate_deviation = generate_deviation;
			this.generate_luckScale = generate_luckScale;
		}

		public String getNBTKey()
		{
			return name().toLowerCase(Locale.US);
		}

		public Component getDisplayString(double value)
		{
			String key = Lib.DESC_INFO+"revolver.perk."+this.toString();
			return Component.translatable(key, valueFormatter.apply(value))
					.withStyle(isBadValue.test(value)?ChatFormatting.RED: ChatFormatting.BLUE);
		}

		public static Component getFormattedName(Component name, CompoundTag perksTag)
		{
			double averageTier = 0;
			for(String key : perksTag.getAllKeys())
			{
				RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
				double value = perksTag.getDouble(key);
				double dTier = (value-perk.generate_median)/perk.generate_deviation*3;
				averageTier += dTier;
				int iTier = (int)Mth.clamp((dTier < 0?Math.floor(dTier): Math.ceil(dTier)), -3, 3);
				if(iTier==0)
					iTier = 1;
				String translate = Lib.DESC_INFO+"revolver.perk."+perk.name().toLowerCase(Locale.US)+".tier"+iTier;
				name = Component.translatable(translate, name);
			}

			int rarityTier = (int)Math.ceil(Mth.clamp(averageTier+3, 0, 6)/6*5);
			Rarity rarity = rarityTier==5?Lib.RARITY_MASTERWORK: rarityTier==4?Rarity.EPIC: rarityTier==3?Rarity.RARE: rarityTier==2?Rarity.UNCOMMON: Rarity.COMMON;
			return name.copy().withStyle(rarity.color);
		}

		public static int calculateTier(CompoundTag perksTag)
		{
			double averageTier = 0;
			for(String key : perksTag.getAllKeys())
			{
				RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
				double value = perksTag.getDouble(key);
				double dTier = (value-perk.generate_median)/perk.generate_deviation*3;
				averageTier += dTier;
			}
			return (int)Math.ceil(Mth.clamp(averageTier+3, 0, 6)/6*5);
		}

		public double concat(double left, double right)
		{
			return this.valueConcat.applyAsDouble(left, right);
		}

		public double generateValue(RandomSource rand, boolean isBad, float luck)
		{
			double d = Utils.generateLuckInfluencedDouble(generate_median, generate_deviation, luck, rand, isBad, generate_luckScale);
			int i = (int)(d*100);
			d = i/100d;
			return d;
		}

		@Override
		public String toString()
		{
			return this.name().toLowerCase(Locale.US);
		}

		public static RevolverPerk get(String name)
		{
			try
			{
				return valueOf(name.toUpperCase(Locale.US));
			} catch(Exception e)
			{
				return null;
			}
		}

		public static RevolverPerk getRandom(RandomSource rand)
		{
			int i = rand.nextInt(values().length);
			return values()[i];
		}

		public static CompoundTag generatePerkSet(RandomSource rand, float luck)
		{
			RevolverPerk goodPerk = RevolverPerk.getRandom(rand);
			RevolverPerk badPerk = RevolverPerk.LUCK;
			//RevolverPerk.getRandom(rand);
			double val = goodPerk.generateValue(rand, false, luck);

			CompoundTag perkCompound = new CompoundTag();
			if(goodPerk==badPerk)
				val = (val+badPerk.generateValue(rand, true, luck))/2;
			else
				perkCompound.putDouble(badPerk.getNBTKey(), badPerk.generateValue(rand, true, luck));
			perkCompound.putDouble(goodPerk.getNBTKey(), val);

			return perkCompound;
		}
	}
}