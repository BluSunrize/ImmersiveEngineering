/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.data.IEDataGenerator;
import blusunrize.immersiveengineering.common.network.MessageMultiblockSync;
import blusunrize.immersiveengineering.common.network.MessageMultiblockSync.SyncedTemplate;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.fml.packs.ResourcePackLoader;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class StaticTemplateManager
{
	public static Map<ResourceLocation, Template> SYNCED_CLIENT_TEMPLATES = new HashMap<>();

	private static Optional<InputStream> getModResource(ResourcePackType type, ResourceLocation name, @Nullable MinecraftServer server)
	{
		if(IEDataGenerator.EXISTING_HELPER!=null)
		{
			try
			{
				int slash = name.getPath().indexOf('/');
				String prefix = name.getPath().substring(0, slash);
				ResourceLocation shortLoc = new ResourceLocation(
						name.getNamespace(),
						name.getPath().substring(slash+1)
				);
				return Optional.of(
						IEDataGenerator.EXISTING_HELPER.getResource(shortLoc, type, "", prefix)
								.getInputStream()
				);
			} catch(Exception x)
			{
				throw new RuntimeException(x);
			}
		}
		else if(server!=null)
		{
			try
			{
				IResource resource = server.getDataPackRegistries().getResourceManager().getResource(name);
				return Optional.of(resource.getInputStream());
			} catch(IOException x)
			{
				return Optional.empty();
			}
		}
		else
		{
			IELogger.error("Falling back to mod resource packs for resource {}", name);
			return ModList.get().getMods().stream()
					.map(ModInfo::getModId)
					.map(ResourcePackLoader::getResourcePackFor)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(mfrp -> mfrp.resourceExists(type, name))
					.map(mfrp -> getInputStreamOrThrow(type, name, mfrp))
					.findAny();
		}
	}

	private static InputStream getInputStreamOrThrow(ResourcePackType type, ResourceLocation name, ModFileResourcePack source)
	{
		try
		{
			return source.getResourceStream(type, name);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Template loadStaticTemplate(ResourceLocation loc, @Nullable MinecraftServer server) throws IOException
	{
		if(server==null && IEDataGenerator.EXISTING_HELPER==null)
			return Objects.requireNonNull(SYNCED_CLIENT_TEMPLATES.get(loc));
		else
		{
			String path = "structures/"+loc.getPath()+".nbt";
			Optional<InputStream> optStream = getModResource(ResourcePackType.SERVER_DATA,
					new ResourceLocation(loc.getNamespace(), path), server);
			if(optStream.isPresent())
				return loadTemplate(optStream.get());
			throw new RuntimeException("Mod resource not found: "+loc);
		}
	}

	private static Template loadTemplate(InputStream inputStreamIn) throws IOException
	{
		CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(inputStreamIn);
		Template template = new Template();
		template.read(compoundnbt);
		return template;
	}

	public static void syncMultiblockTemplates(PacketTarget target, boolean resetMBs)
	{
		List<MessageMultiblockSync.SyncedTemplate> toSync = new ArrayList<>();
		for(IMultiblock mb : MultiblockHandler.getMultiblocks())
			if(mb instanceof TemplateMultiblock)
			{
				TemplateMultiblock templateMB = (TemplateMultiblock)mb;
				if(resetMBs)
					templateMB.reset();
				Template template = templateMB.getTemplate(ServerLifecycleHooks.getCurrentServer());
				ResourceLocation rl = templateMB.getTemplateLocation();
				toSync.add(new SyncedTemplate(template, rl));
			}
		ImmersiveEngineering.packetHandler.send(
				target, new MessageMultiblockSync(toSync)
		);
	}

	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent ev)
	{
		syncMultiblockTemplates(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)ev.getEntityLiving()), false);
	}
}