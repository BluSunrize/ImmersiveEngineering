/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.proxy.DefaultProxyProvider;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import blusunrize.immersiveengineering.mixin.accessors.DataStorageAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Function;

public class WireNetworkCreator
{
	public static final IAttachmentSerializer<CompoundTag, GlobalWireNetwork> SERIALIZER = new Serializer();
	public static final Function<IAttachmentHolder, GlobalWireNetwork> CREATOR = holder -> {
		if(holder instanceof Level level)
			return new GlobalWireNetwork(
					level.isClientSide, new DefaultProxyProvider(level), new WireSyncManager(level)
			);
		else
			throw new RuntimeException("Wire networks should only ever be attached to levels, got "+holder);
	};
	private static final String SAVEDATA_KEY = Lib.MODID+"_wire_network";

	public static GlobalWireNetwork getOrCreateNetwork(Level level)
	{
		replaceOldWireNetwork(level);
		return level.getData(IEDataAttachments.WIRE_NETWORK.get());
	}

	/**
	 * If a wire network stored as "world saved data" is present, move it to the new data attachment and remove the old
	 * data.
	 */
	private static void replaceOldWireNetwork(Level level)
	{
		if(!(level instanceof ServerLevel serverLevel))
			return;
		Factory<GlobalWireNetwork> factory = new Factory<>(
				() -> {
					throw new UnsupportedOperationException();
				},
				(nbt, provider) -> {
					if(nbt.isEmpty())
						return null;
					else
						return SERIALIZER.read(level, nbt, provider);
				}
		);
		DimensionDataStorage dataStorage = serverLevel.getDataStorage();
		GlobalWireNetwork oldNet = dataStorage.get(factory, SAVEDATA_KEY);
		if(oldNet==null)
			return;
		// Old net was present: Store it in the new location, then remove it and delete the file it came from
		level.setData(IEDataAttachments.WIRE_NETWORK.get(), oldNet);
		DataStorageAccess access = (DataStorageAccess)dataStorage;
		access.getCache().remove(SAVEDATA_KEY);
		File fileToRemove = access.invokeGetDataFile(SAVEDATA_KEY);
		if(fileToRemove.exists()&&!fileToRemove.delete())
			throw new RuntimeException(
					"Failed to remove old IE wire network data at "+fileToRemove.getAbsolutePath()
			);
	}

	private static class Serializer implements IAttachmentSerializer<CompoundTag, GlobalWireNetwork>
	{
		@Override
		@Nullable
		public CompoundTag write(GlobalWireNetwork attachment, Provider provider)
		{
			return attachment.save(new CompoundTag(), provider);
		}

		@Override
		@Nonnull
		public GlobalWireNetwork read(@Nonnull IAttachmentHolder holder, @Nonnull CompoundTag tag, Provider provider)
		{
			GlobalWireNetwork baseNet = CREATOR.apply(holder);
			baseNet.readFromNBT(tag);
			return baseNet;
		}
	}
}
