package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PotionBucketItem extends IEBaseItem
{
	public PotionBucketItem()
	{
		super("potion_bucket", new Properties().maxStackSize(1), ItemGroup.BREWING);
	}

	public static ItemStack forPotion(Potion type)
	{
		if(type==Potions.WATER||type==null)
			return new ItemStack(Items.WATER_BUCKET);
		ItemStack result = new ItemStack(PotionFluid.bucket);
		result.getOrCreateTag().putString("Potion", type.getRegistryName().toString());
		return result;
	}

	public static Potion getPotion(ItemStack stack)
	{
		return PotionFluid.fromTag(stack.getTag());
	}

	@Override
	public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items)
	{
		if(!isInGroup(group))
			return;
		List<Potion> sortedPotions = new ArrayList<>(ForgeRegistries.POTION_TYPES.getValues());
		sortedPotions.sort(Comparator.comparing(e -> getPotionName(e).getString()));
		for(Potion p : sortedPotions)
			if(p!=Potions.WATER)
				items.add(forPotion(p));
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
	{
		return new FluidHandler(stack);
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName(@Nonnull ItemStack stack)
	{
		return new TranslationTextComponent(
				"item.immersiveengineering.potion_bucket", getPotionName(getPotion(stack))
		);
	}

	private static ITextComponent getPotionName(Potion potion)
	{
		String potionKey = potion.getNamePrefixed(Items.POTION.getTranslationKey()+".effect.");
		return new TranslationTextComponent(potionKey);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(
			@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn
	)
	{
		RayTraceResult rayTraceResult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.NONE);
		ItemStack stack = playerIn.getHeldItem(handIn);
		ActionResult<ItemStack> forgeResult = ForgeEventFactory.onBucketUse(playerIn, worldIn, stack, rayTraceResult);
		if(forgeResult!=null)
			return forgeResult;
		else
			return ActionResult.resultPass(stack);
	}

	@Override
	public void addInformation(
			@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn
	)
	{
		PotionUtils.addPotionTooltip(stack, tooltip, 1.0F);
	}

	private static class FluidHandler implements IFluidHandlerItem, ICapabilityProvider
	{
		private final ItemStack stack;
		private boolean empty = false;

		private FluidHandler(ItemStack stack)
		{
			this.stack = stack;
		}

		private FluidStack getFluid()
		{
			if(empty)
				return FluidStack.EMPTY;
			else
				return PotionFluid.getFluidStackForType(getPotion(stack), FluidAttributes.BUCKET_VOLUME);
		}

		@Nonnull
		@Override
		public ItemStack getContainer()
		{
			return empty?new ItemStack(Items.BUCKET): stack;
		}

		@Override
		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			if(tank==0)
				return getFluid();
			else
				return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return tank==0?FluidAttributes.BUCKET_VOLUME: 0;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action)
		{
			return 0;
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			FluidStack fluid = getFluid();
			if(!fluid.isFluidEqual(resource)||!Objects.equals(fluid.getTag(), resource.getTag()))
				return FluidStack.EMPTY;
			return drain(resource.getAmount(), action);
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			if(empty||stack.getCount() > 1||maxDrain < FluidAttributes.BUCKET_VOLUME)
				return FluidStack.EMPTY;

			FluidStack potion = getFluid();
			if(action.execute())
				empty = true;
			return potion;
		}

		private final LazyOptional<IFluidHandlerItem> lazyOpt = CapabilityUtils.constantOptional(this);

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
		{
			if(cap==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
				return lazyOpt.cast();
			else
				return LazyOptional.empty();
		}
	}
}
