/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class IEPacketBuffer extends PacketBuffer
{
	private IEPacketBuffer(PacketBuffer vanillaBuffer)
	{
		super(vanillaBuffer);
	}

	public static IEPacketBuffer wrap(PacketBuffer base)
	{
		if(base instanceof IEPacketBuffer)
			return (IEPacketBuffer)base;
		else
			return new IEPacketBuffer(base);
	}

	// List read/write utils (standard format of size followed by x elements)
	public <T> List<T> readList(Function<? super IEPacketBuffer, T> readElement)
	{
		int numElements = readVarInt();
		List<T> ret = new ArrayList<>(numElements);
		for(int i = 0; i < numElements; ++i)
			ret.add(readElement.apply(this));
		return ret;
	}

	public <T> void writeList(Collection<T> toWrite, BiConsumer<T, ? super IEPacketBuffer> writeElement)
	{
		writeVarInt(toWrite.size());
		for(T element : toWrite)
			writeElement.accept(element, this);
	}

	// Optimized versions of read/writeItemStack:
	// Remove "Damage: 0" tag from undamaged items
	// Use ID==0 <=> empty to save a byte
	@Nonnull
	@Override
	public PacketBuffer writeItemStack(@Nonnull ItemStack stack, boolean limitedTag)
	{
		final String damageKey = "Damage";
		if(stack.isEmpty())
		{
			this.writeVarInt(0);
			return this;
		}
		Item item = stack.getItem();
		int id = Item.getIdFromItem(item);
		Preconditions.checkState(id!=0);
		this.writeVarInt(id);
		this.writeByte(stack.getCount());
		CompoundNBT compoundnbt = null;
		if(item.isDamageable()||item.shouldSyncTag())
			compoundnbt = limitedTag?stack.getShareTag(): stack.getTag();

		if(compoundnbt!=null&&item.isDamageable())
		{
			INBT damage = compoundnbt.get(damageKey);
			if(damage instanceof IntNBT&&((IntNBT)damage).getInt()==0)
			{
				compoundnbt = compoundnbt.copy();
				compoundnbt.remove(damageKey);
				if(compoundnbt.isEmpty())
					compoundnbt = null;
			}
		}
		this.writeCompoundTag(compoundnbt);

		return this;
	}

	@Nonnull
	@Override
	public ItemStack readItemStack()
	{
		int itemID = this.readVarInt();
		if(itemID==0)
			return ItemStack.EMPTY;
		int size = this.readByte();
		Item item = Item.getItemById(itemID);
		ItemStack parsedStack = new ItemStack(item, size);
		parsedStack.readShareTag(this.readCompoundTag());
		return parsedStack;
	}

	// read/writeFluidStack:
	// - Safe one byte by using empty fluid to signify empty fluid stack
	// - Use writeRegistryIdUnsafe instead of writeRegistryId since the latter writes the registry name as an RL
	@Override
	public void writeFluidStack(FluidStack stack)
	{
		writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, stack.getFluid());
		if(!stack.isEmpty())
		{
			writeVarInt(stack.getAmount());
			writeCompoundTag(stack.getTag());
		}
	}

	@Override
	public FluidStack readFluidStack()
	{
		Fluid fluid = readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
		if(fluid==Fluids.EMPTY)
			return FluidStack.EMPTY;
		int amount = readVarInt();
		CompoundNBT tag = readCompoundTag();
		return new FluidStack(fluid, amount, tag);
	}

	private static final int NBT_MAGIC_ONLY_POTION;
	private static final int NBT_MAGIC_POTION_AND_TAG;

	static
	{
		int i = 0;
		while(!NBTTypes
				//getType
				.func_229710_a_(i)
				//getName
				.func_225650_b_().startsWith("UNKNOWN_"))
			++i;
		NBT_MAGIC_ONLY_POTION = i+1;
		NBT_MAGIC_POTION_AND_TAG = NBT_MAGIC_ONLY_POTION+1;
	}

	// read/writeCompoundTag with optimization for potion NBT tags (send registry ID instead of RL)
	@Nonnull
	@Override
	public PacketBuffer writeCompoundTag(@Nullable CompoundNBT nbt)
	{
		if(nbt==null)
		{
			this.writeByte(0);
			return this;
		}
		Potion potion = PotionUtils.getPotionTypeFromNBT(nbt);
		if(potion!=Potions.EMPTY)
		{
			int tag;
			if(nbt.size()==1)
			{
				tag = NBT_MAGIC_ONLY_POTION;
				nbt = null;
			}
			else
			{
				tag = NBT_MAGIC_POTION_AND_TAG;
				nbt = nbt.copy();
				nbt.remove("Potion");
			}
			writeVarInt(tag);
			writeRegistryIdUnsafe(ForgeRegistries.POTION_TYPES, potion);
		}
		if(nbt!=null)
			try
			{
				CompressedStreamTools.write(nbt, new ByteBufOutputStream(this));
			} catch(IOException xcp)
			{
				throw new EncoderException(xcp);
			}

		return this;
	}

	@Nullable
	@Override
	public CompoundNBT readCompoundTag()
	{
		int startIndex = this.readerIndex();
		byte typeByte = this.readByte();
		CompoundNBT baseNBT = new CompoundNBT();
		if(typeByte==0)
			return null;
		else if(typeByte==NBT_MAGIC_ONLY_POTION||typeByte==NBT_MAGIC_POTION_AND_TAG)
		{
			Potion potion = readRegistryIdUnsafe(ForgeRegistries.POTION_TYPES);
			if(potion!=Potions.EMPTY)
				baseNBT.putString("Potion", potion.getRegistryName().toString());
			if(typeByte==NBT_MAGIC_ONLY_POTION)
				return baseNBT;
		}
		else
			this.readerIndex(startIndex);

		try
		{
			CompoundNBT mainNBT = CompressedStreamTools.read(
					new ByteBufInputStream(this), new NBTSizeTracker(2097152L)
			);
			return mainNBT.merge(baseNBT);
		} catch(IOException xcp)
		{
			throw new EncoderException(xcp);
		}
	}

	// read/writeResourceLocation: Special cases for IE and MC to get rid of the namespace

	// "I" can never appear in an RL, so if the string in the buffer starts with "I" it can't be anything but the
	// custom format for IE RLs
	private static final char IE_PREFIX = 'I';

	@Nonnull
	@Override
	public PacketBuffer writeResourceLocation(@Nonnull ResourceLocation resourceLocationIn)
	{
		if("minecraft".equals(resourceLocationIn.getNamespace()))
			// MC is the default namespace, so we don't need to send if
			return writeString(resourceLocationIn.getPath());
		else if(Lib.MODID.equals(resourceLocationIn.getNamespace()))
			// Special 1-character prefix for IE
			return writeString(IE_PREFIX+resourceLocationIn.getPath());
		else
			return writeString(resourceLocationIn.toString());
	}

	@Nonnull
	@Override
	public ResourceLocation readResourceLocation()
	{
		String data = readString();
		if(data.charAt(0)==IE_PREFIX)
			return new ResourceLocation(Lib.MODID, data.substring(1));
		else
			return new ResourceLocation(data);
	}
}
