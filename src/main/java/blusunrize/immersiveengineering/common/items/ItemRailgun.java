/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.entities.EntityRailgunShot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ItemRailgun extends ItemUpgradeableTool implements IIEEnergyItem, IZoomTool, ITool, IOBJModelCallback<ItemStack>
{
	public ItemRailgun()
	{
		super("railgun", 1, "RAILGUN");
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 0, 80, 32, "RAILGUN", stack, true),
						new IESlot.Upgrades(container, inv, 1, 100, 32, "RAILGUN", stack, true)
				};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void recalculateUpgrades(ItemStack stack)
	{
		super.recalculateUpgrades(stack);
		if(this.getEnergyStored(stack) > this.getMaxEnergyStored(stack))
			ItemNBTHelper.setInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		if(this.getEnergyStored(stack) > this.getMaxEnergyStored(stack))
			ItemNBTHelper.setInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;
		if(oldStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null)&&newStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			ShaderWrapper wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			ShaderWrapper wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			if(!ItemStack.areItemStacksEqual(wrapperOld.getShaderItem(), wrapperNew.getShaderItem()))
				return true;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final EnergyHelper.ItemEnergyStorage energyStorage = new EnergyHelper.ItemEnergyStorage(stack);
				final ShaderWrapper_Item shaders = new ShaderWrapper_Item("immersiveengineering:railgun", stack);

				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
				{
					return capability==CapabilityEnergy.ENERGY||
							capability==CapabilityShader.SHADER_CAPABILITY||
							super.hasCapability(capability, facing);
				}

				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
				{
					if(capability==CapabilityEnergy.ENERGY)
						return (T)energyStorage;
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return (T)shaders;
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(I18n.format(Lib.DESC+"info.energyStored", stored));
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		//		if(stack.getItemDamage()!=1)
		//		{
		//			String tag = getRevolverDisplayTag(stack);
		//			if(!tag.isEmpty())
		//				return this.getTranslationKey()+"."+tag;
		//		}
		return super.getTranslationKey(stack);
	}

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.NONE;
	}

	private EnumHandSide getActiveSide(EntityLivingBase user)
	{
		EnumHandSide ret = user.getPrimaryHand();
		if(user.getActiveHand()!=EnumHand.MAIN_HAND)
			ret = ret==EnumHandSide.LEFT?EnumHandSide.RIGHT: EnumHandSide.LEFT;
		return ret;

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		int energy = IEConfig.Tools.railgun_consumption;
		float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
		energy = (int)(energy*energyMod);
		if(this.extractEnergy(stack, energy, true)==energy&&!findAmmo(player).isEmpty())
		{
			player.setActiveHand(hand);
			player.world.playSound(null, player.posX, player.posY, player.posZ, getChargeTime(stack) <= 20?IESounds.chargeFast: IESounds.chargeSlow, SoundCategory.PLAYERS, 1.5f, 1f);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase user, int count)
	{
		int inUse = this.getMaxItemUseDuration(stack)-count;
		if(inUse > getChargeTime(stack)&&inUse%20==user.getRNG().nextInt(20))
		{
			user.world.playSound(null, user.posX, user.posY, user.posZ, IESounds.spark, SoundCategory.PLAYERS, .8f+(.2f*user.getRNG().nextFloat()), .5f+(.5f*user.getRNG().nextFloat()));
			Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
			if(shader!=null)
			{
				Vec3d pos = Utils.getLivingFrontPos(user, .4375, user.height*.75, getActiveSide(user), false, 1);
				shader.getMiddle().getEffectFunction().execute(user.world, shader.getLeft(), stack, shader.getRight().getShaderType(), pos, null, .0625f);
			}
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int timeLeft)
	{
		if(user instanceof EntityPlayer)
		{
			int inUse = this.getMaxItemUseDuration(stack)-timeLeft;
			ItemNBTHelper.remove(stack, "inUse");
			if(inUse < getChargeTime(stack))
				return;
			int energy = IEConfig.Tools.railgun_consumption;
			float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
			energy = (int)(energy*energyMod);
			if(this.extractEnergy(stack, energy, true)==energy)
			{
				ItemStack ammo = findAmmo((EntityPlayer)user);
				if(!ammo.isEmpty())
				{
					Vec3d vec = user.getLookVec();
					float speed = 20;
					EntityRailgunShot shot = new EntityRailgunShot(user.world, user, vec.x*speed, vec.y*speed, vec.z*speed, Utils.copyStackWithAmount(ammo, 1));
					ammo.shrink(1);
					if(ammo.getCount() <= 0)
						((EntityPlayer)user).inventory.deleteStack(ammo);
					user.world.playSound(null, user.posX, user.posY, user.posZ, IESounds.railgunFire, SoundCategory.PLAYERS, 1, .5f+(.5f*user.getRNG().nextFloat()));
					this.extractEnergy(stack, energy, false);
					if(!world.isRemote)
						user.world.spawnEntity(shot);

					Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
					if(shader!=null)
					{
						Vec3d pos = Utils.getLivingFrontPos(user, .75, user.height*.75, getActiveSide(user), false, 1);
						shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), stack, shader.getRight().getShaderType(), pos, user.getLookVec(), .125f);
					}
				}
			}
		}
	}

	public static ItemStack findAmmo(EntityPlayer player)
	{
		if(isAmmo(player.getHeldItem(EnumHand.OFF_HAND)))
			return player.getHeldItem(EnumHand.OFF_HAND);
		else if(isAmmo(player.getHeldItem(EnumHand.MAIN_HAND)))
			return player.getHeldItem(EnumHand.MAIN_HAND);
		else
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);
				if(isAmmo(itemstack))
					return itemstack;
			}
		return ItemStack.EMPTY;
	}

	public static boolean isAmmo(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		RailgunHandler.RailgunProjectileProperties prop = RailgunHandler.getProjectileProperties(stack);
		return prop!=null;
	}

	public int getChargeTime(ItemStack railgun)
	{
		return (int)(40/(1+this.getUpgrades(railgun).getFloat("speed")));
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
//		ToDo: Make an Upgrade Advancement?
//		if(contents[18]!=null&&contents[19]!=null)
//			Utils.unlockIEAdvancement(player, "upgrade_railgun");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 1600;
	}


	public String[] compileRender(ItemStack stack)
	{
		HashSet<String> render = new HashSet<String>();
		render.add("frame");
		render.add("barrel");
		render.add("grip");
		render.add("capacitors");
		render.add("sled");
		render.add("wires");
		NBTTagCompound upgrades = this.getUpgrades(stack);
		if(upgrades.getDouble("speed") > 0)
			render.add("upgrade_speed");
		if(upgrades.getBoolean("scope"))
			render.add("upgrade_scope");
		return render.toArray(new String[render.size()]);
	}

	@Override
	public boolean canZoom(ItemStack stack, EntityPlayer player)
	{
		return this.getUpgrades(stack).getBoolean("scope");
	}

	float[] zoomSteps = new float[]{.1f, .15625f, .2f, .25f, .3125f, .4f, .5f, .625f};

	@Override
	public float[] getZoomSteps(ItemStack stack, EntityPlayer player)
	{
		return zoomSteps;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if(group.equals("upgrade_scope"))
			return getUpgrades(stack).getBoolean("scope");
		if(group.equals("upgrade_speed"))
			return getUpgrades(stack).getDouble("speed") > 0;
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Optional<TRSRTransformation> applyTransformations(ItemStack stack, String group, Optional<TRSRTransformation> transform)
	{
		//		if(transform.isPresent())
		//		{
		//			NBTTagCompound upgrades = this.getUpgrades(stack);
		//			Matrix4 mat = new Matrix4(transform.get().getMatrix());
		////			mat.translate(.41f,2,0);
		//			return Optional.of(new TRSRTransformation(mat.toMatrix4f()));
		//		}
		return transform;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Matrix4 handlePerspective(ItemStack stack, TransformType cameraTransformType, Matrix4 perspective, EntityLivingBase entity)
	{
		//		if(stack.)
//		if(ItemNBTHelper.getBoolean(stack, "inUse"))
//		{
//			if (cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
//				perspective = perspective.translate(-.75, -2, -.5).rotate(Math.toRadians(-78), 0, 0, 1);
//			else
//				perspective = perspective.translate(0, -.5, -.375).rotate(Math.toRadians(8), 0, 1, 0).rotate(Math.toRadians(-12), 1, 0, 0).rotate(Math.toRadians(8), 0, 0, 1);
//		}
		return perspective;
	}
}